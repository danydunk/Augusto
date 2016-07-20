package usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;

import com.google.common.collect.Lists;

public class GUIFunctionality_refine {

	private final GUI gui;
	private final Instance_GUI_pattern instancePattern;
	private SpecificSemantics constrained;
	private SpecificSemantics un_constrained;
	private final GUI_Pattern pattern;
	private final List<GUITestCase> observed_tcs;
	private final Ripper ripper;

	public GUIFunctionality_refine(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.gui = gui;
		this.instancePattern = instancePattern.clone();
		this.constrained = null;
		this.un_constrained = this.instancePattern.getSemantics();
		this.pattern = this.instancePattern.getGuipattern();
		this.observed_tcs = new ArrayList<>();
		this.ripper = new Ripper(ConfigurationManager.getSleepTime(), this.gui);
	}

	public Instance_GUI_pattern refine() throws Exception {

		final SpecificSemantics old_constrained_s = this.constrained;
		do {
			this.discoverWindows();
			this.discoverDynamicEdges();
		} while (old_constrained_s != this.constrained);
		if (this.pattern.isInstance(this.instancePattern)) {
			this.instancePattern.setSpecificSemantics(this.constrained);
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
			// all the dynamic edges that go to the window to discover
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

						if (this.instancePattern.getGui().isEdge(aw.getId(), target_window.getId())) {
							continue;
						}
						final GUITestCase tc = this.getTestToCoverEdge(source_window,
								target_window, aw);

						final Instance_window found = this.getFoundWindow(tc, target);
						final String constrain = AlloyUtil.extractProperty(tc.getAlloySolution(),
								this.instancePattern.getSemantics());

						if (found != null
								&& found.getInstance().getId().equals(target_window.getId())
								&& this.instancePattern.getWindows().contains(found)) {
							// the window was found
							this.instancePattern.getGui().addEdge(aw.getId(),
									found.getInstance().getId());
						} else {
							this.un_constrained = null;
							this.constrained = this
									.addConstrain(this.constrained, constrain, false);
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

					final Instance_window found = this.getFoundWindow(tc, to_discover);
					final String constrain = AlloyUtil.extractProperty(tc.getAlloySolution(),
							this.instancePattern.getSemantics());

					if (found != null
							&& !this.instancePattern.getGui().containsWindow(
									found.getInstance().getId())
							&& !this.instancePattern.getWindows().contains(found)) {
						// the window was found
						this.instancePattern.getGui().addWindow(found.getInstance());
						this.instancePattern.getGui().addEdge(aw.getId(),
								found.getInstance().getId());
						this.instancePattern.addWindow(found);
					} else {
						this.un_constrained = null;
						this.constrained = this.addConstrain(this.constrained, constrain, false);
					}
				}
			}
		}
	}

	protected GUITestCase getTestToReachWindow(final Window sourceWindow,
			final Pattern_window to_discover, final Action_widget aw) throws Exception {

		String property = null;
		GUITestCase tc = null;
		SpecificSemantics new_sem = null;

		while (tc == null) {
			if (this.constrained == null) {
				new_sem = this.semantic4DiscoverWindow(this.un_constrained, sourceWindow,
						to_discover, aw);
			} else {
				new_sem = this.semantic4DiscoverWindow(this.constrained, sourceWindow, to_discover,
						aw);
			}

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(new_sem);

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - discoverClasses: error generating test case.");
			}

			if (tests.size() == 0) {
				continue;
			}

			property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), new_sem);

			final boolean valid = this.validateProperty(property);

			if (valid) {
				tc = tests.get(0);
				if (this.constrained == null) {
					this.constrained = this.addConstrain(this.un_constrained, property, true);
				}

			} else {
				this.constrained = null;
				// add constraint
				this.un_constrained = this.addConstrain(this.un_constrained, property, false);
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
			if (this.constrained == null) {
				new_sem = this.semantic4DiscoverEdge(this.un_constrained, sourceWindow,
						targetWindow, aw);
			} else {
				new_sem = this.semantic4DiscoverEdge(this.constrained, sourceWindow, targetWindow,
						aw);
			}

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(new_sem);

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - discoverClasses: error generating test case.");
			}

			if (tests.size() == 0) {
				continue;
			}

			property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), new_sem);

			final boolean valid = this.validateProperty(property);

			if (valid) {
				tc = tests.get(0);
				if (this.constrained == null) {
					this.constrained = this.addConstrain(this.un_constrained, property, true);
				}

			} else {
				this.constrained = null;
				// add constraint
				this.un_constrained = this.addConstrain(this.un_constrained, property, false);
			}
		}
		return tc;
	}

	private boolean validateProperty(final String prop) {

		// add code
		return true;
	}

	private SpecificSemantics addConstrain(final SpecificSemantics sem, final String prop,
			final boolean valid) throws Exception {

		Fact constraint = null;
		if (valid) {
			constraint = new Fact("sem_prop", prop);
		} else {
			constraint = new Fact("sem_prop", "not (" + prop + ")");

		}

		final List<Fact> facts = new ArrayList<>(sem.getFacts());
		facts.add(constraint);
		final SpecificSemantics out = new SpecificSemantics(sem.getSignatures(), facts,
				sem.getPredicates(), sem.getFunctions(), sem.getOpenStatements());
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
		final Signature sigWinToDiscover = new Signature("Window_" + pattern_TargetWindow.getId()
				+ "_undiscovered", Cardinality.ONE, false, Lists.newArrayList(parent_w_sig), false);

		// Inputs:
		final List<Pattern_input_widget> piws = pattern_TargetWindow.getInputWidgets();

		final List<Signature> iw_sig = new ArrayList<>();

		for (final Pattern_input_widget piw : piws) {

			final Signature piw_sig = AlloyUtil.searchForParent(originalSemantic, piw);

			if (piw_sig == null) {
				throw new Exception("Element not found: " + piw);
			}

			final Signature sigIW = new Signature("Input_widget_" + piw.getId(), Cardinality.ONE,
					false, Lists.newArrayList(piw_sig), false);

			iw_sig.add(sigIW);

		}

		// Action:
		final List<Pattern_action_widget> paws = pattern_TargetWindow.getActionWidgets();

		final List<Signature> aw_sig = new ArrayList<>();

		for (final Pattern_action_widget paw : paws) {

			final Signature paw_sig = AlloyUtil.searchForParent(originalSemantic, paw);

			if (paw_sig == null) {
				throw new Exception("Element not found: " + paw_sig);
			}

			final Signature sigAW = new Signature("Action_widget_" + paw.getId(), Cardinality.ONE,
					false, Lists.newArrayList(paw_sig), false);

			aw_sig.add(sigAW);
		}

		// We put widgets to the undiscover window
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
		final Fact factDiscovering = new Fact("", fcontent);

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
		final Fact factDiscovering = new Fact("", fcontent);

		final String runCom = "run {System} for 4";

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

	private Instance_window getFoundWindow(final GUITestCase tc, final Pattern_window target)
			throws Exception {

		// the last action widget exercised
		final Action_widget aw = (Action_widget) tc.getActions().get(tc.getActions().size() - 1)
				.getWidget();
		final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime());
		runner.runTestCase(tc);
		final GuiStateManager gmanager = GuiStateManager.getInstance();
		gmanager.readGUI();
		if (gmanager.getCurrentWindows().size() == 0) {
			return null;
		}
		final Window reached_w = gmanager.getCurrentWindows().get(0);

		tc.getActions().get(tc.getActions().size() - 1).setResult(reached_w);
		this.observed_tcs.add(tc);

		Window previoulsy_found = null;
		for (final Window w : this.gui.getWindows()) {
			if (w.isSame(reached_w)) {
				previoulsy_found = w;
				this.gui.addEdge(aw.getId(), w.getId());
			}
		}

		if (previoulsy_found == null) {
			// the window is new
			// it gets ripped
			this.ripper.ripWindow(tc.getActions(), reached_w);

			this.gui.addWindow(reached_w);
			this.gui.addEdge(aw.getId(), reached_w.getId());
			final List<Instance_window> instances = target.getMatches(reached_w);
			if (instances.size() == 0) {
				return null;
			}
			// the first is returned because it the one that maps more elements
			return instances.get(0);

		} else {
			// the window was found before
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (iw.getPattern() == target && iw.getInstance().isSame(previoulsy_found)) {
					return iw;
				}
			}
			return null;
		}
	}
}
