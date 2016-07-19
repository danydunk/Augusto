package usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import usi.configuration.ConfigurationManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;

import com.google.common.collect.Lists;

public class GUIFunctionality_refine {

	private final Instance_GUI_pattern instancePattern;
	private final GUI gui;
	private final GUI_Pattern pattern;
	private final SpecificSemantics semantics;

	public GUIFunctionality_refine(final Instance_GUI_pattern instance, final GUI gui) {

		this.instancePattern = instance;
		this.pattern = instance.getGuipattern();
		this.gui = instance.getGui();
		this.semantics = instance.getSemantics();
	}

	public Instance_GUI_pattern refine() throws Exception {

		final Instance_GUI_pattern working_obj = this.instancePattern;

		final GUI_Pattern pattern = this.instancePattern.getGuipattern();
		final SpecificSemantics semantics = this.instancePattern.getSemantics();

		final GUI matched_gui = this.instancePattern.getGui();

		// all windows match found
		for (final Instance_window iw : this.instancePattern.getWindows()) {
			final Pattern_window pw = iw.getPattern();
			final Window sourcew = iw.getInstance();

			for (final Pattern_action_widget paw : pw.getActionWidgets()) {
				// all the dynamic edges
				for (final Pattern_window target_pw : pattern.getDynamicForwardLinks(paw.getId())) {
					// all the concrete aw that match the paw
					for (final Action_widget aw : iw.getAWS_for_PAW(paw.getId())) {
						final Instance_GUI_pattern clone = working_obj.clone();
						final List<Window> target_w_matched = this.instancePattern
								.getPatternWindowMatches(target_pw.getId());

						// if target window was already discovered
						if (target_w_matched.size() > 0) {

							boolean edge_found = false;
							for (final Window w : target_w_matched) {
								if (matched_gui.isEdge(aw.getId(), w.getId())) {
									edge_found = true;
									break;
								}
							}
							if (!edge_found) {
								for (final Window w : target_w_matched) {
									final SpecificSemantics new_sem = this.semantic4DiscoverWindow(
											semantics, sourcew, w, aw);
									clone.setSpecificSemantics(new_sem);
									final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(
											clone);
									final List<GUITestCase> tests = test_gen
											.generateMinimalTestCases();

								}
							}
						} else {
							// if target window needs to be discovered
							final SpecificSemantics new_sem = this.semantic4DiscoverWindow(
									semantics, sourcew, target_pw, aw);
							clone.setSpecificSemantics(new_sem);
							final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(
									clone);
							final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

						}
					}

				}
			}
		}

		return null;
	}

	private Instance_GUI_pattern discoverDynamicEdges() throws Exception {

		// final Instance_GUI_pattern working_obj = this.instancePattern;
		//
		// final GUI_Pattern pattern = this.instancePattern.getGuipattern();
		// final SpecificSemantics semantics =
		// this.instancePattern.getSemantics();
		//
		// final GUI matched_gui = this.instancePattern.getGui();
		//
		// for (final Pattern_window pw : pattern.getWindows()) {
		// final List<Window> target_w_matched =
		// this.instancePattern.getPatternWindowMatches(pw
		// .getId());
		// // if target_w_matched is empty it means the pattern_window was not
		// // found
		// if (target_w_matched.size() == 0) {
		// continue;
		// }
		// boolean edge_found = false;
		// for (final Window w : target_w_matched) {
		// if (matched_gui.isEdge(aw.getId(), w.getId())) {
		// edge_found = true;
		// break;
		// }
		// }
		//
		// }
		//
		// // all windows match found
		// for (final Instance_window iw : this.instancePattern.getWindows()) {
		// final Pattern_window pw = iw.getPattern();
		// final Window sourcew = iw.getInstance();
		//
		// for (final Pattern_action_widget paw : pw.getActionWidgets()) {
		// // all the dynamic edges
		// for (final Pattern_window target_pw :
		// pattern.getDynamicForwardLinks(paw.getId())) {
		// // all the concrete aw that match the paw
		// for (final Action_widget aw : iw.getAw_map().get(paw)) {
		// // we clone the instance
		// final Instance_GUI_pattern clone = working_obj.clone();
		// // all the windows that matched the target window of the
		// // edge
		// final List<Window> target_w_matched = this.instancePattern
		// .getPatternWindowMatches(target_pw.getId());
		//
		// // if target window was already discovered
		// if (target_w_matched.size() > 0) {
		//
		// boolean edge_found = false;
		// for (final Window w : target_w_matched) {
		// if (matched_gui.isEdge(aw.getId(), w.getId())) {
		// edge_found = true;
		// break;
		// }
		// }
		// if (!edge_found) {
		// for (final Window w : target_w_matched) {
		// final SpecificSemantics new_sem = this.semantic4DiscoverWindow(
		// semantics, sourcew, w, aw);
		// clone.setSpecificSemantics(new_sem);
		// final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(
		// clone);
		// final List<GUITestCase> tests = test_gen
		// .generateMinimalTestCases();
		//
		// }
		// }
		// }
		// }
		//
		// }
		// }
		// }

		return null;
	}

	private Instance_GUI_pattern discoverWindows() throws Exception {

		// final Instance_GUI_pattern working_obj = this.instancePattern;

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
					final SpecificSemantics new_sem = this.semantic4DiscoverWindow(this.semantics,
							source_window, to_discover, aw);

					final Instance_GUI_pattern clone = this.instancePattern.clone();
					clone.setSpecificSemantics(new_sem);

					final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
					final List<GUITestCase> tests = test_gen.generateMinimalTestCases();

					if (tests.size() != 1) {
						throw new Exception(
								"GUIFunctionality_refine - discoverClasses: error generating test case.");
					}

					// execute test

					// reached window match
					// add the dynamic edge to the gui
					// rip the window
					// update instance

					// reached window does not match

					// add contraints

				}

			}

		}

		return null;
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

	private SpecificSemantics semantic4DiscoverWindow(final SpecificSemantics originalSemantic,
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

	private boolean isWindowFound(final GUITestCase tc, final Pattern_window target) {

		return true;
	}

}
