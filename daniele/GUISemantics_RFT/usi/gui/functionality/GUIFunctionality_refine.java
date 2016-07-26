package usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.gui.GuiStateManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.ripping.Ripper;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.Go;
import usi.gui.semantic.testcase.Select;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;

import com.google.common.collect.Lists;

import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUIFunctionality_refine {

	private final GUI gui;
	private final Instance_GUI_pattern instancePattern;
	private List<String> unvalid_constraints;
	// additional list of unvalid constraints that has to be adapted during the
	// window search
	List<String> additional_constraints;
	private String valid_constraint;
	private final GUI_Pattern pattern;
	private final List<GUITestCase> observed_tcs;
	private final Ripper ripper;
	private final List<String> covered_dyn_edges;

	public GUIFunctionality_refine(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.gui = gui;
		this.instancePattern = instancePattern.clone();

		this.pattern = this.instancePattern.getGuipattern();
		this.observed_tcs = new ArrayList<>();
		this.ripper = new Ripper(ConfigurationManager.getSleepTime(), this.gui);
		this.covered_dyn_edges = new ArrayList<>();
	}

	public Instance_GUI_pattern refine() throws Exception {

		this.valid_constraint = "";
		this.unvalid_constraints = new ArrayList<>();

		String old_valid_constraints;
		int old_unvalid_constraints_size;
		int old_windows_number;
		int old_edges_number;
		do {
			old_valid_constraints = this.valid_constraint;
			old_unvalid_constraints_size = this.unvalid_constraints.size();
			old_windows_number = this.instancePattern.getWindows().size();
			old_edges_number = this.instancePattern.getGui().getNumberOfEdges();

			this.discoverWindows();
			this.discoverDynamicEdges();
			// if something has changed we iterate again
		} while (!old_valid_constraints.equals(this.valid_constraint)
				|| old_unvalid_constraints_size != this.unvalid_constraints.size()
				|| old_windows_number != this.instancePattern.getWindows().size()
				|| old_edges_number != this.instancePattern.getGui().getNumberOfEdges());

		System.out.println("RESULT: " + this.valid_constraint);

		if (this.pattern.isInstance(this.instancePattern)) {

			final List<String> constraints = new ArrayList<>();
			if (this.valid_constraint == null || this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
			} else {
				constraints.add(this.valid_constraint);
			}

			final SpecificSemantics new_sem = this.addConstrain(
					this.instancePattern.getSemantics(), constraints);
			this.instancePattern.setSpecificSemantics(new_sem);
			return this.instancePattern;
		} else {
			return null;
		}
	}

	private void discoverDynamicEdges() throws Exception {

		for (final Pattern_window target : this.pattern.getWindows()) {
			final List<Window> target_w_matched = this.instancePattern
					.getPatternWindowMatches(target.getId());
			// if target_w_matched is empty it means the pattern_window was not
			// already found
			if (target_w_matched.size() == 0) {
				continue;
			}
			// all the dynamic edges to cover
			for (final Pattern_action_widget paw : this.pattern.getDynamicBackwardLinks(target
					.getId())) {
				final List<Action_widget> matched_aws = this.instancePattern.getAWS_for_PAW(paw
						.getId());
				// if the paw has no matches
				if (matched_aws == null || matched_aws.size() == 0) {
					continue;
				}

				for (final Action_widget aw : matched_aws) {
					final Window source_window = this.gui.getActionWidget_Window(aw.getId());

					for (final Window target_window : target_w_matched) {

						final String edge = aw.getId() + " - " + target_window.getId();
						if (this.covered_dyn_edges.contains(edge)) {
							continue;
						}
						final GUITestCase tc = this.getTestToCoverEdge(source_window,
								target_window, aw);

						final Instance_window found = this.getFoundWindow(tc, target, paw);

						if (found != null
								&& found.getInstance().getId().equals(target_window.getId())
								&& this.instancePattern.getWindows().contains(found)) {
							// the edge was covered
							this.covered_dyn_edges.add(edge);
							this.instancePattern.getGui().addEdge(aw.getId(),
									found.getInstance().getId());
						} else {
							if (found == null) {
								this.unvalid_constraints.add("not(" + this.valid_constraint + ")");
								this.valid_constraint = "";
							}
						}
					}
				}
			}
		}
	}

	private void discoverWindows() throws Exception {

		for (final Pattern_window to_discover : this.pattern.getWindows()) {
			final List<Window> target_w_matched = this.instancePattern
					.getPatternWindowMatches(to_discover.getId());
			// if target_w_matched is not empty it means the pattern_window was
			// already found
			if (target_w_matched.size() > 0) {
				continue;
			}
			// all the dynamic edges that go to the window to discover
			for (final Pattern_action_widget paw : this.pattern.getDynamicBackwardLinks(to_discover
					.getId())) {
				final List<Action_widget> matched_aws = this.instancePattern.getAWS_for_PAW(paw
						.getId());
				// if the paw has no matches
				if (matched_aws == null || matched_aws.size() == 0) {
					continue;
				}
				for (final Action_widget aw : matched_aws) {
					final Window source_window = this.gui.getActionWidget_Window(aw.getId());

					final GUITestCase tc = this
							.getTestToReachWindow(source_window, to_discover, aw);

					final Instance_window found = this.getFoundWindow(tc, to_discover, paw);

					if (found != null) {

						if (!this.instancePattern.getGui().containsWindow(
								found.getInstance().getId())
								&& !this.instancePattern.getWindows().contains(found)) {
							// the window was found
							this.instancePattern.getGui().addWindow(found.getInstance());
							this.instancePattern.getGui().addEdge(aw.getId(),
									found.getInstance().getId());
							this.instancePattern.addWindow(found);
							this.instancePattern.generateSpecificSemantics();
						}
						this.valid_constraint = this.getAdaptedConstraint(this.instancePattern
								.getSemantics());

					} else {
						this.unvalid_constraints.add(this.getAdaptedConstraint(this.instancePattern
								.getSemantics()));
						this.valid_constraint = "";
					}
				}
			}
		}
	}

	/*
	 * method that returns a constraint consistent with all the test cases seen
	 * so far Method used in the findWindow method because constraints in that
	 * case cannot be extracted from the solutions
	 */
	private String getAdaptedConstraint(final SpecificSemantics in_sem) throws Exception {

		final List<String> tcs = new ArrayList<>();
		for (final GUITestCase tc : this.observed_tcs) {
			tcs.add(this.getTCaseFact(tc));
		}

		String prop = null;
		loop: while (prop == null) {

			for (int cont = 0; cont < tcs.size(); cont++) {
				final int time_size = this.observed_tcs.get(cont).getActions().size() + 2;

				final List<String> constraints = new ArrayList<>(this.unvalid_constraints);
				constraints.add(tcs.get(cont));
				if (prop != null) {
					constraints.add(prop);
				}

				final SpecificSemantics sem = this.addConstrain(in_sem, constraints);

				final String runCom = "run {System} for " + ConfigurationManager.getAlloyRunScope()
						+ " but " + time_size + " Time";
				sem.addRun_command(runCom);
				final Module comp = AlloyUtil.compileAlloyModel(sem.toString());
				final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));
				if (sol.satisfiable()) {
					final String new_prop = AlloyUtil.extractProperty(sol, sem);
					if (prop == null) {
						prop = new_prop;
					}
				} else {
					if (prop != null) {
						this.unvalid_constraints.add("not(" + prop + ")");
					}
					prop = null;
					continue loop;
				}
			}
		}
		return prop;
	}

	protected GUITestCase getTestToReachWindow(final Window sourceWindow,
			final Pattern_window to_discover, final Action_widget aw) throws Exception {

		String property = null;
		GUITestCase tc = null;
		SpecificSemantics new_sem = null;

		this.additional_constraints = new ArrayList<>();

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
				constraints.addAll(this.additional_constraints);
			} else {
				constraints.add(this.valid_constraint);
			}

			new_sem = this.semantic4DiscoverWindow(this.instancePattern.getSemantics(),
					sourceWindow, to_discover, aw);
			final SpecificSemantics constrained = this.addConstrain(new_sem, constraints);

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(constrained);

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - discoverClasses: error generating test case.");
			}

			if (tests.size() == 0) {
				this.valid_constraint = "";
				continue;
			}

			// if valid_constraint is not null it means we are using the
			// previous constraint that it is still valid
			if (this.valid_constraint.length() > 0) {
				tc = tests.get(0);
			} else {
				// if not we need to validate the new constraint
				property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), constrained);

				final boolean valid = this.validateProperty(property, new_sem);

				if (valid) {
					tc = tests.get(0);
					this.valid_constraint = property;
				} else {
					this.valid_constraint = "";
					// add constraint
					this.additional_constraints.add("not(" + property + ")");
				}
			}
		}
		return tc;
	}

	protected GUITestCase getTestToCoverEdge(final Window sourceWindow, final Window targetWindow,
			final Action_widget aw) throws Exception {

		String property = null;
		GUITestCase tc = null;
		SpecificSemantics new_sem = null;

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
			} else {
				constraints.add(this.valid_constraint);
			}

			new_sem = this.semantic4DiscoverEdge(this.instancePattern.getSemantics(), sourceWindow,
					targetWindow, aw);
			final SpecificSemantics constrained = this.addConstrain(new_sem, constraints);

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(constrained);

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - getTestToCoverEdge: error generating test case.");
			}

			if (tests.size() == 0) {
				this.valid_constraint = "";
				continue;
			}

			// if valid_constraint is not null it means we are using the
			// previous constraint that it is still valid
			if (this.valid_constraint.length() > 0) {
				tc = tests.get(0);
			} else {
				// if not we need to validate the new constraint
				property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), new_sem);

				final boolean valid = this.validateProperty(property, constrained);

				if (valid) {
					tc = tests.get(0);
					this.valid_constraint = property;

				} else {
					this.valid_constraint = "";
					// add constraint
					this.unvalid_constraints.add("not(" + property + ")");
				}
			}
		}
		return tc;
	}

	private String getTCaseFact(final GUITestCase tc) {

		String fact = "one ";
		String operations = "";
		String times = "t0,";
		String second_part = "";
		String click = "Click =(";
		String fill = "Fill =(";

		final List<GUIAction> acts = tc.getActions().stream().filter(e -> !(e instanceof Go))
				.collect(Collectors.toList());
		for (int cont = 0; cont < acts.size(); cont++) {
			final GUIAction act = acts.get(cont);
			operations += "op" + (cont + 1);
			times += "t" + (cont + 1);
			times += ",";
			if (cont == 0) {
				second_part += "Track.op.t" + (cont + 1) + "=" + "op" + (cont + 1) + " ";

			} else {
				second_part += " and Track.op.t" + (cont + 1) + "=" + "op" + (cont + 1) + " ";
			}
			if (act instanceof Click) {
				final Click c = (Click) act;
				click += "op" + (cont + 1) + "+";
				second_part += "and op" + (cont + 1) + ".clicked=Action_widget_"
						+ c.getWidget().getId();
			}

			if (act instanceof Fill) {
				final Fill f = (Fill) act;
				fill += "op" + (cont + 1) + "+";
				second_part += "and op" + (cont + 1) + ".filled=Input_widget_"
						+ f.getWidget().getId();
				// TODO: deal with value
			}

			if (act instanceof Select) {
				// TODO:
			}

			second_part += " and t" + (cont + 2) + "=T/next[t" + (cont + 1) + "]";
			if (cont < acts.size() - 1) {
				operations += ",";
			}
		}
		second_part += " and t0=T/first";

		times += "t" + (acts.size() + 1);
		times += ":Time";
		operations += ":Operation";
		if (!click.equals("Click =(")) {
			click = click.substring(0, click.length() - 1) + ")";
			second_part += " and " + click;
		}
		if (!fill.equals("Fill =(")) {
			fill = fill.substring(0, fill.length() - 1) + ")";
			second_part += " and " + fill;
		}
		fact += operations + "| one " + times + "|" + second_part;
		fact += " and Current_window.is_in.t" + (acts.size() + 1) + "=Window_"
				+ acts.get(acts.size() - 1).getResult().getId();
		return fact;
	}

	private boolean validateProperty(final String prop, final SpecificSemantics in_sem)
			throws Exception {

		final List<String> tcs = new ArrayList<>();
		for (final GUITestCase tc : this.observed_tcs) {
			tcs.add(this.getTCaseFact(tc));
		}

		// we clone the semantics to remove all the facts related to discovering
		// windows/edges
		final List<Fact> facts = new ArrayList<>();
		for (final Fact f : in_sem.getFacts()) {
			if (!f.getIdentifier().equals("for_disocvering")) {
				facts.add(f);
			}
		}

		final SpecificSemantics new_sem = new SpecificSemantics(in_sem.getSignatures(), facts,
				in_sem.getPredicates(), in_sem.getFunctions(), in_sem.getOpenStatements());

		for (int cont = 0; cont < tcs.size(); cont++) {
			// the time size is the number of actions +2 (because of Go)
			final int time_size = this.observed_tcs.get(cont).getActions().size() + 2;
			final List<String> constraints = new ArrayList<>();
			constraints.add(tcs.get(cont));
			constraints.add(prop);

			final SpecificSemantics sem = this.addConstrain(new_sem, constraints);

			final String runCom = "run {System} for " + ConfigurationManager.getAlloyRunScope()
					+ " but " + time_size + " Time";
			sem.addRun_command(runCom);
			final Module comp = AlloyUtil.compileAlloyModel(sem.toString());
			final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));
			if (!sol.satisfiable()) {
				return false;
			}
		}
		return true;
	}

	private SpecificSemantics addConstrain(final SpecificSemantics sem, final List<String> props)
			throws Exception {

		final List<Fact> facts = new ArrayList<>(sem.getFacts());

		for (final String prop : props) {
			final Fact constraint = new Fact("sem_prop", prop);
			facts.add(constraint);
		}

		final SpecificSemantics out = new SpecificSemantics(sem.getSignatures(), facts,
				sem.getPredicates(), sem.getFunctions(), sem.getOpenStatements());
		for (final String run : sem.getRun_commands()) {
			out.addRun_command(run);
		}
		return out;
	}

	protected SpecificSemantics semantic4DiscoverWindow(final SpecificSemantics originalSemantic,
			final Window sourceWindow, final Pattern_window pattern_TargetWindow,
			final Action_widget actionWidget) throws Exception {

		// Maybe we should check the action that relates them.
		if (this.instancePattern.getWS_for_PW(pattern_TargetWindow.getId()).size() > 0) {
			throw new Exception("The pattern window to discover was already mapped.");
		}

		if (!sourceWindow.getActionWidgets().contains(actionWidget)) {
			throw new Exception("The action to exercice is not included in the source  window");
		}

		final Pattern_window pw = pattern_TargetWindow;

		final Signature parent_w_sig = AlloyUtil.searchSignatureInList(
				originalSemantic.getSignatures(), pw.getAlloyCorrespondence());

		if (parent_w_sig == null) {
			throw new Exception("Element not found: " + pw.getAlloyCorrespondence() + " at "
					+ originalSemantic.getSignatures());
		}

		// We define the windows to discover.
		final Signature sigWinToDiscover = new Signature("Undiscovered_window_"
				+ pattern_TargetWindow.getId(), Cardinality.ONE, false,
				Lists.newArrayList(parent_w_sig), false);

		// Inputs:
		final List<Pattern_input_widget> piws = pattern_TargetWindow.getInputWidgets();

		final List<Signature> iw_sig = new ArrayList<>();

		for (final Pattern_input_widget piw : piws) {

			if (piw.getCardinality().getMax() == 0) {
				continue;
			}
			final Signature piw_sig = AlloyUtil.searchForParent(originalSemantic, piw);

			if (piw_sig == null) {
				throw new Exception("Element not found: " + piw);
			}

			final Signature sigIW = new Signature("Undiscovered_inputwidget_" + piw.getId(),
					Cardinality.ONE, false, Lists.newArrayList(piw_sig), false);

			iw_sig.add(sigIW);

		}

		// Action:
		final List<Pattern_action_widget> paws = pattern_TargetWindow.getActionWidgets();

		final List<Signature> aw_sig = new ArrayList<>();

		for (final Pattern_action_widget paw : paws) {
			if (paw.getCardinality().getMax() == 0) {
				continue;
			}
			final Signature paw_sig = AlloyUtil.searchForParent(originalSemantic, paw);

			if (paw_sig == null) {
				throw new Exception("Element not found: " + paw_sig);
			}

			final Signature sigAW = new Signature("Undiscovered_actionwidget_" + paw.getId(),
					Cardinality.ONE, false, Lists.newArrayList(paw_sig), false);

			aw_sig.add(sigAW);
		}

		// We put widgets to the undiscovered window
		final Fact fact_aws_from_undiscover_window = AlloyUtil.createFactsForElement(aw_sig,
				sigWinToDiscover, "aws");

		Signature sig_action_to_execute = null;

		final Action_widget awi = actionWidget;

		final String candidateId = "Action_widget_" + awi.getId();
		final List<Signature> sigAWs = originalSemantic.getSignatures().stream()
				.filter(e -> e.getIdentifier().equals(candidateId)).collect(Collectors.toList());

		if (sigAWs.isEmpty()) {
			final List<String> signames = originalSemantic.getSignatures().stream()
					.map(e -> e.getIdentifier()).collect(Collectors.toList());

			throw new Exception("Action widget without signature: " + awi.getId() + " from "
					+ signames);
		}
		sig_action_to_execute = sigAWs.get(0);

		if (sig_action_to_execute == null) {
			throw new Exception("Signature not found for action widget: ");
		}

		final String contentFactLink = sig_action_to_execute.getIdentifier() + ".goes = "
				+ sigWinToDiscover.getIdentifier();

		final Fact factLinkActions = new Fact(sigWinToDiscover.getIdentifier() + "_awsd",
				contentFactLink);

		final String fcontent = "some t, t': Time, w: Window_w2, " + " w': "
				+ sigWinToDiscover.getIdentifier() + " , c: Click " + " | click ["
				+ (sig_action_to_execute.getIdentifier()) + ", t, T/next[t], c] and "
				+ " Current_window.is_in.t = w and Current_window.is_in.t' = w' "
				+ " and t' in T/next[t] ";
		final Fact factDiscovering = new Fact("for_discovering", fcontent);

		final String runCom = "run {System} for " + ConfigurationManager.getAlloyRunScope();

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Fact> facts = new ArrayList<>(originalSemantic.getFacts());
		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		signatures.add(sigWinToDiscover);
		signatures.addAll(aw_sig);
		signatures.addAll(iw_sig);
		facts.add(factDiscovering);
		facts.add(factLinkActions);
		facts.add(fact_aws_from_undiscover_window);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);
		semantif4DiscoverWindow.addRun_command(runCom);

		return semantif4DiscoverWindow;
	}

	private SpecificSemantics semantic4DiscoverEdge(final SpecificSemantics originalSemantic,
			final Window sourceWindow, final Window targetWindow, final Action_widget actionWidget)
					throws Exception {

		// Maybe we should check the action that relates them.
		if (!this.instancePattern.getGui().containsWindow(targetWindow.getId())) {
			throw new Exception("The pattern window to discover was not already mapped.");
		}

		if (!sourceWindow.getActionWidgets().contains(actionWidget)) {
			throw new Exception("The action to exercice is not included in the source  window");
		}

		final Signature sigWinToDiscover = AlloyUtil.searchSignatureInList(
				originalSemantic.getSignatures(), "Window_" + targetWindow.getId());

		if (sigWinToDiscover == null) {
			throw new Exception("Element not found: Window_" + targetWindow.getId() + ".");

		}

		Signature sig_action_to_execute = null;

		final Action_widget awi = actionWidget;

		final String candidateId = "Action_widget_" + awi.getId();
		final List<Signature> sigAWs = originalSemantic.getSignatures().stream()
				.filter(e -> e.getIdentifier().equals(candidateId)).collect(Collectors.toList());

		if (sigAWs.isEmpty()) {
			final List<String> signames = originalSemantic.getSignatures().stream()
					.map(e -> e.getIdentifier()).collect(Collectors.toList());

			throw new Exception("Action widget without signature: " + awi.getId() + " from "
					+ signames);
		}
		sig_action_to_execute = sigAWs.get(0);

		if (sig_action_to_execute == null) {
			throw new Exception("Signature not found for action widget: ");
		}

		final String contentFactLink = sig_action_to_execute.getIdentifier() + ".goes = "
				+ sigWinToDiscover.getIdentifier();

		final Fact factLinkActions = new Fact(sigWinToDiscover.getIdentifier() + "_awsd",
				contentFactLink);

		final String fcontent = "some t, t': Time, w: Window_w2, " + " w': "
				+ sigWinToDiscover.getIdentifier() + " , c: Click " + " | click ["
				+ (sig_action_to_execute.getIdentifier()) + ", t, T/next[t], c] and "
				+ " Current_window.is_in.t = w and Current_window.is_in.t' = w' "
				+ " and t' in T/next[t] ";
		final Fact factDiscovering = new Fact("for_discovering", fcontent);

		final String runCom = "run {System} for " + ConfigurationManager.getAlloyRunScope();

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Fact> facts = new ArrayList<>(originalSemantic.getFacts());
		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		facts.add(factDiscovering);
		facts.add(factLinkActions);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);
		semantif4DiscoverWindow.addRun_command(runCom);

		return semantif4DiscoverWindow;
	}

	private Instance_window getFoundWindow(final GUITestCase tc, final Pattern_window target,
			final Pattern_action_widget paw) throws Exception {

		// the last action widget exercised
		final Action_widget aw = (Action_widget) tc.getActions().get(tc.getActions().size() - 1)
				.getWidget();

		Window reached_w = this.wasTestCasePreviouslyExecuted(tc);

		if (reached_w == null) {
			final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime());
			runner.runTestCase(tc);
			final GuiStateManager gmanager = GuiStateManager.getInstance();
			gmanager.readGUI();
			if (gmanager.getCurrentWindows().size() == 0) {
				return null;
			}
			reached_w = gmanager.getCurrentWindows().get(0);
		}

		Window previoulsy_found = null;
		for (final Window w : this.gui.getWindows()) {
			if (w.isSame(reached_w)) {
				previoulsy_found = w;
				this.gui.addEdge(aw.getId(), w.getId());
				break;
			}
		}

		if (previoulsy_found == null) {
			// the window is new
			tc.getActions().get(tc.getActions().size() - 1).setResult(reached_w);
			this.observed_tcs.add(tc);
			this.ripper.ripWindow(tc.getActions(), reached_w);
			ApplicationHelper.getInstance().closeApplication();

			this.gui.addWindow(reached_w);
			this.gui.addEdge(aw.getId(), reached_w.getId());
			List<Instance_window> instances = target.getMatches(reached_w);
			if (instances.size() != 0) {
				// the first is returned because it the one that maps more
				// elements
				return instances.get(0);
			}
			for (final Pattern_window pw : this.pattern.getDynamicForwardLinks(paw.getId())) {
				instances = pw.getMatches(reached_w);
				if (instances.size() != 0) {
					return instances.get(0);
				}

			}
			return null;
		} else {
			// the window was found before
			tc.getActions().get(tc.getActions().size() - 1).setResult(previoulsy_found);
			this.observed_tcs.add(tc);
			ApplicationHelper.getInstance().closeApplication();
			this.gui.addEdge(aw.getId(), previoulsy_found.getId());
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (iw.getPattern().getId().equals(target.getId())
						&& iw.getInstance().getId().equals(previoulsy_found.getId())) {
					return iw;
				}
			}
			final List<String> pws = this.pattern.getDynamicForwardLinks(paw.getId()).stream()
					.map(e -> e.getId()).collect(Collectors.toList());
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (pws.contains(iw.getPattern().getId())
						&& iw.getInstance().getId().equals(previoulsy_found.getId())) {
					return iw;
				}
			}

			return null;
		}
	}

	private Window wasTestCasePreviouslyExecuted(final GUITestCase tc) {

		mainloop: for (final GUITestCase tc2 : this.observed_tcs) {
			if (tc2.getActions().size() != tc.getActions().size()) {
				continue;
			}
			for (int cont = 0; cont < tc.getActions().size(); cont++) {
				// TODO: remove after dealing with go actions
				final GUIAction act1 = tc.getActions().get(cont);
				final GUIAction act2 = tc2.getActions().get(cont);
				if (act1 instanceof Go && act2 instanceof Go) {
					continue;
				}

				if (!act1.getWidget().getId().equals(act2.getWidget().getId())
						|| !act1.getWindow().getId().equals(act2.getWindow().getId())) {
					continue mainloop;
				}

				if (act1 instanceof Click) {
					if (!(act2 instanceof Click)) {
						continue mainloop;
					}
				}
				if (act1 instanceof Fill) {
					if (!(act2 instanceof Fill)) {
						continue mainloop;
					}
					final Fill fill1 = (Fill) act1;
					final Fill fill2 = (Fill) act2;
					if (!fill1.getInput().equals(fill2.getInput())) {
						continue mainloop;
					}

				}
				if (act1 instanceof Select) {
					if (!(act2 instanceof Select)) {
						continue mainloop;
					}
					final Select select1 = (Select) act1;
					final Select select2 = (Select) act2;
					if (select1.getIndex() != select2.getIndex()) {
						continue mainloop;
					}
				}

			}
			return tc2.getActions().get(tc2.getActions().size() - 1).getResult();
		}

	return null;
	}
}
