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
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
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
import usi.gui.structure.Window;

import com.google.common.collect.Lists;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUIFunctionality_refine {

	private final GUI gui;
	private Instance_GUI_pattern instancePattern;
	private List<String> unvalid_constraints;
	private List<String> new_unvalid_constraints;

	// additional list of unvalid constraints that has to be adapted during the
	// window search
	// private List<String> additional_constraints;
	private String valid_constraint;
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
			old_edges_number = this.instancePattern.getGui().getNumberOfStaticEdges()
					+ this.instancePattern.getGui().getNumberOfDynamicEdges();

			this.discoverDynamicEdges();
			this.discoverWindows();
			this.discoverWindows_special();
			// if something has changed we iterate again
		} while (!old_valid_constraints.equals(this.valid_constraint)
				|| old_unvalid_constraints_size != this.unvalid_constraints.size()
				|| old_windows_number != this.instancePattern.getWindows().size()
				|| old_edges_number != (this.instancePattern.getGui().getNumberOfStaticEdges() + this.instancePattern
						.getGui().getNumberOfDynamicEdges()));
		System.out.println("INITIAL CONSTRAINT FOUND: " + this.valid_constraint);

		// we filter out the aw that don't have the correct forward edges
		this.filterAWS();

		if (this.pattern.isInstance(this.instancePattern)) {
			System.out.println("PATTERN IS INSTANCE");
			this.instancePattern.generateSpecificSemantics();
			// System.out.println(this.instancePattern.getSemantics());
			// Thread.sleep(100000);
			this.semanticPropertyRefine();
			if (this.valid_constraint == null || this.valid_constraint.length() == 0) {
				throw new Exception(
						"GUIFunctionality_refine - refine: error refining semantic property.");
			}
			System.out.println("FINAL CONSTRAINT FOUND: " + this.valid_constraint);

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

						String edge = aw.getId() + " - " + target_window.getId();
						System.out.println("DISCOVER DYNAMIC EDGE: looking for edge " + edge
								+ " (from " + paw.getId() + ").");

						if (this.covered_dyn_edges.contains(edge)) {
							System.out.println("DISCOVER DYNAMIC EDGE: edge already found before.");
							continue;
						}
						final GUITestCase tc = this.getTestToCoverEdge(source_window,
								target_window, aw);
						if (tc == null) {
							System.out.println("DISCOVER DYNAMIC EDGE: edge not found.");
							continue;
						}

						final Instance_window found = this.getFoundWindow(tc, target, paw);
						final String old_valid = this.valid_constraint;
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
								this.valid_constraint = "";
							} else {
								this.covered_dyn_edges.add(edge);
								this.unvalid_constraints.addAll(this.new_unvalid_constraints);
							}
						} else {
							System.out.println("DISCOVER DYNAMIC EDGE: edge not found.");
							// this.unvalid_constraints.add("not(" +
							// this.valid_constraint + ")");
							if (this.valid_constraint.length() == 0) {
								this.valid_constraint = old_valid;
							} else {
								this.valid_constraint = "";
							}
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

			System.out.println("DISCOVER DYNAMIC WINDOW SPECIAL: looking for "
					+ to_discover.getId());

			final GUITestCase tc = this.getTestToReachWindow_special(to_discover);
			if (tc == null) {
				System.out.println("DISCOVER DYNAMIC WINDOW SPECIAL: test case not found.");
				continue;
			}

			final Instance_window found = this.getFoundWindow(tc, to_discover, null);

			final String old_valid = this.valid_constraint;

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
					System.out.println("DISCOVER DYNAMIC WINDOW SPECIAL: window found.");
				}

				if (!this.instancePattern.isSemanticsValid()) {
					System.out
							.println("DISCOVER WINDOW SPECIAL: semantics not valid, window not found.");
					this.instancePattern = old;
					this.valid_constraint = "";
				} else {
					this.valid_constraint = this.getAdaptedConstraint(this.instancePattern
							.getSemantics());
					this.unvalid_constraints.addAll(this.new_unvalid_constraints);
				}

				continue mainloop;
			} else {
				System.out.println("DISCOVER DYNAMIC WINDOW: window not found.");
				// this.unvalid_constraints.add(this.getAdaptedConstraint(this.instancePattern
				// .getSemantics()));
				if (this.valid_constraint.length() == 0) {
					this.valid_constraint = old_valid;
				} else {
					this.valid_constraint = "";
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
					// if
					// (this.instancePattern.getGui().getDynamicForwardLinks(aw.getId()).size()
					// > 0) {
					// // if we already have a dynamic edge in the pattern for
					// // this aw it makes no sense to look for more
					// System.out
					// .println("DISCOVER DYNAMIC WINDOW: we already have a dynamic edge for "
					// + aw.getId() + ".");
					// continue;
					// }

					final Window source_window = this.gui.getActionWidget_Window(aw.getId());

					System.out.println("DISCOVER DYNAMIC WINDOW: looking for "
							+ to_discover.getId() + " from " + source_window.getLabel() + ".");

					final GUITestCase tc = this
							.getTestToReachWindow(source_window, to_discover, aw);
					if (tc == null) {
						System.out.println("DISCOVER DYNAMIC WINDOW: test case not found.");
						continue;
					}

					final Instance_window found = this.getFoundWindow(tc, to_discover, paw);
					final String old_valid = this.valid_constraint;

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
							this.valid_constraint = "";
						} else {
							this.covered_dyn_edges.add(edge);
							this.valid_constraint = this.getAdaptedConstraint(this.instancePattern
									.getSemantics());
							this.unvalid_constraints.addAll(this.new_unvalid_constraints);
						}

						continue mainloop;
					} else {
						System.out.println("DISCOVER DYNAMIC WINDOW: window not found.");
						// this.unvalid_constraints.add(this.getAdaptedConstraint(this.instancePattern
						// .getSemantics()));
						if (this.valid_constraint.length() == 0) {
							this.valid_constraint = old_valid;
						} else {
							this.valid_constraint = "";
						}
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

		//
		// final List<String> tcs = new ArrayList<>();
		// for (final GUITestCaseResult tcr : this.observed_tcs) {
		// tcs.add(this.getTCaseFact(tcr));
		// }
		System.out.println("GET ADAPTED CONSTRAINT: start.");

		final List<Fact> facts = new ArrayList<>();
		for (final Fact f : in_sem.getFacts()) {
			if (!f.getIdentifier().equals("sem_prop")) {
				facts.add(f);
			}
		}

		final Alloy_Model mod_filtered = new Alloy_Model(in_sem.getSignatures(), facts,
				in_sem.getPredicates(), in_sem.getFunctions(), in_sem.getOpenStatements());
		final SpecificSemantics sem_filtered = SpecificSemantics.instantiate(mod_filtered);

		String prop = null;
		loop: while (prop == null) {

			for (int cont = 0; cont < this.observed_tcs.size(); cont++) {

				final List<String> constraints = new ArrayList<>(this.unvalid_constraints);
				// constraints.add(tcs.get(cont));
				if (prop != null) {
					constraints.add(prop);
				}

				final Alloy_Model sem = AlloyUtil.getTCaseModel(sem_filtered,
						this.observed_tcs.get(cont));
				final SpecificSemantics new_sem = this.addConstrain(sem, constraints);

				// final SpecificSemantics sem = this.addConstrain(in_sem,
				// constraints);

				// final String runCom = "run {System} for " +
				// ConfigurationManager.getAlloyRunScope()
				// + " but " + time_size + " Time";
				// sem.addRun_command(runCom);
				//
				// System.out.println("start");
				// System.out.println(sem);
				// System.out.println("end");

				final Module comp = AlloyUtil.compileAlloyModel(new_sem.toString());
				final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));
				if (sol.satisfiable()) {
					final String new_prop = AlloyUtil.extractProperty(sol, new_sem);
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
		System.out.println("GET ADAPTED CONSTRAINT: end.");
		return prop;
	}

	protected GUITestCase getTestToReachWindow(final Window sourceWindow,
			final Pattern_window to_discover, final Action_widget aw) throws Exception {

		String property = null;
		GUITestCase tc = null;
		SpecificSemantics new_sem = null;
		this.new_unvalid_constraints = new ArrayList<>();
		final String old_valid_constraint = this.valid_constraint;
		final List<String> old_invalid_constraint = new ArrayList<>(this.unvalid_constraints);

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
				constraints.addAll(this.new_unvalid_constraints);
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
				if (this.valid_constraint.length() > 0) {
					this.new_unvalid_constraints.add("not(" + this.valid_constraint + ")");
					this.valid_constraint = "";
					continue;
				} else {
					this.valid_constraint = old_valid_constraint;
					this.unvalid_constraints = old_invalid_constraint;
					return null;
				}
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
					System.out.println("GET TEST TO DISCOVER WINDOW: new valid property  - "
							+ property);
				} else {
					this.valid_constraint = "";
					// add constraint
					this.new_unvalid_constraints.add("not(" + property + ")");
					System.out
							.println("GET TEST TO DISCOVER WINDOW: new invalid property added - not("
									+ property + ")");
				}
			}
		}
		return tc;
	}

	protected GUITestCase getTestToReachWindow_special(final Pattern_window to_discover)
			throws Exception {

		String property = null;
		GUITestCase tc = null;
		SpecificSemantics new_sem = null;

		this.new_unvalid_constraints = new ArrayList<>();
		final String old_valid_constraint = this.valid_constraint;
		final List<String> old_invalid_constraint = new ArrayList<>(this.unvalid_constraints);

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
				constraints.addAll(this.new_unvalid_constraints);
			} else {
				constraints.add(this.valid_constraint);
			}

			new_sem = this.semantic4DiscoverWindow_special(this.instancePattern.getSemantics(),
					to_discover);
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
				if (this.valid_constraint.length() > 0) {
					this.new_unvalid_constraints.add("not(" + this.valid_constraint + ")");
					this.valid_constraint = "";
					continue;
				} else {
					this.valid_constraint = old_valid_constraint;
					this.unvalid_constraints = old_invalid_constraint;
					return null;
				}
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
					System.out
							.println("GET TEST TO DISCOVER WINDOW SPECIAL: new valid property  - "
									+ property);
				} else {
					this.valid_constraint = "";
					// add constraint
					this.new_unvalid_constraints.add("not(" + property + ")");
					System.out
							.println("GET TEST TO DISCOVER WINDOW SPECIAL: new invalid property added - not("
									+ property + ")");
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

		final String old_valid_constraint = this.valid_constraint;
		final List<String> old_invalid_constraint = new ArrayList<>(this.unvalid_constraints);

		while (tc == null) {
			final List<String> constraints = new ArrayList<>();

			if (this.valid_constraint.length() == 0) {
				constraints.addAll(this.unvalid_constraints);
				constraints.addAll(this.new_unvalid_constraints);
			} else {
				constraints.add(this.valid_constraint);
			}

			new_sem = this.semantic4DiscoverEdge(this.instancePattern.getSemantics(), sourceWindow,
					targetWindow, aw);
			final SpecificSemantics constrained = this.addConstrain(new_sem, constraints);

			final Instance_GUI_pattern clone = this.instancePattern.clone();
			clone.setSpecificSemantics(constrained);

			// System.out.println("Start constrained semantics");
			// System.out.println(constrained);
			//
			// System.out.println("End constrained semantics");

			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - getTestToCoverEdge: error generating test case.");
			}

			if (tests.size() == 0) {
				if (this.valid_constraint.length() > 0) {
					this.new_unvalid_constraints.add("not(" + this.valid_constraint + ")");
					this.valid_constraint = "";
					continue;
				} else {
					this.valid_constraint = old_valid_constraint;
					this.unvalid_constraints = old_invalid_constraint;
					return null;
				}
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
					System.out.println("GET TEST TO COVER EDGE: new valid property - " + property);
					this.valid_constraint = property;

				} else {
					this.valid_constraint = "";
					// add constraint
					System.out.println("GET TEST TO COVER EDGE: new invalid property added - not("
							+ property + ")");
					this.new_unvalid_constraints.add("not(" + property + ")");
				}
			}
		}
		return tc;
	}

	// private boolean validateProperty2(final String prop, final
	// SpecificSemantics in_sem)
	// throws Exception {
	//
	// System.out.println("VALIDATE PROPERTY: start.");
	//
	// // we clone the semantics to remove all the facts related to discovering
	// // windows/edges
	// final List<Fact> facts = new ArrayList<>();
	// for (final Fact f : in_sem.getFacts()) {
	// if (!f.getIdentifier().equals("for_discovering")
	// && !f.getIdentifier().equals("sem_prop")) {
	// facts.add(f);
	// }
	// }
	//
	// final Alloy_Model mod_filtered = new Alloy_Model(in_sem.getSignatures(),
	// facts,
	// in_sem.getPredicates(), in_sem.getFunctions(),
	// in_sem.getOpenStatements());
	// final SpecificSemantics sem_filtered =
	// SpecificSemantics.instantiate(mod_filtered);
	//
	// for (int cont = 0; cont < this.observed_tcs.size(); cont++) {
	// // the time size is the number of actions +2 (because of Go)
	// final List<String> constraints = new ArrayList<>();
	// // constraints.add(tcs.get(cont));
	// constraints.add(prop);
	//
	// Alloy_Model sem = AlloyUtil.getTCaseModel(sem_filtered,
	// this.observed_tcs.get(cont));
	// sem = this.addConstrain(sem, constraints);
	//
	// // System.out.println("start validate sem");
	// // System.out.println(sem);
	// // System.out.println("end validate sem");
	//
	// final Module comp = AlloyUtil.compileAlloyModel(sem.toString());
	// final A4Solution sol = AlloyUtil.runCommand(comp,
	// comp.getAllCommands().get(0));
	// if (!sol.satisfiable()) {
	// System.out.println("VALIDATE PROPERTY: -false- end.");
	// return false;
	// }
	// }
	// System.out.println("VALIDATE PROPERTY: -true- end.");
	// return true;
	// }

	private SpecificSemantics addConstrain(final Alloy_Model sem, final List<String> props)
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

	protected SpecificSemantics semantic4DiscoverWindow_special(
			final SpecificSemantics originalSemantic, final Pattern_window pattern_TargetWindow)
			throws Exception {

		// Maybe we should check the action that relates them.
		if (this.instancePattern.getWS_for_PW(pattern_TargetWindow.getId()).size() > 0) {
			throw new Exception("The pattern window to discover was already mapped.");
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
			Signature sigIW = null;
			if (piw.getCardinality().getMax() == 1) {
				sigIW = new Signature("Undiscovered_inputwidget_" + piw.getId(), Cardinality.ONE,
						false, Lists.newArrayList(piw_sig), false);

			} else {
				sigIW = new Signature("Undiscovered_inputwidget_" + piw.getId(), Cardinality.SOME,
						false, Lists.newArrayList(piw_sig), false);
			}
			iw_sig.add(sigIW);

		}

		// We put widgets to the undiscovered window
		final Fact fact_iws_from_undiscover_window = AlloyUtil.createFactsForElement(iw_sig,
				sigWinToDiscover, "iws");

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

			Signature sigAW = null;
			if (paw.getCardinality().getMax() == 1) {
				sigAW = new Signature("Undiscovered_actionwidget_" + paw.getId(), Cardinality.ONE,
						false, Lists.newArrayList(paw_sig), false);
			} else {
				sigAW = new Signature("Undiscovered_actionwidget_" + paw.getId(), Cardinality.SOME,
						false, Lists.newArrayList(paw_sig), false);
			}

			aw_sig.add(sigAW);
		}

		// We put widgets to the undiscovered window
		final Fact fact_aws_from_undiscover_window = AlloyUtil.createFactsForElement(aw_sig,
				sigWinToDiscover, "aws");

		// selectable:
		final List<Pattern_selectable_widget> psws = pattern_TargetWindow.getSelectableWidgets();

		final List<Signature> sw_sig = new ArrayList<>();

		for (final Pattern_selectable_widget psw : psws) {
			if (psw.getCardinality().getMax() == 0) {
				continue;
			}
			final Signature psw_sig = AlloyUtil.searchForParent(originalSemantic, psw);

			if (psw_sig == null) {
				throw new Exception("Element not found: " + psw_sig);
			}

			Signature sigSW = null;
			if (psw.getCardinality().getMax() == 1) {
				sigSW = new Signature("Undiscovered_selectablewidget_" + psw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(psw_sig), false);
			} else {
				sigSW = new Signature("Undiscovered_selectablewidget_" + psw.getId(),
						Cardinality.SOME, false, Lists.newArrayList(psw_sig), false);
			}

			sw_sig.add(sigSW);
		}

		// We put widgets to the undiscovered window
		final Fact fact_sws_from_undiscover_window = AlloyUtil.createFactsForElement(sw_sig,
				sigWinToDiscover, "sws");

		final String fcontent = "some t: Time, w:" + sigWinToDiscover.getIdentifier()
				+ " | Current_window.is_in.t = w";
		final Fact factDiscovering = new Fact("for_discovering", fcontent);

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Fact> facts = new ArrayList<>();
		for (final Fact fact : originalSemantic.getFacts()) {
			if (!fact.getIdentifier().equals("windows_number")) {
				facts.add(fact);
			} else {
				// a new fact for the number of windows is created
				final int num = Integer.valueOf(fact.getContent().substring(9).trim());
				final Fact win_num = new Fact("windows_number", "#Window = " + (num + 1));
				facts.add(win_num);
			}
		}

		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		signatures.add(sigWinToDiscover);
		signatures.addAll(aw_sig);
		signatures.addAll(iw_sig);
		signatures.addAll(sw_sig);

		facts.add(factDiscovering);
		// facts.add(factLinkActions);
		facts.add(fact_aws_from_undiscover_window);
		facts.add(fact_iws_from_undiscover_window);
		facts.add(fact_sws_from_undiscover_window);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);

		final String runCom = "run {System}";

		semantif4DiscoverWindow.addRun_command(runCom);

		return semantif4DiscoverWindow;
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
			Signature sigIW = null;
			if (piw.getCardinality().getMax() == 1) {
				sigIW = new Signature("Undiscovered_inputwidget_" + piw.getId(), Cardinality.ONE,
						false, Lists.newArrayList(piw_sig), false);

			} else {
				sigIW = new Signature("Undiscovered_inputwidget_" + piw.getId(), Cardinality.SOME,
						false, Lists.newArrayList(piw_sig), false);
			}
			iw_sig.add(sigIW);

		}

		// We put widgets to the undiscovered window
		final Fact fact_iws_from_undiscover_window = AlloyUtil.createFactsForElement(iw_sig,
				sigWinToDiscover, "iws");

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

			Signature sigAW = null;
			if (paw.getCardinality().getMax() == 1) {
				sigAW = new Signature("Undiscovered_actionwidget_" + paw.getId(), Cardinality.ONE,
						false, Lists.newArrayList(paw_sig), false);
			} else {
				sigAW = new Signature("Undiscovered_actionwidget_" + paw.getId(), Cardinality.SOME,
						false, Lists.newArrayList(paw_sig), false);
			}

			aw_sig.add(sigAW);
		}

		// We put widgets to the undiscovered window
		final Fact fact_aws_from_undiscover_window = AlloyUtil.createFactsForElement(aw_sig,
				sigWinToDiscover, "aws");

		// selectable:
		final List<Pattern_selectable_widget> psws = pattern_TargetWindow.getSelectableWidgets();

		final List<Signature> sw_sig = new ArrayList<>();

		for (final Pattern_selectable_widget psw : psws) {
			if (psw.getCardinality().getMax() == 0) {
				continue;
			}
			final Signature psw_sig = AlloyUtil.searchForParent(originalSemantic, psw);

			if (psw_sig == null) {
				throw new Exception("Element not found: " + psw_sig);
			}

			Signature sigSW = null;
			if (psw.getCardinality().getMax() == 1) {
				sigSW = new Signature("Undiscovered_selectablewidget_" + psw.getId(),
						Cardinality.ONE, false, Lists.newArrayList(psw_sig), false);
			} else {
				sigSW = new Signature("Undiscovered_selectablewidget_" + psw.getId(),
						Cardinality.SOME, false, Lists.newArrayList(psw_sig), false);
			}

			sw_sig.add(sigSW);
		}

		// We put widgets to the undiscovered window
		final Fact fact_sws_from_undiscover_window = AlloyUtil.createFactsForElement(sw_sig,
				sigWinToDiscover, "sws");

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

		final String fcontent = "some t, t': Time, w: Window_" + sourceWindow.getId() + ", "
				+ " w': " + sigWinToDiscover.getIdentifier() + " , c: Click " + " | click ["
				+ (sig_action_to_execute.getIdentifier()) + ", t, T/next[t], c] and "
				+ " Current_window.is_in.t = w and Current_window.is_in.t' = w' "
				+ " and t' in T/next[t] and click_semantics["
				+ (sig_action_to_execute.getIdentifier()) + ",t]";
		final Fact factDiscovering = new Fact("for_discovering", fcontent);

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Fact> facts = new ArrayList<>();
		for (final Fact fact : originalSemantic.getFacts()) {
			// we remove the constraint the limited the aw
			if (fact.getIdentifier().equals("Window_" + sourceWindow.getId() + "_aws")) {
				String content = fact.getContent().replace(
						"#Action_widget_" + actionWidget.getId() + ".goes = 0",
						"Action_widget_" + actionWidget.getId() + ".goes = "
								+ sigWinToDiscover.getIdentifier());
				content = content.replace(
						"Action_widget_" + actionWidget.getId() + ".goes = ",
						"Action_widget_" + actionWidget.getId() + ".goes = "
								+ sigWinToDiscover.getIdentifier() + " + ");
				final Fact fact2 = new Fact("Window_" + sourceWindow.getId() + "_aws", content);
				facts.add(fact2);
				continue;
			}
			if (!fact.getIdentifier().equals("windows_number")) {
				facts.add(fact);
			} else {
				// a new fact for the number of windows is created
				final int num = Integer.valueOf(fact.getContent().substring(9).trim());
				final Fact win_num = new Fact("windows_number", "#Window = " + (num + 1));
				facts.add(win_num);
			}
		}

		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		signatures.add(sigWinToDiscover);
		signatures.addAll(aw_sig);
		signatures.addAll(iw_sig);
		signatures.addAll(sw_sig);
		facts.add(factDiscovering);
		// facts.add(factLinkActions);
		facts.add(fact_aws_from_undiscover_window);
		facts.add(fact_iws_from_undiscover_window);
		facts.add(fact_sws_from_undiscover_window);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);

		final String runCom = "run {System}";

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

		final String fcontent = "some t, t': Time, w: Window_" + sourceWindow.getId() + ", w': "
				+ sigWinToDiscover.getIdentifier() + " , c: Click " + " | click ["
				+ (sig_action_to_execute.getIdentifier()) + ", t, T/next[t], c] and "
				+ " Current_window.is_in.t = w and Current_window.is_in.t' = w' "
				+ " and t' in T/next[t] and click_semantics["
				+ (sig_action_to_execute.getIdentifier()) + ",t]";
		final Fact factDiscovering = new Fact("for_discovering", fcontent);

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Fact> facts = new ArrayList<>();
		for (final Fact f : originalSemantic.getFacts()) {
			// we remove the fact that contrained the aw
			if (f.getIdentifier().equals("Window_" + sourceWindow.getId() + "_aws")) {
				String content = "";

				if (f.getContent().contains("#Action_widget_" + actionWidget.getId() + ".goes = 0")) {
					content = f.getContent().replace(
							"#Action_widget_" + actionWidget.getId() + ".goes = 0",
							"Action_widget_" + actionWidget.getId() + ".goes = "
									+ sigWinToDiscover.getIdentifier());
				} else {
					content = f.getContent().replace(
							"Action_widget_" + actionWidget.getId() + ".goes = ",
							"Action_widget_" + actionWidget.getId() + ".goes = "
									+ sigWinToDiscover.getIdentifier() + " + ");
				}
				final Fact fact = new Fact("Window_" + sourceWindow.getId() + "_aws", content);
				facts.add(fact);
				continue;
			}
			facts.add(f);
		}
		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		facts.add(factDiscovering);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);

		final String runCom = "run {System}";

		semantif4DiscoverWindow.addRun_command(runCom);
		return semantif4DiscoverWindow;
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
				+ ""
				+ "(some aw:Action_widget, iw:Input_widget, sw:Selectable_widget, w: Window| one opp:Operation| Track.op.(T/last) = opp and ((opp in Go and opp.where=w and not go_semantics[w, T/prev[T/last]]) or (opp in Click and opp.clicked = aw and not click_semantics[aw, T/prev[T/last]]) or (opp in Fill and opp.filled=iw and not fill_semantics[iw, T/prev[T/last], opp.with])  or (opp in Select and opp.wid=sw and not select_semantics[sw, T/prev[T/last], opp.selected])))}";

		List<String> true_constraints = new ArrayList<>();
		true_constraints.add(this.valid_constraint);

		final Instance_GUI_pattern clone_with = this.instancePattern.clone();
		SpecificSemantics sem_with = this.addConstrain(this.instancePattern.getSemantics(),
				true_constraints);

		mainloop: while ((System.currentTimeMillis() - beginTime) < ConfigurationManager
				.getSemanticRefinementTimeout()) {
			System.out.println("CURRENT SEMANTIC PROPERTY: " + this.valid_constraint);
			sem_with.addRun_command(runCmd);
			// System.out.println(sem_with);
			clone_with.setSpecificSemantics(sem_with);
			final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone_with);
			final List<GUITestCase> tests = test_gen.generateMinimalTestCases();
			if (tests.size() == 0) {
				System.out.println("PROPERTY MAYBE OVERSEMPLIFIED");

				// maybe the property was oversemplified
				// we remove the current sem prop
				final List<Fact> facts = new ArrayList<>();
				for (final Fact fact : sem_with.getFacts()) {
					if (!fact.getIdentifier().equals("sem_prop")) {
						facts.add(fact);
					}
				}
				final Alloy_Model mod = new Alloy_Model(sem_with.getSignatures(), facts,
						sem_with.getPredicates(), sem_with.getFunctions(),
						sem_with.getOpenStatements());

				final List<String> false_constraints = new ArrayList<>();
				false_constraints.addAll(this.unvalid_constraints);
				false_constraints.add("not(" + this.valid_constraint + ")");
				final Random r = new Random();

				while (true) {
					// we randomly pick one of the executed test cases
					final int index = r.nextInt(this.observed_tcs.size());
					sem_with = this.addConstrain(mod, false_constraints);
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
						this.unvalid_constraints.add("not(" + new_prop + ")");

					} else {
						// this.unvalid_constraints.add("not(" +
						// this.valid_constraint + ")");
						this.valid_constraint = new_prop;
						true_constraints = new ArrayList<>();
						true_constraints.add(this.valid_constraint);

						sem_with = this.addConstrain(mod, true_constraints);
						continue mainloop;
					}
				}
			}
			if (tests.size() > 1) {
				throw new Exception(
						"GUIFunctionality_refine - semanticPropertyRefine: impossible to generate test cases.");
			}

			// for (final GUIAction a : tests.get(0).getActions()) {
			// System.out.println(a + " " + a.getWidget().getId());
			// }

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

				this.unvalid_constraints.add("not(" + this.valid_constraint + ")");

				final SpecificSemantics sem_without = SpecificSemantics.instantiate(AlloyUtil
						.getTCaseModel(this.instancePattern.getSemantics(), res));

				String new_prop = null;
				while (new_prop == null) {
					final SpecificSemantics new_sem = this.addConstrain(sem_without,
							this.unvalid_constraints);

					// System.out.println("start");
					// System.out.println(new_sem);
					// System.out.println("end");

					final Module comp = AlloyUtil.compileAlloyModel(new_sem.toString());
					final A4Solution sol = AlloyUtil.runCommand(comp, comp.getAllCommands().get(0));

					if (sol.satisfiable()) {
						new_prop = AlloyUtil.extractProperty(sol, new_sem);
						System.out.println("VALIDATING PROPERTY: " + new_prop);
						if (!this.validateProperty(new_prop, sem_without)) {
							this.unvalid_constraints.add("not(" + new_prop + ")");
							new_prop = null;
						}

					} else {
						System.out
								.println("SEMANTIC PROPERTY REFINE: INCONSISTENCY. SEMANTIC PROPERTY NOT FOUND!");
						this.valid_constraint = "";
						return;
					}
				}
				this.valid_constraint = new_prop;
				true_constraints = new ArrayList<>();
				true_constraints.add(this.valid_constraint);
				// we remove the old sem property
				final List<Fact> facts = new ArrayList<>();
				for (final Fact fact : sem_with.getFacts()) {
					if (!fact.getIdentifier().equals("sem_prop")) {
						facts.add(fact);
					}
				}

				final Alloy_Model mod = new Alloy_Model(sem_with.getSignatures(), facts,
						sem_with.getPredicates(), sem_with.getFunctions(),
						sem_with.getOpenStatements());
				sem_with = this.addConstrain(mod, true_constraints);
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
			if (!f.getIdentifier().equals("for_discovering")
					&& !f.getIdentifier().equals("sem_prop")
					&& !f.getIdentifier().equals("testcase")) {
				facts.add(f);
			}
		}

		final Alloy_Model mod_filtered = new Alloy_Model(in_sem.getSignatures(), facts,
				in_sem.getPredicates(), in_sem.getFunctions(), in_sem.getOpenStatements());
		SpecificSemantics sem_filtered = SpecificSemantics.instantiate(mod_filtered);
		final List<String> constraints = new ArrayList<>();
		// constraints.add(tcs.get(cont));
		constraints.add(prop);
		sem_filtered = this.addConstrain(sem_filtered, constraints);

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

	public void filterAWS() throws Exception {

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
