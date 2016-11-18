package src.usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.gui.Ripper;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.Cardinality;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_widget;
import src.usi.pattern.structure.Pattern_window;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.semantic.alloy.Alloy_Model;
import src.usi.semantic.alloy.structure.Fact;
import src.usi.semantic.alloy.structure.Signature;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.OracleChecker;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUIFunctionality_refine {

	private final GUI gui;
	private Instance_GUI_pattern instancePattern;
	private String current_semantic_property;
	private final List<String> discarded_semantic_properties;
	private List<String> canididate_semantic_properties;
	private final GUI_Pattern pattern;
	private List<GUITestCaseResult> observed_tcs;
	private final List<String> covered_dyn_edges;
	private List<String> unsat_commands;
	private final long beginTime = System.currentTimeMillis();

	public GUIFunctionality_refine(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.gui = gui;
		this.instancePattern = instancePattern.clone();

		this.pattern = this.instancePattern.getGuipattern();
		this.observed_tcs = new ArrayList<>();
		this.covered_dyn_edges = new ArrayList<>();
		this.current_semantic_property = "";
		// this.current_semantic_property =
		// "one Field_3:Property_required|Property_required = (Field_3) and Field_3.associated_to = (Input_widget_iw62)";
		// this.current_semantic_property =
		// "one Field_2:Property_unique|one Field_3,Field_4:Property_required|Property_required = (Field_3+Field_4) and Property_unique = (Field_2+Field_4) and Field_3.associated_to = (Input_widget_iw89) and Field_4.associated_to = (Input_widget_iw86) and Field_2.associated_to = (Input_widget_iw87)";
		// this.current_semantic_property = "";
		this.discarded_semantic_properties = new ArrayList<>();
		this.unsat_commands = new ArrayList<>();
	}

	public Instance_GUI_pattern refine() throws Exception {

		String old_current_semantic_property;
		Instance_GUI_pattern old_instancePattern;
		this.instancePattern.generateSpecificSemantics();
		do {
			// we save information to use to decide whether to terminate
			old_current_semantic_property = this.current_semantic_property;
			old_instancePattern = this.instancePattern.clone();

			this.discoverDynamicEdges();
			this.discoverWindows();
			// if we reached timeout we exit
			if ((System.currentTimeMillis() - this.beginTime) >= ConfigurationManager
					.getRefinementTimeout()) {
				System.out.println("TIMEOUT IN STRUCTURAL REFINEMENT");
				break;
			}
			// if something has changed we iterate again
		} while (!old_current_semantic_property.equals(this.current_semantic_property)
				|| this.anyChanges(old_instancePattern));

		// we filter out the aw that don't have the correct forward edges
		this.filterAWS();

		if (this.pattern.isInstance(this.instancePattern)) {
			System.out.println("INSTANCE FOUND!");
			System.out.println("INITIAL CONSTRAINT FOUND: " + this.current_semantic_property);

			this.instancePattern.generateSpecificSemantics();

			this.semanticPropertyRefine();

			System.out.println("FINAL CONSTRAINT FOUND: " + this.current_semantic_property);

			final List<String> constraints = new ArrayList<>();
			if (this.current_semantic_property == null
					|| this.current_semantic_property.length() == 0) {
				constraints.addAll(this.discarded_semantic_properties);
			} else {
				constraints.add(this.current_semantic_property);
			}
			final SpecificSemantics new_sem = addSemanticConstrain_to_Model(
					this.instancePattern.getSemantics(), constraints);
			this.instancePattern.setSpecificSemantics(new_sem);
			return this.instancePattern;
		} else {
			System.out.println("INSTANCE NOT FOUND!");
			return null;
		}
	}

	private boolean anyChanges(final Instance_GUI_pattern old_instance) throws Exception {

		if (this.instancePattern.getWindows().size() != old_instance.getWindows().size()) {
			return true;
		}
		if (this.instancePattern.getGui().getNumberOfDynamicEdges() != old_instance.getGui()
				.getNumberOfDynamicEdges()) {
			return true;
		}
		if (this.instancePattern.getGui().getNumberOfStaticEdges() != old_instance.getGui()
				.getNumberOfStaticEdges()) {
			return true;
		}
		for (int x = 0; x < this.instancePattern.getWindows().size(); x++) {
			final Instance_window iw = this.instancePattern.getWindows().get(x);
			final Instance_window iw_old = old_instance.getWindows().get(x);
			for (final Pattern_action_widget paw : iw.getPattern().getActionWidgets()) {
				if (iw.getAWS_for_PAW(paw.getId()).size() != iw_old.getAWS_for_PAW(paw.getId())
						.size()) {
					return true;
				}
			}
		}
		return false;
	}

	private void discoverDynamicEdges() throws Exception {

		mainloop: for (final Pattern_window target : this.pattern.getWindows()) {
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

					for (final Window target_window : target_w_matched) {

						// if we reached timeout
						if ((System.currentTimeMillis() - this.beginTime) >= ConfigurationManager
								.getRefinementTimeout()) {
							System.out.println("TIMEOUT IN DISCOVER EDGES");
							break mainloop;
						}

						final String edge = aw.getId() + " - " + target_window.getId();
						System.out.println("DISCOVER DYNAMIC EDGE: looking for edge " + edge
								+ " (from " + paw.getId() + ").");

						if (this.covered_dyn_edges.contains(edge)) {
							System.out.println("DISCOVER DYNAMIC EDGE: edge already found before.");
							continue;
						}
						// the semantics is updated
						final Instance_GUI_pattern clone = this.instancePattern.clone();
						clone.getGui().addDynamicEdge(aw.getId(), target_window.getId());
						clone.generateSpecificSemantics();
						final String run_command = "run{System and (some t: Time" + ", c: Click "
								+ " | click [" + "Action_widget_" + aw.getId()
								+ ", t, T/next[t], c] and "
								+ "Current_window.is_in.(T/next[t]) = Window_"
								+ target_window.getId() + " and click_semantics[Action_widget_"
								+ aw.getId() + ",t])}";

						if (this.unsat_commands.contains(run_command)) {
							System.out
							.println("DISCOVER DYNAMIC EDGE: this run command was previusly observed as unsat.");
							continue;

						}

						clone.getSemantics().addRun_command(run_command);

						// we generate the testcase
						final GUITestCase tc = this.getTestCase(clone.getSemantics());
						this.run_and_update(tc, target);
					}
				}
			}
		}
	}

	private boolean run_and_update(final GUITestCase tc, final Pattern_window target)
			throws Exception {

		if (tc == null) {
			System.out.println("TESTCASE NOT FOUND.");
			return false;
		}

		String aw = null;
		if (tc.getActions().get(tc.getActions().size() - 1) instanceof Click) {
			aw = tc.getActions().get(tc.getActions().size() - 1).getWidget().getId();
		}

		final Instance_window found = this.getFoundWindow(tc, target);

		boolean new_window = false;
		boolean new_edge = false;

		if (found != null) {
			System.out.println("FOUND MATCHING WINDOW");
			String edge = null;
			final Window source_window = tc.getActions().get(tc.getActions().size() - 1)
					.getWindow();

			// an edge is covered if the window reached is not the same as the
			// source OR if it was a target window
			boolean covered_edge = !found.getInstance().getId().equals(source_window.getId());
			if (aw != null) {

				final Pattern_action_widget paw = this.instancePattern.getPAW_for_AW(aw);

				covered_edge = covered_edge
						| this.pattern.isDyanamicEdge(paw.getId(), found.getPattern().getId());
			}

			final Instance_GUI_pattern old = this.instancePattern.clone();

			if (covered_edge) {
				// if we did not stay in the same window
				if (!this.instancePattern.getGui().containsWindow(found.getInstance().getId())) {
					// new window was found
					this.instancePattern.getGui().addWindow(found.getInstance());

					// we add the found static edges to the instance gui
					// TODO: deal with the fact that the ripping might find new
					// windows connected by static edges that are part of the
					// pattern
					for (final Window w : this.gui.getWindows()) {
						for (final Action_widget aww : w.getActionWidgets()) {
							for (final Window targetw : this.gui.getStaticForwardLinks(aww.getId())) {
								if (this.instancePattern.getGui().containsWindow(w.getId())
										&& this.instancePattern.getGui().containsWindow(
												targetw.getId())) {
									this.instancePattern.getGui().addStaticEdge(aww.getId(),
											targetw.getId());
								}
							}
						}
					}

				}

				if (!this.instancePattern.getWindows().contains(found)) {
					this.instancePattern.addWindow(found);
					new_window = true;
				}
				if (aw != null) {
					this.instancePattern.getGui().addDynamicEdge(aw, found.getInstance().getId());
					edge = aw + " - " + found.getInstance().getId();
					new_edge = true;
				}
				this.instancePattern.generateSpecificSemantics();

				if (!this.instancePattern.isSemanticsValid()) {
					// System.out.println(this.instancePattern.getSemantics());
					System.out.println("SEMANTICS NOT VALID");
					this.instancePattern = old;
					this.cleanInstance(aw);

					return false;
				}
			}

			if (found.getPattern().getId().equals(target.getId())) {
				// we found the correct window
				final List<String> sem = this.canididate_semantic_properties.stream()
						.filter(e -> !e.startsWith("not(")).collect(Collectors.toList());
				assert sem.size() <= 1;

				if (sem.size() == 1) {
					if (this.current_semantic_property.length() > 0) {
						this.discarded_semantic_properties.add("not("
								+ this.current_semantic_property + ")");
					}
					this.current_semantic_property = sem.get(0);
					this.canididate_semantic_properties.remove(this.current_semantic_property);
				}
				this.discarded_semantic_properties.addAll(this.canididate_semantic_properties);

				if (new_edge) {
					this.covered_dyn_edges.add(edge);
				}
				// we reset the unsat commands since the semantics is modified
				if (new_edge || new_window) {
					this.unsat_commands = new ArrayList<>();
				}
				return true;
			} else {
				System.out.println("MATCHING WINDOW IS NOT THE EXPECTED ONE.");

				final List<String> vsem = this.canididate_semantic_properties.stream()
						.filter(e -> {
							if (!e.startsWith("not(")) {
								return true;
							}
							return false;
						}).collect(Collectors.toList());
				assert vsem.size() < 2;

				String prop = (vsem.size() == 1) ? vsem.get(0) : this.current_semantic_property;
				if (!this.validateProperty(prop, this.instancePattern.getSemantics(),
						this.observed_tcs)) {
					System.out.println("ADAPTING SEMANTIC PROPERTY");
					// System.out.println(this.instancePattern.getSemantics());

					final String new_prop = this.getAdaptedConstraint(this.instancePattern
							.getSemantics());
					if (new_prop == null) {
						this.instancePattern = old;
						this.cleanInstance(aw);
						System.out.println("ADAPTATION IMPOSSIBLE.");
						return false;
					} else {
						prop = new_prop;
					}
				}

				this.canididate_semantic_properties.remove(prop);

				final List<String> sem = this.canididate_semantic_properties.stream().map(e -> {
					if (e.startsWith("not(")) {
						return e;
					} else {
						return "not(" + e + ")";
					}
				}).collect(Collectors.toList());

				this.discarded_semantic_properties.addAll(sem);
				if (this.current_semantic_property.length() > 0
						&& !this.current_semantic_property.equals(prop)) {
					this.discarded_semantic_properties.add("not(" + this.current_semantic_property
							+ ")");
				}
				this.current_semantic_property = prop;
				if (new_edge) {
					this.covered_dyn_edges.add(edge);
				}

				if (new_edge || new_window) {
					// we reset the unsat commands since the semantics is
					// modified
					this.unsat_commands = new ArrayList<>();
				}

			}

		} else {
			System.out.println("MATCHING WINDOW NOT FOUND.");
			// we remove the edge
			this.cleanInstance(aw);
		}
		return false;
	}

	private void cleanInstance(final String aw) throws Exception {

		System.out.println("CLEANING INSTANCE");

		if (aw != null) {
			System.out.println("REMOVING " + aw + " MAPPING");

			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (iw.getPAW_for_AW(aw) != null) {
					iw.removeAW_mapping(iw.getPAW_for_AW(aw).getId(), aw);
				}
			}

			List<Window> ws = this.instancePattern.getGui().getDynamicForwardLinks(aw);
			for (final Window ww : ws) {
				this.instancePattern.getGui().removeDynamicEdge(aw, ww.getId());
			}
			ws = this.instancePattern.getGui().getStaticForwardLinks(aw);
			for (final Window ww : ws) {
				this.instancePattern.getGui().removeStaticEdge(aw, ww.getId());
			}
			this.instancePattern.generateSpecificSemantics();

			final List<GUITestCaseResult> new_tcs = new ArrayList<>();
			for (final GUITestCaseResult tcr : this.observed_tcs) {
				if (!tcr.getTc().getActions().get(tcr.getTc().getActions().size() - 1).getWidget()
						.getId().equals(aw)) {
					new_tcs.add(tcr);
				}
			}
			this.observed_tcs = new_tcs;
		}

		final List<GUITestCaseResult> new_tcs = new ArrayList<>();
		for (final GUITestCaseResult tcr : this.observed_tcs) {
			final String last_w_id = tcr.getResults().get(tcr.getResults().size() - 1).getId();
			if (this.instancePattern.getGui().containsWindow(last_w_id)) {
				new_tcs.add(tcr);
			}
		}
		this.observed_tcs = new_tcs;
	}

	private void discoverWindows() throws Exception {

		mainloop: for (final Pattern_window to_discover : this.pattern.getWindows()) {
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

					// if we reached timeout
					if ((System.currentTimeMillis() - this.beginTime) >= ConfigurationManager
							.getRefinementTimeout()) {
						System.out.println("TIMEOUT IN DISCOVER WINDOWS");
						break mainloop;
					}

					System.out.println("DISCOVER DYNAMIC WINDOW: looking for "
							+ to_discover.getId() + " from " + aw.getId() + ".");

					final Instance_GUI_pattern clone = this.createConcreteWindowFromPattern(
							to_discover, aw.getId());
					final String run_command = "run {System and (some t: Time" + ", c: Click "
							+ " | click [Action_widget_" + aw.getId() + ", t, T/next[t], c] and "
							+ "Current_window.is_in.(T/next[t]) = Window_" + to_discover.getId()
							+ " and click_semantics[Action_widget_" + (aw.getId()) + ",t])}";
					if (this.unsat_commands.contains(run_command)) {
						System.out
								.println("DISCOVER DYNAMIC WINDOW: this run command was previusly observed as unsat.");
						continue;

					}

					clone.getSemantics().addRun_command(run_command);

					final GUITestCase tc = this.getTestCase(clone.getSemantics());

					if (this.run_and_update(tc, to_discover)) {
						continue mainloop;
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

		assert this.observed_tcs.size() > 0;
		System.out.println("GET ADAPTED CONSTRAINT: start.");
		while (true) {

			// we pop a random testcase from the observed ones
			// final Random ran = new Random();
			// final int index = ran.nextInt(this.observed_tcs.size());
			// final GUITestCaseResult res = this.observed_tcs.get(index);

			List<GUITestCaseResult> tcs = new ArrayList<>();
			final List<String> constraints = new ArrayList<>(this.discarded_semantic_properties);
			Alloy_Model sem = null;

			if (this.observed_tcs.size() > 0) {
				// we use the last inserted testcase
				final GUITestCaseResult res = this.observed_tcs.get(this.observed_tcs.size() - 1);
				tcs = this.observed_tcs.stream().filter(e -> !e.equals(res))
						.collect(Collectors.toList());

				try {
					sem = AlloyUtil.getTCaseModel(in_sem, res.getTc().getActions(), res
							.getResults().get(res.getResults().size() - 1));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			} else {
				sem = SpecificSemantics.instantiate(in_sem);
				sem.clearRunCommands();
				sem.addRun_command("run {System}");
			}

			final SpecificSemantics new_sem = addSemanticConstrain_to_Model(sem, constraints);

			final Module comp = AlloyUtil.compileAlloyModel(new_sem.toString());
			final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));

			if (sol.satisfiable()) {
				final String new_prop = AlloyUtil.extractProperty(sol, new_sem);
				if (this.validateProperty(new_prop, in_sem, tcs)) {
					System.out.println("GET ADAPTED CONSTRAINT: found new constraint: " + new_prop);
					return new_prop;
				} else {
					this.discarded_semantic_properties.add("not(" + new_prop + ")");
					continue;
				}
			} else {
				System.out.println("GET ADAPTED CONSTRAINT: null end.");
				return null;
			}
		}
	}

	private static SpecificSemantics addSemanticConstrain_to_Model(final Alloy_Model sem,
			final List<String> props) throws Exception {

		final List<Fact> facts = sem.getFacts().stream()
				.filter(e -> (!e.getIdentifier().equals("semantic_property")))
				.collect(Collectors.toList());

		for (final String prop : props) {
			final Fact constraint = new Fact("semantic_property", prop);
			facts.add(constraint);
		}

		final SpecificSemantics out = new SpecificSemantics(sem.getSignatures(), facts,
				sem.getPredicates(), sem.getFunctions(), sem.getOpenStatements());
		for (final String run : sem.getRun_commands()) {
			out.addRun_command(run);
		}
		return out;
	}

	private Instance_window getFoundWindow(GUITestCase tc, final Pattern_window target)
			throws Exception {

		System.out.println("GET FOUND WINDOW: start.");
		// the last action widget exercised
		final Widget wid = tc.getActions().get(tc.getActions().size() - 1).getWidget();
		Pattern_widget pwid = null;
		Pattern_window pw = null;

		for (final Instance_window iw : this.instancePattern.getWindows()) {
			if (iw.getPAW_for_AW(wid.getId()) != null) {
				pwid = iw.getPAW_for_AW(wid.getId());
				pw = iw.getPattern();
			}
		}

		Window reached_w = null;
		final GUITestCaseResult prev_res = this.wasTestCasePreviouslyExecuted(tc);
		if (prev_res != null) {
			reached_w = prev_res.getResults().get(prev_res.getActions_executed().size() - 1);
		} else {
			final TestCaseRunner runner = new TestCaseRunner(this.gui);
			GUITestCaseResult res = null;

			res = runner.runTestCase(tc);
			// res = this.last_used_instance.updateTCResult(res);
			// the window reached after the last action was executed
			reached_w = res.getResults().get(res.getActions_executed().size() - 1);

			if (this.gui.getWindow(reached_w.getId()) == null) {
				// the window is new, we add it and rip it
				this.gui.addWindow(reached_w);
				final List<GUIAction> action_executed = res.getActions_actually_executed();

				final Ripper ripper = new Ripper(this.gui);
				ripper.ripWindow(action_executed, reached_w);
				ApplicationHelper.getInstance().closeApplication();
			}
			if (wid instanceof Action_widget) {
				this.gui.addDynamicEdge(wid.getId(), reached_w.getId());
			}

			// we dont need the result (it wastes too much memory)
			final GUITestCase new_tc = new GUITestCase(null, res.getTc().getActions(), res.getTc()
					.getRunCommand());
			final GUITestCaseResult new_res = new GUITestCaseResult(new_tc,
					res.getActions_executed(), res.getResults(), res.getActions_actually_executed());
			this.observed_tcs.add(new_res);
			tc = new_tc;
		}

		final Window previus = tc.getActions().get(tc.getActions().size() - 1).getWindow();
		if (previus.getId().equals(reached_w.getId())) {
			// we stayed in the same window
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (iw.getPattern().getId().equals(pw.getId())
						&& iw.getInstance().getId().equals(previus.getId())) {
					System.out.println("GET FOUND WINDOW: end.");
					return iw;
				}
			}
		}

		// we check whether a match with the target was found already
		for (final Instance_window iw : this.instancePattern.getWindows()) {
			if (iw.getPattern().getId().equals(target.getId())
					&& iw.getInstance().getId().equals(reached_w.getId())) {
				System.out.println("GET FOUND WINDOW: end.");
				return iw;
			}
		}

		if (wid instanceof Action_widget) {
			// we check whether there was match with another valid dynamic edge
			final List<String> pws = this.pattern.getDynamicForwardLinks(pwid.getId()).stream()
					.map(e -> e.getId()).collect(Collectors.toList());
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (pws.contains(iw.getPattern().getId())
						&& iw.getInstance().getId().equals(reached_w.getId())) {
					System.out.println("GET FOUND WINDOW: end.");
					return iw;
				}
			}
		}
		// we compute new matches with the pattern
		List<Instance_window> instances = target.getMatches(reached_w);
		if (instances.size() != 0) {
			// the first is returned because it the one that maps more
			// elements
			System.out.println("GET FOUND WINDOW: end.");
			return instances.get(0);
		}
		if (wid instanceof Action_widget) {
			// we compute new matches with the other valid edges
			for (final Pattern_window ppw : this.pattern.getDynamicForwardLinks(pwid.getId())) {
				instances = ppw.getMatches(reached_w);
				if (instances.size() != 0) {
					System.out.println("GET FOUND WINDOW: end.");
					return instances.get(0);
				}
			}
		}
		System.out.println("GET FOUND WINDOW: null end.");
		return null;
	}

	private GUITestCaseResult wasTestCasePreviouslyExecuted(final GUITestCase tc) {

		for (final GUITestCaseResult tc2 : this.observed_tcs) {
			if (tc.isSame(tc2.getTc())) {
				return tc2;
			}
		}

		return null;
	}

	private void semanticPropertyRefine() throws Exception {

		int size = -1;
		final long beginTime = System.currentTimeMillis();

		final OracleChecker oracle = new OracleChecker(this.gui);

		final String runCmd = "run {"
				+ "System and "
				+ "(all t: Time| (t = T/last) <=> ((Track.op.t in Click and not click_semantics[Track.op.t.clicked, T/prev[t]]) or (Track.op.t in Fill and not fill_semantics[Track.op.t.filled, T/prev[t], Track.op.t.with]) or (Track.op.t in Select and not select_semantics[Track.op.t.wid, T/prev[t], Track.op.t.selected])))}";

		List<String> true_constraints = new ArrayList<>();
		true_constraints.add(this.current_semantic_property);

		final Instance_GUI_pattern clone_with = this.instancePattern.clone();
		SpecificSemantics sem_with = addSemanticConstrain_to_Model(
				this.instancePattern.getSemantics(), true_constraints);

		// we add a fact to remove redundant actions
		final List<Fact> facts = sem_with.getFacts();
		final Fact new_fact = new Fact(
				"filter_redundant_actions",
				"all t: Time | not (Track.op.t in Select and Track.op.(T/next[t]) in Select and Track.op.t.wid = Track.op.(T/next[t]).wid)"
						+ System.lineSeparator()
						+ "all t: Time | not (Track.op.t in Fill and Track.op.(T/next[t]) in Fill and Track.op.t.filled = Track.op.(T/next[t]).filled)");
		facts.add(new_fact);
		sem_with = new SpecificSemantics(sem_with.getSignatures(), facts, sem_with.getPredicates(),
				sem_with.getFunctions(), sem_with.getOpenStatements());

		mainloop: while (size <= ConfigurationManager.getRefinementAlloyTimeScope()) {

			if ((System.currentTimeMillis() - beginTime) >= ConfigurationManager
					.getRefinementTimeout()) {
				System.out.println("SEMANTIC REFINEMENT TIMEOUT");
				break;
			}
			System.out.println("CURRENT SEMANTIC PROPERTY: " + this.current_semantic_property);
			sem_with.clearRunCommands();

			List<GUITestCase> tests = null;
			if (size == -1) {
				sem_with.addRun_command(runCmd);
				clone_with.setSpecificSemantics(sem_with);
				final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone_with);
				tests = test_gen.generateMinimalTestCases(ConfigurationManager
						.getRefinementAlloyTimeScope());
			} else {
				sem_with.addRun_command(runCmd + " for " + ConfigurationManager.getAlloyRunScope()
						+ " but " + size + " Time");

				clone_with.setSpecificSemantics(sem_with);
				final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone_with);
				tests = test_gen.generateTestCases();
			}
			sem_with.clearRunCommands();

			assert tests.size() < 2;

			if (tests.size() == 0) {
				size = -1;

				System.out.println("PROPERTY MAYBE OVERSEMPLIFIED");

				this.discarded_semantic_properties.add("not(" + this.current_semantic_property
						+ ")");
				final String new_prop = this.getAdaptedConstraint(this.instancePattern
						.getSemantics());
				if (new_prop == null) {
					System.out
					.println("SEMANTIC PROPERTY REFINE: no more possible semantic properties to be found. CORRECT ONE FOUND!");
					this.discarded_semantic_properties.remove(this.current_semantic_property);
					break mainloop;
				}
				System.out.println("NEW SEMANTIC PROPERTY: " + new_prop);
				this.current_semantic_property = new_prop;
				true_constraints = new ArrayList<>();
				true_constraints.add(this.current_semantic_property);
				sem_with = addSemanticConstrain_to_Model(sem_with, true_constraints);
				continue mainloop;
			}

			final GUITestCase tc = tests.get(0);

			GUITestCaseResult res = this.wasTestCasePreviouslyExecuted(tc);
			if (res == null) {
				final TestCaseRunner runner = new TestCaseRunner(this.gui);
				res = runner.runTestCase(tc);

				// we dont need the result (it wastes too much memory)
				final GUITestCase new_tc = new GUITestCase(null, res.getTc().getActions(), res
						.getTc().getRunCommand());
				final GUITestCaseResult new_res = new GUITestCaseResult(new_tc,
						res.getActions_executed(), res.getResults(),
						res.getActions_actually_executed());
				res = new_res;

			} else {
				System.out.println("TESTCASE ALREADY RUN!!!");
			}
			sem_with = SpecificSemantics.instantiate(AlloyUtil.getTCaseModelOpposite(sem_with, res
					.getTc().getActions()));

			final GUITestCaseResult res2 = clone_with.updateTCResult(res);
			if (res2 != null) {
				res = res2;
			}
			this.observed_tcs.add(res);

			if (oracle.check(res, true)) {
				// the beahviour was the same
				System.out.println("SAME BEAHVIOUR");
				size = res.getTc().getActions().size() + 2;
			} else {
				System.out.println("DIFFERENT BEAHVIOUR");
				size = -1;
				this.discarded_semantic_properties.add("not(" + this.current_semantic_property
						+ ")");

				final String new_prop = this.getAdaptedConstraint(this.instancePattern
						.getSemantics());
				if (new_prop == null) {
					System.out
					.println("SEMANTIC PROPERTY REFINE: INCONSISTENCY. SEMANTIC PROPERTY NOT FOUND!");
					this.current_semantic_property = "";
					return;
				}
				this.current_semantic_property = new_prop;
				true_constraints = new ArrayList<>();
				true_constraints.add(this.current_semantic_property);
				sem_with = addSemanticConstrain_to_Model(sem_with, true_constraints);
			}
		}
		System.out.println("SEMANTIC PROPERTY REFINE: end.");
	}

	private boolean validateProperty(final String prop, final SpecificSemantics in_sem,
			final List<GUITestCaseResult> tcs) throws Exception {

		System.out.println("VALIDATE PROPERTY: start.");

		// we clone the semantics to remove all the facts related to discovering
		// windows/edges
		final List<Fact> facts = new ArrayList<>();
		for (final Fact f : in_sem.getFacts()) {
			if (!f.getIdentifier().equals("testcase")) {
				facts.add(f);
			}
		}
		final Alloy_Model mod_filtered = new Alloy_Model(in_sem.getSignatures(), facts,
				in_sem.getPredicates(), in_sem.getFunctions(), in_sem.getOpenStatements());
		SpecificSemantics sem_filtered = SpecificSemantics.instantiate(mod_filtered);
		final List<String> constraints = new ArrayList<>();
		// constraints.add(tcs.get(cont));
		constraints.add(prop);
		sem_filtered = addSemanticConstrain_to_Model(sem_filtered, constraints);

		// we divide the testcases to check in batches
		int batchn = 0;
		final int batch_size = ConfigurationManager.getMultithreadingBatchSize();
		while ((batchn * batch_size) < tcs.size()) {
			final List<GUITestCaseResult> batch = new ArrayList<>();
			for (int x = 0; x < batch_size && ((batchn * batch_size) + x) < tcs.size(); x++) {
				batch.add(tcs.get(((batchn * batch_size) + x)));
			}
			final List<Run_command_thread> threads = new ArrayList<>();
			for (int cont = 0; cont < batch.size(); cont++) {

				final Alloy_Model sem = AlloyUtil.getTCaseModel(sem_filtered, batch.get(cont)
						.getTc().getActions(),
						batch.get(cont).getResults().get(batch.get(cont).getResults().size() - 1));

				final Module comp = AlloyUtil.compileAlloyModel(sem.toString());
				final Run_command_thread run = new Run_command_thread(comp, comp.getAllCommands()
						.get(0));
				run.start();
				threads.add(run);
			}
			boolean alive = true;

			while (alive) {
				alive = false;
				for (final Run_command_thread run : threads) {
					if (run.isAlive()) {
						alive = true;
						continue;
					} else {
						if (run.exception) {
							for (final Run_command_thread run2 : threads) {
								run2.interrupt();
							}
							throw new Exception(
									"GUIFunctionality_refine - validateProperty: error.");
						}
						final A4Solution sol = run.solution;
						if (!sol.satisfiable()) {
							for (final Run_command_thread run2 : threads) {
								run2.interrupt();
							}
							System.out.println("VALIDATE PROPERTY: -false- end.");
							return false;
						} else {

						}
					}

				}
			}
			batchn++;
		}
		System.out.println("VALIDATE PROPERTY: -true- end.");
		return true;
	}

	protected GUITestCase getTestCase(final SpecificSemantics sem) throws Exception {

		// System.out.println("START ALLOY MODEL");
		// System.out.println(sem);
		// System.out.println("END ALLOY MODEL");

		String property = null;
		GUITestCase tc = null;

		final List<String> new_invalid_properties = new ArrayList<>();
		String current = this.current_semantic_property;
		this.canididate_semantic_properties = new ArrayList<>();

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (current.length() == 0) {
				constraints.addAll(this.discarded_semantic_properties);
				constraints.addAll(new_invalid_properties);
			} else {
				constraints.add(current);
			}

			final SpecificSemantics constrained = addSemanticConstrain_to_Model(sem, constraints);
			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(constrained);
			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases(ConfigurationManager
					.getRefinementAlloyTimeScope());

			assert tests.size() < 2;

			if (tests.size() == 0) {
				if (current.length() > 0) {
					new_invalid_properties.add("not(" + current + ")");
					current = "";
					continue;
				} else {
					this.canididate_semantic_properties.addAll(new_invalid_properties);
					// we save the fact that this run command was unsat
					this.unsat_commands.add(constrained.getRun_commands().get(0));
					return null;
				}
			}

			if (tests.get(0) == null) {
				return null;
			}

			// if valid_constraint is not null it means we are using the
			// previous constraint that it is still valid
			if (current.length() > 0) {
				tc = tests.get(0);
				if (!current.equals(this.current_semantic_property)) {
					this.canididate_semantic_properties.add(current);
				}
			} else {
				// if not we need to validate the new constraint
				property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), sem);

				final boolean valid = this.validateProperty(property, sem, this.observed_tcs);

				if (valid) {
					tc = tests.get(0);
					System.out.println("GET TESTCASE: new valid property - " + property);
					current = property;
					this.canididate_semantic_properties.add(current);

				} else {
					// since it not valid according to previous testcases we add
					// it to the list of discarded properties
					this.discarded_semantic_properties.add("not(" + property + ")");
					current = "";
					// add constraint
					System.out.println("GET TESTCASE: new invalid property added - not(" + property
							+ ")");
				}
			}
		}
		return tc;
	}

	private Instance_GUI_pattern createConcreteWindowFromPattern(final Pattern_window pw,
			final String aw_id) throws Exception {

		final Window new_wind = new Window(pw.getId(), "", "", 1, 1, false);
		final Instance_window inst = new Instance_window(pw, new_wind);
		final List<Widget> widgets = new ArrayList<>();
		for (final Pattern_action_widget paw : pw.getActionWidgets()) {
			final Action_widget aw = new Action_widget(paw.getId(), "", "", 1, 1);
			widgets.add(aw);
			new_wind.addWidget(aw);
			final List<Action_widget> aws = new ArrayList<>();
			aws.add(aw);
			inst.addAW_mapping(paw, aws);
		}
		for (final Pattern_input_widget piw : pw.getInputWidgets()) {
			final Input_widget iw = new Input_widget(piw.getId(), "", "", 1, 1, "");
			widgets.add(iw);
			new_wind.addWidget(iw);
			final List<Input_widget> iws = new ArrayList<>();
			iws.add(iw);
			inst.addIW_mapping(piw, iws);

		}
		for (final Pattern_selectable_widget psw : pw.getSelectableWidgets()) {
			final Selectable_widget sw = new Selectable_widget(psw.getId(), "", "", 1, 1, 0, -1);
			widgets.add(sw);
			new_wind.addWidget(sw);
			final List<Selectable_widget> sws = new ArrayList<>();
			sws.add(sw);
			inst.addSW_mapping(psw, sws);
		}

		final Instance_GUI_pattern clone = this.instancePattern.clone();
		clone.getGui().addWindow(new_wind);
		if (aw_id != null) {
			clone.getGui().addDynamicEdge(aw_id, new_wind.getId());
		}
		clone.addWindow(inst);
		clone.generateSpecificSemantics();
		SpecificSemantics sem = clone.getSemantics();
		final List<Signature> sigs = new ArrayList<>();
		for (final Signature sig : sem.getSignatures()) {
			if (sig.getIdentifier().contains("_piw") || sig.getIdentifier().contains("_psw")
					|| sig.getIdentifier().contains("_paw")) {
				final Signature s = new Signature(sig.getIdentifier(), Cardinality.SET,
						sig.isAbstract_(), sig.getParent(), sig.isSubset());
				sigs.add(s);
			} else {
				sigs.add(sig);
			}
		}

		final List<Fact> facts = new ArrayList<>();
		for (final Fact fact : sem.getFacts()) {
			if (fact.getIdentifier().startsWith("Window_" + new_wind.getId())) {
				final String[] lines = fact.getContent().split("\\r?\\n");
				final Fact new_fact = new Fact(fact.getIdentifier(), lines[0]);
				facts.add(new_fact);
			} else {
				facts.add(fact);
			}
		}
		sem = new SpecificSemantics(sigs, facts, sem.getPredicates(), sem.getFunctions(),
				sem.getOpenStatements());
		clone.setSpecificSemantics(sem);
		return clone;
	}

	private void filterAWS() throws Exception {

		for (final Instance_window inw : this.instancePattern.getWindows()) {
			for (final Pattern_action_widget paw : inw.getPattern().getActionWidgets()) {
				for (final Action_widget aw : inw.getAWS_for_PAW(paw.getId())) {
					if (this.instancePattern.getGuipattern().getDynamicForwardLinks(paw.getId())
							.size() > 0) {
						if (this.instancePattern.getGui().getDynamicForwardLinks(aw.getId()).size() == 0) {
							inw.removeAW_mapping(paw.getId(), aw.getId());
						}
					}
					if (this.instancePattern.getGuipattern().getStaticForwardLinks(paw.getId())
							.size() > 0) {
						if (this.instancePattern.getGui().getStaticForwardLinks(aw.getId()).size() == 0) {
							inw.removeAW_mapping(paw.getId(), aw.getId());
						}
					}
				}
			}
		}
	}

	final class Run_command_thread extends Thread {

		private A4Solution solution;
		private boolean exception = false;
		private final Module model;
		private final Command run_command;

		public Run_command_thread(final Module model, final Command run_command) {

			this.model = model;
			this.run_command = run_command;
		}

		public boolean hasException() {

			return this.exception;
		}

		public A4Solution getSolution() {

			return this.solution;
		}

		@Override
		public void run() {

			try {
				this.solution = AlloyUtil.runCommand(this.model, this.run_command);
			} catch (final InterruptedException e) {

			} catch (final Exception e) {
				// e.printStackTrace();
				this.exception = true;
			}
		}
	}

}
