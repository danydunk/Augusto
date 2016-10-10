package usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_selectable_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.ripping.Ripper;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.GUITestCaseResult;
import usi.gui.semantic.testcase.OracleChecker;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Widget;
import usi.gui.structure.Window;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUIFunctionality_refine {

	private final GUI gui;
	private Instance_GUI_pattern instancePattern;
	private String semantic_property;
	private List<String> unvalid_semantic_properties;
	private final List<String> new_unvalid_semantic_properties;

	// additional list of unvalid constraints that has to be adapted during the
	// window search
	// private List<String> additional_constraints;
	private final GUI_Pattern pattern;
	private final List<GUITestCaseResult> observed_tcs;
	private final List<String> covered_dyn_edges;

	public GUIFunctionality_refine(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.gui = gui;
		this.instancePattern = instancePattern.clone();

		this.pattern = this.instancePattern.getGuipattern();
		this.observed_tcs = new ArrayList<>();
		this.covered_dyn_edges = new ArrayList<>();
		this.semantic_property = "";
		this.unvalid_semantic_properties = new ArrayList<>();
		this.new_unvalid_semantic_properties = new ArrayList<>();
	}

	public Instance_GUI_pattern refine() throws Exception {

		String old_valid_constraints;
		int old_unvalid_constraints_size;
		int old_windows_number;
		int old_edges_number;
		do {
			old_valid_constraints = this.semantic_property;
			old_unvalid_constraints_size = this.unvalid_semantic_properties.size();
			old_windows_number = this.instancePattern.getWindows().size();
			old_edges_number = this.instancePattern.getGui().getNumberOfStaticEdges()
					+ this.instancePattern.getGui().getNumberOfDynamicEdges();

			this.discoverDynamicEdges();
			this.discoverWindows();
			this.discoverWindows_special();
			// if something has changed we iterate again
		} while (!old_valid_constraints.equals(this.semantic_property)
				|| old_unvalid_constraints_size != this.unvalid_semantic_properties.size()
				|| old_windows_number != this.instancePattern.getWindows().size()
				|| old_edges_number != (this.instancePattern.getGui().getNumberOfStaticEdges() + this.instancePattern
						.getGui().getNumberOfDynamicEdges()));
		System.out.println("INITIAL CONSTRAINT FOUND: " + this.semantic_property);

		// we filter out the aw that don't have the correct forward edges
		this.filterAWS();

		if (this.pattern.isInstance(this.instancePattern)) {
			System.out.println("PATTERN IS INSTANCE");
			this.instancePattern.generateSpecificSemantics();
			// System.out.println(this.instancePattern.getSemantics());
			// Thread.sleep(100000);
			this.semanticPropertyRefine();
			if (this.semantic_property == null || this.semantic_property.length() == 0) {
				throw new Exception(
						"GUIFunctionality_refine - refine: error refining semantic property.");
			}
			System.out.println("FINAL CONSTRAINT FOUND: " + this.semantic_property);

			final List<String> constraints = new ArrayList<>();
			if (this.semantic_property == null || this.semantic_property.length() == 0) {
				constraints.addAll(this.unvalid_semantic_properties);
			} else {
				constraints.add(this.semantic_property);
			}
			final SpecificSemantics new_sem = addSemanticConstrain_to_Model(
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

					for (final Window target_window : target_w_matched) {

						String edge = aw.getId() + " - " + target_window.getId();
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
						final String run_command = "run{System and (some t: Time, w: Window_"
								+ target_window.getId() + ", c: Click " + " | click ["
								+ "Action_widget_" + aw.getId() + ", t, T/next[t], c] and "
								+ "Current_window.is_in.(T/next[t]) = w "
								+ " and click_semantics[Action_widget_" + aw.getId() + ",t])}";
						clone.getSemantics().addRun_command(run_command);
						// we generate the testcase
						final GUITestCase tc = this.getTestCase(clone.getSemantics());

						if (tc == null) {
							System.out.println("DISCOVER DYNAMIC EDGE: edge not found.");
							continue;
						}

						final Instance_window found = this.getFoundWindow(tc, target, paw);
						final String old_valid = this.semantic_property;
						if (found != null) {
							final Instance_GUI_pattern old = this.instancePattern.clone();

							if (!this.instancePattern.getGui().containsWindow(
									found.getInstance().getId())) {
								// new window was found
								this.instancePattern.getGui().addWindow(found.getInstance());

								// we add the found static edges to the instance
								// gui
								// TODO: deal with the fact that the ripping
								// might
								// find new windows
								// connected by static edges that are part of
								// the
								// pattern
								for (final Window w : this.gui.getWindows()) {
									for (final Action_widget aww : w.getActionWidgets()) {
										for (final Window targetw : this.gui
												.getStaticForwardLinks(aww.getId())) {
											if (this.instancePattern.getGui().containsWindow(
													w.getId())
													&& this.instancePattern.getGui()
															.containsWindow(targetw.getId())) {
												this.instancePattern.getGui().addStaticEdge(
														aww.getId(), targetw.getId());
											}
										}
									}
								}

							}

							System.out.println("DISCOVER DYNAMIC EDGE: edge found.");
							if (!this.instancePattern.getWindows().contains(found)) {
								this.instancePattern.addWindow(found);
							}
							edge = aw.getId() + " - " + found.getInstance().getId();
							this.covered_dyn_edges.add(edge);
							this.instancePattern.getGui().addDynamicEdge(aw.getId(),
									found.getInstance().getId());
							this.instancePattern.generateSpecificSemantics();
							if (!this.instancePattern.isSemanticsValid()) {
								System.out
										.println("DISCOVER DYNAMIC EDGE: semantics not valid, edge not found.");
								this.instancePattern = old;
								this.semantic_property = "";
							} else {
								this.covered_dyn_edges.add(edge);
								this.unvalid_semantic_properties
										.addAll(this.new_unvalid_semantic_properties);
							}
						} else {
							System.out.println("DISCOVER DYNAMIC EDGE: edge not found.");
							// this.unvalid_constraints.add("not(" +
							// this.valid_constraint + ")");
							if (this.semantic_property.length() == 0) {
								this.semantic_property = old_valid;
							} else {
								this.semantic_property = "";
							}
						}
					}
				}
			}
		}
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

					System.out.println("DISCOVER DYNAMIC WINDOW: looking for "
							+ to_discover.getId() + " from " + aw.getId() + ".");

					final Instance_GUI_pattern clone = this.createConcreteWindowFromPattern(
							to_discover, aw.getId());
					final String run_command = "run {System and (some t: Time, w: Window_"
							+ to_discover.getId() + " , c: Click " + " | click [Action_widget_"
							+ aw.getId() + ", t, T/next[t], c] and "
							+ "Current_window.is_in.(T/next[t]) = w "
							+ "and click_semantics[Action_widget_" + (aw.getId()) + ",t])}";
					clone.getSemantics().addRun_command(run_command);

					final GUITestCase tc = this.getTestCase(clone.getSemantics());

					if (tc == null) {
						System.out.println("DISCOVER DYNAMIC WINDOW: test case not found.");
						continue;
					}

					final Instance_window found = this.getFoundWindow(tc, to_discover, paw);
					final String old_valid = this.semantic_property;

					if (found != null) {
						final Instance_GUI_pattern old = this.instancePattern.clone();

						if (!this.instancePattern.getGui().containsWindow(
								found.getInstance().getId())) {
							// new window was found
							this.instancePattern.getGui().addWindow(found.getInstance());

							// we add the found static edges to the instance gui
							// TODO: deal with the fact that the ripping might
							// find new windows
							// connected by static edges that are part of the
							// pattern
							for (final Window w : this.gui.getWindows()) {
								for (final Action_widget aww : w.getActionWidgets()) {
									for (final Window targetw : this.gui.getStaticForwardLinks(aww
											.getId())) {
										if (this.instancePattern.getGui().containsWindow(w.getId())
												&& this.instancePattern.getGui().containsWindow(
														targetw.getId())) {
											this.instancePattern.getGui().addStaticEdge(
													aww.getId(), targetw.getId());
										}
									}
								}
							}

						}
						if (!this.instancePattern.getWindows().contains(found)) {

							this.instancePattern.addWindow(found);
							System.out.println("DISCOVER DYNAMIC WINDOW: window found.");
						}
						this.instancePattern.getGui().addDynamicEdge(aw.getId(),
								found.getInstance().getId());
						this.instancePattern.generateSpecificSemantics();
						final String edge = aw.getId() + " - " + found.getInstance().getId();

						if (!this.instancePattern.isSemanticsValid()) {
							System.out
									.println("DISCOVER WINDOW: semantics not valid, window not found.");
							this.instancePattern = old;
							this.semantic_property = "";
						} else {
							this.covered_dyn_edges.add(edge);
							this.semantic_property = this.getAdaptedConstraint(this.instancePattern
									.getSemantics());
							this.unvalid_semantic_properties
									.addAll(this.new_unvalid_semantic_properties);
						}

						continue mainloop;
					} else {
						System.out.println("DISCOVER DYNAMIC WINDOW: window not found.");
						// this.unvalid_constraints.add(this.getAdaptedConstraint(this.instancePattern
						// .getSemantics()));
						if (this.semantic_property.length() == 0) {
							this.semantic_property = old_valid;
						} else {
							this.semantic_property = "";
						}
					}
				}
			}
		}
	}

	private void discoverWindows_special() throws Exception {

		mainloop: for (final Pattern_window to_discover : this.pattern.getWindows()) {
			final List<Window> target_w_matched = this.instancePattern
					.getPatternWindowMatches(to_discover.getId());
			// if target_w_matched is not empty it means the pattern_window was
			// already found
			if (target_w_matched.size() > 0) {
				continue;
			}

			System.out.println("DISCOVER DYNAMIC WINDOW: looking for " + to_discover.getId());

			final Instance_GUI_pattern clone = this.createConcreteWindowFromPattern(to_discover,
					null);
			final String run_command = "run{System and(some t: Time, w: Window_"
					+ to_discover.getId() + " | Current_window.is_in.t = w)}";
			clone.getSemantics().addRun_command(run_command);

			final GUITestCase tc = this.getTestCase(clone.getSemantics());

			if (tc == null) {
				System.out.println("DISCOVER DYNAMIC WINDOW: test case not found.");
				continue;
			}

			final Instance_window found = this.getFoundWindow(tc, to_discover, null);
			final String old_valid = this.semantic_property;

			if (found != null) {
				final Instance_GUI_pattern old = this.instancePattern.clone();

				if (!this.instancePattern.getGui().containsWindow(found.getInstance().getId())) {
					// new window was found
					this.instancePattern.getGui().addWindow(found.getInstance());

					// we add the found static edges to the instance gui
					// TODO: deal with the fact that the ripping might
					// find new windows
					// connected by static edges that are part of the
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
					System.out.println("DISCOVER DYNAMIC WINDOW: window found.");
				}

				this.instancePattern.generateSpecificSemantics();

				if (!this.instancePattern.isSemanticsValid()) {
					System.out.println("DISCOVER WINDOW: semantics not valid, window not found.");
					this.instancePattern = old;
					this.semantic_property = "";
				} else {
					this.semantic_property = this.getAdaptedConstraint(this.instancePattern
							.getSemantics());
					this.unvalid_semantic_properties.addAll(this.new_unvalid_semantic_properties);
				}

				continue mainloop;
			} else {
				System.out.println("DISCOVER DYNAMIC WINDOW: window not found.");
				// this.unvalid_constraints.add(this.getAdaptedConstraint(this.instancePattern
				// .getSemantics()));
				if (this.semantic_property.length() == 0) {
					this.semantic_property = old_valid;
				} else {
					this.semantic_property = "";
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

		//
		// final List<String> tcs = new ArrayList<>();
		// for (final GUITestCaseResult tcr : this.observed_tcs) {
		// tcs.add(this.getTCaseFact(tcr));
		// }
		System.out.println("GET ADAPTED CONSTRAINT: start.");

		String prop = null;
		loop: while (prop == null) {

			for (int cont = 0; cont < this.observed_tcs.size(); cont++) {

				final List<String> constraints = new ArrayList<>(this.unvalid_semantic_properties);
				// constraints.add(tcs.get(cont));
				if (prop != null) {
					constraints.add(prop);
				}

				final Alloy_Model sem = AlloyUtil
						.getTCaseModel(in_sem, this.observed_tcs.get(cont));
				final SpecificSemantics new_sem = addSemanticConstrain_to_Model(sem, constraints);

				final Module comp = AlloyUtil.compileAlloyModel(new_sem.toString());
				final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));
				if (sol.satisfiable()) {
					final String new_prop = AlloyUtil.extractProperty(sol, new_sem);
					if (prop == null) {
						prop = new_prop;
					}
				} else {
					if (prop != null) {
						this.unvalid_semantic_properties.add("not(" + prop + ")");
					}
					prop = null;
					continue loop;
				}
			}
		}
		System.out.println("GET ADAPTED CONSTRAINT: end.");
		return prop;
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

	private Instance_window getFoundWindow(final GUITestCase tc, final Pattern_window target,
			final Pattern_action_widget paw) throws Exception {

		System.out.println("GET FOUND WINDOW: start.");
		// the last action widget exercised
		Action_widget aw = null;
		if (tc.getActions().get(tc.getActions().size() - 1) instanceof Click) {
			aw = (Action_widget) tc.getActions().get(tc.getActions().size() - 1).getWidget();
		}

		Window reached_w = null;
		final GUITestCaseResult prev_res = this.wasTestCasePreviouslyExecuted(tc);
		if (prev_res != null) {
			reached_w = prev_res.getResults().get(prev_res.getActions_executed().size() - 1);
		}
		// if the test case was run already the window was found already
		Window previoulsy_found = reached_w;
		if (reached_w == null) {
			final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime(),
					this.gui);
			GUITestCaseResult res = null;
			try {
				res = runner.runTestCase(tc);
			} catch (final Exception e) {
				System.out
						.println("GET FOUND WINDOW: test case was not able to run correctly, returning null. "
								+ e.getMessage());
				e.printStackTrace();
				return null;
			}

			if (res.getActions_executed().size() != tc.getActions().size()
					|| res.getResults().get(tc.getActions().size() - 1) == null) {
				return null;
			}
			// the window reached after the last action was executed
			reached_w = res.getResults().get(tc.getActions().size() - 1);

			previoulsy_found = null;
			for (final Window w : this.gui.getWindows()) {
				if (w.isSame(reached_w)) {
					previoulsy_found = w;
					this.gui.addDynamicEdge(aw.getId(), w.getId());
					break;
				}
			}
			if (previoulsy_found != null) {
				final List<Window> results = res.getResults();
				results.remove(res.getActions_executed().size() - 1);
				results.add(res.getActions_executed().size() - 1, previoulsy_found);
				res.setResults(results);
			}

			// we dont need the result
			final GUITestCase new_tc = new GUITestCase(null, res.getTc().getActions(), res.getTc()
					.getRunCommand());
			final GUITestCaseResult new_res = new GUITestCaseResult(new_tc,
					res.getActions_executed(), res.getResults(), res.getActions_actually_executed());
			this.observed_tcs.add(new_res);
		}
		if (previoulsy_found == null) {
			// the window is new
			// we take the actions executed to reach the window(for ripping)
			// we take the last element of the list because it is the for sure
			// the last inserted
			this.gui.addWindow(reached_w);
			if (aw != null) {
				this.gui.addDynamicEdge(aw.getId(), reached_w.getId());
			}
			final List<GUIAction> action_executed = this.observed_tcs.get(
					this.observed_tcs.size() - 1).getActions_actually_executed();

			final Ripper ripper = new Ripper(ConfigurationManager.getSleepTime(), this.gui, null);
			ripper.ripWindow(action_executed, reached_w);
			ApplicationHelper.getInstance().closeApplication();

			List<Instance_window> instances = target.getMatches(reached_w);
			if (instances.size() != 0) {
				// the first is returned because it the one that maps more
				// elements
				System.out.println("GET FOUND WINDOW: end.");
				return instances.get(0);
			}
			if (paw != null) {
				for (final Pattern_window pw : this.pattern.getDynamicForwardLinks(paw.getId())) {
					instances = pw.getMatches(reached_w);
					if (instances.size() != 0) {
						System.out.println("GET FOUND WINDOW: end.");
						return instances.get(0);
					}
				}
			}
			System.out.println("GET FOUND WINDOW: null end.");
			return null;
		} else {
			// the window was found before
			if (aw != null) {
				this.gui.addDynamicEdge(aw.getId(), previoulsy_found.getId());
			}
			for (final Instance_window iw : this.instancePattern.getWindows()) {
				if (iw.getPattern().getId().equals(target.getId())
						&& iw.getInstance().getId().equals(previoulsy_found.getId())) {
					System.out.println("GET FOUND WINDOW: end.");
					return iw;
				}
			}
			if (paw != null) {
				final List<String> pws = this.pattern.getDynamicForwardLinks(paw.getId()).stream()
						.map(e -> e.getId()).collect(Collectors.toList());
				for (final Instance_window iw : this.instancePattern.getWindows()) {
					if (pws.contains(iw.getPattern().getId())
							&& iw.getInstance().getId().equals(previoulsy_found.getId())) {
						System.out.println("GET FOUND WINDOW: end.");
						return iw;
					}
				}
			}
			System.out.println("GET FOUND WINDOW: null end.");
			return null;
		}
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

		final long beginTime = System.currentTimeMillis();

		final OracleChecker oracle = new OracleChecker(this.gui);

		final String runCmd = "run {"
				+ "System and "
				+ "(some aw:Action_widget, iw:Input_widget, sw:Selectable_widget, w: Window| one opp:Operation| Track.op.(T/last) = opp and ((opp in Go and opp.where=w and not go_semantics[w, T/prev[T/last]]) or (opp in Click and opp.clicked = aw and not click_semantics[aw, T/prev[T/last]]) or (opp in Fill and opp.filled=iw and not fill_semantics[iw, T/prev[T/last], opp.with])  or (opp in Select and opp.wid=sw and not select_semantics[sw, T/prev[T/last], opp.selected])))}";

		List<String> true_constraints = new ArrayList<>();
		true_constraints.add(this.semantic_property);

		final Instance_GUI_pattern clone_with = this.instancePattern.clone();
		SpecificSemantics sem_with = addSemanticConstrain_to_Model(
				this.instancePattern.getSemantics(), true_constraints);

		mainloop: while ((System.currentTimeMillis() - beginTime) < ConfigurationManager
				.getSemanticRefinementTimeout()) {
			System.out.println("CURRENT SEMANTIC PROPERTY: " + this.semantic_property);
			sem_with.clearRunCommands();
			sem_with.addRun_command(runCmd);
			// System.out.println(sem_with);
			clone_with.setSpecificSemantics(sem_with);
			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone_with);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();
			sem_with.clearRunCommands();

			if (tests.size() == 0) {
				System.out.println("PROPERTY MAYBE OVERSEMPLIFIED");

				final List<String> false_constraints = new ArrayList<>();
				false_constraints.addAll(this.unvalid_semantic_properties);
				false_constraints.add("not(" + this.semantic_property + ")");
				final Random r = new Random();

				while (true) {
					sem_with.clearRunCommands();
					// we randomly pick one of the executed test cases
					final int index = r.nextInt(this.observed_tcs.size());
					sem_with = addSemanticConstrain_to_Model(sem_with, false_constraints);
					sem_with = SpecificSemantics.instantiate(AlloyUtil.getTCaseModel(sem_with,
							this.observed_tcs.get(index)));

					// we try to generate a test
					final Module comp = AlloyUtil.compileAlloyModel(sem_with.toString());
					final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));

					if (!sol.satisfiable()) {
						System.out
								.println("SEMANTIC PROPERTY REFINE: no more possible semantic properties to be found. CORRECT ONE FOUND!");
						break mainloop;
					}

					final String new_prop = AlloyUtil.extractProperty(sol, sem_with);
					System.out.println("VALIDATING PROPERTY: " + new_prop);

					if (!this.validateProperty(new_prop, sem_with)) {
						false_constraints.add("not(" + new_prop + ")");
						this.unvalid_semantic_properties.add("not(" + new_prop + ")");

					} else {
						// this.unvalid_constraints.add("not(" +
						// this.valid_constraint + ")");
						this.semantic_property = new_prop;
						true_constraints = new ArrayList<>();
						true_constraints.add(this.semantic_property);

						sem_with = addSemanticConstrain_to_Model(sem_with, true_constraints);
						continue mainloop;
					}
				}
			}
			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - semanticPropertyRefine: impossible to generate test cases.");
			}

			final GUITestCase tc = tests.get(0);

			GUITestCaseResult res = this.wasTestCasePreviouslyExecuted(tc);
			if (res == null) {
				final TestCaseRunner runner = new TestCaseRunner(
						ConfigurationManager.getSleepTime(), this.gui);
				res = runner.runTestCase(tc);

			} else {
				System.out.println("TESTCASE ALREADY RUN!!!");
			}

			switch (oracle.check(res)) {
			case 1:
				// the beahviour was the same
				System.out.println("SAME BEAHVIOUR");
				sem_with = SpecificSemantics.instantiate(AlloyUtil.getTCaseModelOpposite(sem_with,
						tc));
				break;
			case -1:
				System.out.println("DIFFERENT BEAHVIOUR");
				// System.out.println(oracle.getDescriptionOfLastOracleCheck());
				// System.out.println();

				this.unvalid_semantic_properties.add("not(" + this.semantic_property + ")");

				final SpecificSemantics sem_without = SpecificSemantics.instantiate(AlloyUtil
						.getTCaseModel(this.instancePattern.getSemantics(), res));

				String new_prop = null;
				while (new_prop == null) {
					final SpecificSemantics new_sem = addSemanticConstrain_to_Model(sem_without,
							this.unvalid_semantic_properties);

					final Module comp = AlloyUtil.compileAlloyModel(new_sem.toString());
					final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));

					if (sol.satisfiable()) {
						new_prop = AlloyUtil.extractProperty(sol, new_sem);
						System.out.println("VALIDATING PROPERTY: " + new_prop);
						if (!this.validateProperty(new_prop, sem_without)) {
							this.unvalid_semantic_properties.add("not(" + new_prop + ")");
							new_prop = null;
						}

					} else {
						System.out
								.println("SEMANTIC PROPERTY REFINE: INCONSISTENCY. SEMANTIC PROPERTY NOT FOUND!");
						this.semantic_property = "";
						return;
					}
				}
				this.semantic_property = new_prop;
				true_constraints = new ArrayList<>();
				true_constraints.add(this.semantic_property);

				sem_with = addSemanticConstrain_to_Model(sem_with, true_constraints);
				break;
			case 0:
				throw new Exception(
						"GUIFunctionality_refine - semanticPropertyRefine: it was not possible to run the whole testcase.");

			}
			this.observed_tcs.add(res);
		}
		System.out.println("SEMANTIC PROPERTY REFINE: end.");
	}

	private boolean validateProperty(final String prop, final SpecificSemantics in_sem)
			throws Exception {

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
				} catch (final Exception e) {
					// e.printStackTrace();
					this.exception = true;
				}
			}
		}

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

		final List<Run_command_thread> threads = new ArrayList<>();

		for (int cont = 0; cont < this.observed_tcs.size(); cont++) {
			// the time size is the number of actions +2 (because of Go)

			final Alloy_Model sem = AlloyUtil.getTCaseModel(sem_filtered,
					this.observed_tcs.get(cont));

			// System.out.println("start validate sem");
			// System.out.println(sem);
			// System.out.println("end validate sem");

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
						throw new Exception("GUIFunctionality_refine - validateProperty: error.");
					}
					final A4Solution sol = run.solution;
					if (!sol.satisfiable()) {
						for (final Run_command_thread run2 : threads) {
							run2.interrupt();
						}
						System.out.println("VALIDATE PROPERTY: -false- end.");
						return false;
					}
				}

			}
		}
		System.out.println("VALIDATE PROPERTY: -true- end.");
		return true;
	}

	protected GUITestCase getTestCase(final SpecificSemantics sem) throws Exception {

		String property = null;
		GUITestCase tc = null;

		final String old_valid_constraint = this.semantic_property;
		final List<String> old_invalid_constraint = new ArrayList<>(
				this.unvalid_semantic_properties);

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.semantic_property.length() == 0) {
				constraints.addAll(this.unvalid_semantic_properties);
				constraints.addAll(this.new_unvalid_semantic_properties);
			} else {
				constraints.add(this.semantic_property);
			}

			final SpecificSemantics constrained = addSemanticConstrain_to_Model(sem, constraints);

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(constrained);

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - getTestCase: error generating test case.");
			}

			if (tests.size() == 0) {
				if (this.semantic_property.length() > 0) {
					this.new_unvalid_semantic_properties.add("not(" + this.semantic_property + ")");
					this.semantic_property = "";
					continue;
				} else {
					this.semantic_property = old_valid_constraint;
					this.unvalid_semantic_properties = old_invalid_constraint;
					return null;
				}
			}

			// if valid_constraint is not null it means we are using the
			// previous constraint that it is still valid
			if (this.semantic_property.length() > 0) {
				tc = tests.get(0);
			} else {
				// if not we need to validate the new constraint
				property = AlloyUtil.extractProperty(tests.get(0).getAlloySolution(), sem);

				final boolean valid = this.validateProperty(property, sem);

				if (valid) {
					tc = tests.get(0);
					System.out.println("GET TEST TO COVER EDGE: new valid property - " + property);
					this.semantic_property = property;

				} else {
					this.semantic_property = "";
					// add constraint
					System.out.println("GET TEST TO COVER EDGE: new invalid property added - not("
							+ property + ")");
					this.new_unvalid_semantic_properties.add("not(" + property + ")");
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
		sem = new SpecificSemantics(sigs, sem.getFacts(), sem.getPredicates(), sem.getFunctions(),
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
}
