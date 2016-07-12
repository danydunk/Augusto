package usi.guisemantic.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.guisemantic.SpecificSemantics;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.entity.Fact;
import usi.guisemantic.alloy.entity.Function;
import usi.guisemantic.alloy.entity.Predicate;
import usi.guisemantic.alloy.entity.Signature;

import com.google.common.collect.Lists;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;

public class AlloyTestCaseGenerator {

	final static private int RUN_INITIAL_TIMEOUT = 120000; // 2 minute
	final static private int MAX_RUN = 5;

	final Instance_GUI_pattern instance;

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each run command is run for a maximum of MAX_RUN
	 * times. The timeout of each run command is RUN_INITIAL_TIMEOUT. When a
	 * command goes in timeout is rerun with a double timeout. If a command is
	 * unsat, the scope is dubled.
	 *
	 * @param initial_timout
	 * @return
	 * @throws Exception
	 */

	public AlloyTestCaseGenerator(final Instance_GUI_pattern instance) {

		this.instance = instance;
	}

	public List<GUITestCase> generateTestCases() throws Exception {

		return this.generateTestCases(MAX_RUN, RUN_INITIAL_TIMEOUT);
	}

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each run command is run for a maximum of max_run
	 * times. The timeout of each run command is initial_timeout. When a command
	 * goes in timeout is rerun with a double timeout. If a command is unsat,
	 * the scope is dubled.
	 *
	 * @param model
	 * @param max_run
	 * @param initial_timout
	 * @return
	 * @throws Exception
	 */
	public List<GUITestCase> generateTestCases(final int max_run, final int initial_timout)
			throws Exception {

		final SpecificSemantics model = this.instance.getSemantics();

		// inner class used to run threads for parallelism
		final class RunCommandThread extends Thread {

			final private Command run_command;
			final private Module alloy_model;
			private A4Solution solution;
			private boolean exception;

			public RunCommandThread(final Command run, final Module alloy) {

				this.run_command = run;
				this.alloy_model = alloy;
				this.exception = false;
			}

			@Override
			public void run() {

				Command run = this.run_command;
				int timeout = initial_timout;
				try {
					for (int x = 0; x < max_run; x++) {
						System.out.println("STARTING COMMAND: " + run.toString() + " RUN "
								+ (x + 1));
						final A4Solution solution = AlloyUtil.runCommand(this.alloy_model, run,
								timeout);

						if (solution == null) {
							timeout += initial_timout;
							System.out.println("RUN " + (x + 1) + " COMMAND:" + run.toString()
									+ " timeout. New timeout =" + timeout);
						} else if (!solution.satisfiable()) {
							final int new_scope = run.overall + this.run_command.overall;

							run = new Command(this.run_command.pos, this.run_command.label,
									this.run_command.check, new_scope, this.run_command.bitwidth,
									this.run_command.maxseq, this.run_command.expects,
									this.run_command.scope, this.run_command.additionalExactScopes,
									this.run_command.formula, this.run_command.parent);
							if (x + 1 < max_run) {
								System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
										+ " unsat. New scope = " + new_scope);
							}

						} else {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
									+ " found solution.");
							this.solution = solution;
							break;
						}
					}
				} catch (final Exception e) {
					this.exception = true;
				}
			}

			public A4Solution getSolution() {

				return this.solution;
			}

			public boolean hasExceptions() {

				return this.exception;
			}
		}

		final String alloy_model = model.toString();
		System.out.println("START ALLOY MODEL");
		System.out.println(model);
		System.out.println("END ALLOY MODEL");

		final Module compiled = AlloyUtil.compileAlloyModel(alloy_model);

		final List<Command> run_commands = compiled.getAllCommands();

		final List<RunCommandThread> threads = new ArrayList<>();
		for (final Command cmd : run_commands) {
			final RunCommandThread rc = new RunCommandThread(cmd, compiled);
			rc.start();
			threads.add(rc);
		}

		final List<A4Solution> solutions = new ArrayList<>();
		for (final RunCommandThread t : threads) {
			t.join();
			if (!t.hasExceptions() && t.getSolution() != null && t.getSolution().satisfiable()) {
				solutions.add(t.getSolution());
			}
		}

		final List<GUITestCase> out = new ArrayList<>();
		// TO DO: add the creation of GUI test cases
		for (final A4Solution sol : solutions) {
			out.add(this.analyzeTuples(sol));
		}

		return out;
	}

	protected GUITestCase analyzeTuples(final A4Solution solution) throws Exception {

		// TODO: oracle
		final List<A4Tuple> tracks = AlloyUtil.getTuples(solution, "Track");

		final List<GUIAction> actions = new ArrayList<>(tracks.size());
		for (final A4Tuple t : tracks) {
			actions.add(null);
		}

		for (final A4Tuple tuple : tracks) {
			if (tuple.arity() != 3) {
				throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong arity of track");
			}

			final int time_index = Integer.valueOf(tuple.atom(2).split("\\$")[1]);

			// TODO: fix
			// the oracle is retrieved
			// final Window oracle = null;
			// final List<A4Tuple> curr = AlloyUtil.getTuples(solution,
			// "Current_window");
			// for (final A4Tuple t : curr) {
			// if (t.arity() != 3) {
			// throw new Exception(
			// "AlloyTestCaseGenerator - analyzeTuples: wrong arity of current window");
			// }
			// if (t.atom(2).equals(tuple.atom(2))) {
			// String window = t.atom(1).split("\\$")[0];
			// if (window.equals("General")) {
			// break;
			// }
			// window = window.substring(7);
			// Window target_w = null;
			// for (final Instance_window w : this.instance.getWindows()) {
			// if (w.getInstance().getId().equals(window)) {
			// target_w = w.getInstance();
			// break;
			// }
			// }
			//
			// if (target_w == null) {
			// throw new Exception(
			// "AlloyTestCaseGenerator - analyzeTuples: error detecting oracle.");
			// }
			// // we create a new window
			// // oracle = new Window(target_w.getId(), target_w.isModal(),
			// // target_w.getLabel(),
			// // target_w.isRoot());
			// for (final Action_widget aw : target_w.getActionWidgets()) {
			// oracle.addWidget(aw);
			// }
			// for (final Input_widget iw : target_w.getInputWidgets()) {
			// final List<A4Tuple> values = AlloyUtil.getTuples(solution,
			// "Input_widget_"
			// + iw.getId());
			// String v = "";
			// for (final A4Tuple value : values) {
			// if (value.arity() != 3) {
			// throw new Exception(
			// "AlloyTestCaseGenerator - analyzeTuples: error retriving input widget value.");
			// }
			// if (value.atom(2).equals(tuple.atom(2))) {
			// final int value_index = Integer
			// .valueOf(value.atom(1).split("\\$")[1]);
			// // TODO: manage values
			// v = String.valueOf(value_index);
			// }
			// }
			//
			// final Input_widget iww = new Input_widget(iw.getId(),
			// iw.getLabel(), v,
			// iw.getClasss());
			// oracle.addWidget(iww);
			// }
			// for (final Selectable_widget sw :
			// target_w.getSelectableWidgets()) {
			// // TODO
			// oracle.addWidget(sw);
			// }
			//
			// break;
			// }
			// }

			final List<A4Tuple> params = AlloyUtil.getTuples(solution, tuple.atom(1));

			if (tuple.atom(1).startsWith("Go")) {
				if (params.size() != 1) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for go.");
				}
				final A4Tuple wid_tuple = params.get(0);
				if (wid_tuple.atom(1).equals("General$0")) {
					continue;
				}

				if (!wid_tuple.atom(1).startsWith("Window_")) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in go.");
				}
				String window_id = wid_tuple.atom(1).substring(7);
				window_id = window_id.split("\\$")[0];

				Window target_w = null;
				for (final Instance_window w : this.instance.getWindows()) {
					if (w.getInstance().getId().equals(window_id)) {
						target_w = w.getInstance();
						break;
					}
				}

				if (target_w == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in go.");
				}

				final Go action = new Go(target_w, null);
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Fill")) {
				if (params.size() != 2) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for fill.");
				}

				String iw_id = null;
				int value_index = -1;

				for (final A4Tuple t : params) {
					if (t.atom(1).startsWith("Input_widget_")) {
						iw_id = t.atom(1).substring(13);
						iw_id = iw_id.split("\\$")[0];
						continue;
					}
					if (t.atom(1).startsWith("Value")) {
						value_index = Integer.valueOf(t.atom(1).split("\\$")[1]);
						continue;
					}
				}

				if (value_index == -1 || iw_id == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in fill.");
				}

				Input_widget target_iw = null;
				for (final Input_widget iw : this.instance.getInput_widgets()) {
					if (iw.getId().equals(iw_id)) {
						target_iw = iw;
						break;
					}
				}

				if (target_iw == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in fill.");
				}

				// TODO: deal with input data
				final Fill action = new Fill(target_iw, null, String.valueOf(value_index));
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Click")) {
				if (params.size() != 1) {
					throw new Exception(
							"AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for click.");
				}
				final A4Tuple wid_tuple = params.get(0);
				if (!wid_tuple.atom(1).startsWith("Action_widget_")) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in click.");
				}
				String aw_id = wid_tuple.atom(1).substring(14);
				aw_id = aw_id.split("\\$")[0];

				Action_widget target_aw = null;
				for (final Action_widget aw : this.instance.getAction_widgets()) {
					if (aw.getId().equals(aw_id)) {
						target_aw = aw;
						break;
					}
				}

				if (target_aw == null) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error in click.");
				}

				final Click action = new Click(target_aw, null);
				actions.set(time_index - 1, action);
				continue;
			}
			// TODO:select
		}

		final GUITestCase test = new GUITestCase(actions);
		return test;
	}

	public static SpecificSemantics semantic4DiscoverWindow(
			final Instance_GUI_pattern instancePattern, final Window sourceWindow,
			final Pattern_window pattern_TargetWindow,
			final Pattern_action_widget patternActionWidget // the
			// edge
			// that
			// we
			// want
			// use
			// to
			// discover
			// the
			// window
			) throws Exception {

		final Pattern_window pattern_source_window = instancePattern.getWindows_mapping().get(
				sourceWindow);
		if (pattern_source_window == null) {
			throw new Exception("Source window without pattern correspondence");
		}

		// Maybe we should check the action that relates them.
		if (instancePattern.getWindows_mapping().values().contains(pattern_TargetWindow)) {
			throw new Exception("The pattern window to discover was already mapped.");
		}

		if (!pattern_source_window.getActionWidgets().contains(patternActionWidget)) {
			throw new Exception(
					"The action to exercice is not included in the source pattern window");
		}

		final SpecificSemantics originalSemantic = instancePattern.getSemantics();

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

		System.out.println("Creating " + sigWinToDiscover);

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

			System.out.println("Creating " + sigIW);

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
			System.out.println("Creating " + sigAW);

		}

		// We put widgets to the undiscover window
		final Fact fact_aws_from_undiscover_window = AlloyUtil.createFactsForElement(aw_sig,
				sigWinToDiscover, "aws");
		System.out.println("Creating " + fact_aws_from_undiscover_window);

		Signature sig_action_to_execute = null;
		for (final Action_widget awi : instancePattern.getAction_widgets_mapping().keySet()) {
			if (instancePattern.getAction_widgets_mapping().get(awi).equals(patternActionWidget)) {
				// We have the action widget that we want to trigger to discover
				// the window.

				final String candidateId = "Action_widget_" + awi.getId();
				final List<Signature> sigAWs = originalSemantic.getSignatures().stream()
						.filter(e -> e.getIdentifier().equals(candidateId))
						.collect(Collectors.toList());

				if (sigAWs.isEmpty()) {
					final List<String> signames = originalSemantic.getSignatures().stream()
							.map(e -> e.getIdentifier()).collect(Collectors.toList());

					throw new Exception("Action widget without signature: " + awi.getId()
							+ " from " + signames);
				}
				sig_action_to_execute = sigAWs.get(0);
				break;
			}

		}

		if (sig_action_to_execute == null) {
			throw new Exception("Signature not found for action widget: ");
		}

		final String contentFactLink = sig_action_to_execute.getIdentifier() + ".goes = "
				+ sigWinToDiscover.getIdentifier();

		final Fact factLinkActions = new Fact(sigWinToDiscover.getIdentifier() + "_awsd",
				contentFactLink);
		System.out.println("Creating " + factLinkActions);

		/*
		 * one sig Conf_undiscovered extends Confirm{ } one sig
		 * Action_widget_aw5 extends Ok { } one sig Action_widget_aw6 extends
		 * Cancel { } fact Conf_undiscovered_aws{ Action_widget_aw3.goes =
		 * Conf_undiscovered Conf_undiscovered.aws = Action_widget_aw5 +
		 * Action_widget_aw6 }
		 */
		//

		//
		final String fcontent = "some t, t': Time, w: Window_w2, " + " w': "
				+ sigWinToDiscover.getIdentifier() + " , c: Click " + " | click ["
				+ (sig_action_to_execute.getIdentifier()) + ", t, T/next[t], c] and "
				+ " Current_window.is_in.t = w and Current_window.is_in.t' = w' "
				+ " and t' in T/next[t] ";
		final Fact factDiscovering = new Fact("", fcontent);

		System.out.println("Creating " + factDiscovering);

		final String runCom = "run {System} for 4";
		/*
		 * fact{ some t, t': Time, w: Window_w2, w': Conf_undiscovered, c: Click
		 * | click [Action_widget_aw3, t, T/next[t], c] and
		 * Current_window.is_in.t = w and Current_window.is_in.t' = w' and t' in
		 * T/next[t] }
		 */
		// run{System} for 20 but exactly 10 Time

		// After discovering
		// fact{
		// all t: Time | (#Input_widget_iw1.content.t=1 and click_semantics
		// [Action_widget_aw3, t])
		// }

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
		opens.add(runCom);

		final SpecificSemantics semantif4DiscoverWindow = new SpecificSemantics(signatures, facts,
				predicates, functions, opens);

		return semantif4DiscoverWindow;
	}

	public SpecificSemantics validateRequired(final Instance_GUI_pattern instancePattern,
			final Signature iwsigRequired, final Signature awsig) throws Exception {

		final SpecificSemantics originalSemantic = instancePattern.getSemantics();
		final int generalMaxSet = 4;
		int time = 1;
		boolean foundSat = false;
		final int limitTime = 10;
		SpecificSemantics semanticSolution = null;

		while (!foundSat && time < limitTime) {

			// We create the fact
			final String factContent = "all t: Time |  (#" + iwsigRequired.getIdentifier()
					+ ".content.t=" + time + ") => click_semantics [" + awsig.getIdentifier()
					+ ", t]";
			final Fact fact = new Fact("", factContent);

			final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
			final List<Fact> facts = new ArrayList<>(originalSemantic.getFacts());
			final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
			final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
			final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

			facts.add(fact);
			// Run command
			final String restriction = "for " + generalMaxSet + " but exactly " + time + " Time";
			opens.add("run{System} " + restriction);

			//
			final SpecificSemantics semantic = new SpecificSemantics(signatures, facts, predicates,
					functions, opens);
			// ---
			final Module compiled = AlloyUtil.compileAlloyModel(semantic.toString());

			if (compiled == null) {
				new IllegalStateException("Any module created");
			}
			final List<Command> run_commands = compiled.getAllCommands();
			System.out.println(run_commands);
			final List<Command> runSystem = run_commands.stream()
					.filter(e -> e.toString().equals("Run run$1 " + restriction))
					.collect(Collectors.toList());

			if (runSystem.isEmpty()) {
				new IllegalStateException("Any module created");
			}

			final A4Solution solution = AlloyUtil.runCommand(compiled, runSystem.get(0));
			System.out.println("Has solution: " + solution);

			foundSat = solution.satisfiable();
			if (foundSat) {
				semanticSolution = semantic;
			} else {
				time++;
			}

		}

		if (!foundSat) {
			System.out.println("Not found solution after time " + time);

		}

		/*
		 * fact{ all t: Time | (#Input_widget_iw2.content.t=0) =>
		 * click_semantics [Action_widget_aw3, t] }
		 *
		 * fact{ Required.associated_to = Input_widget_iw2 } fact{ all t: Time |
		 * (#Input_widget_iw1.content.t=1 and click_semantics
		 * [Action_widget_aw3, t]) }
		 */

		return semanticSolution;

	}
}
