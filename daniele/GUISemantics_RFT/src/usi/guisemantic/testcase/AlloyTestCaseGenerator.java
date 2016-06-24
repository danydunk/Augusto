package usi.guisemantic.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guifunctionality.mapping.Instance_window;
import usi.guisemantic.SpecificSemantics;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guistructure.Action_widget;
import usi.guistructure.Input_widget;
import usi.guistructure.Selectable_widget;
import usi.guistructure.Window;

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
	public List<GUITestCase> generateTestCases(final int max_run, final int initial_timout) throws Exception {

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
						System.out.println("STARTING COMMAND: " + run.toString() + " RUN " + (x + 1));
						final A4Solution solution = AlloyUtil.runCommand(this.alloy_model, run, timeout);

						if (solution == null) {
							timeout += initial_timout;
							System.out.println("RUN " + (x + 1) + " COMMAND:" + run.toString()
									+ " timeout. New timeout =" + timeout);
						} else if (!solution.satisfiable()) {
							final int new_scope = run.overall + this.run_command.overall;

							run = new Command(this.run_command.pos, this.run_command.label, this.run_command.check,
									new_scope, this.run_command.bitwidth, this.run_command.maxseq,
									this.run_command.expects, this.run_command.scope,
									this.run_command.additionalExactScopes, this.run_command.formula,
									this.run_command.parent);
							if (x + 1 < max_run) {
								System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
										+ " unsat. New scope = " + new_scope);
							}

						} else {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString() + " found solution.");
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
		final List<RunCommandThread> threads = run_commands.stream().map(e -> {
			final RunCommandThread out = new RunCommandThread(e, compiled);
			out.start();
			return out;
		}).collect(Collectors.toList());

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

			// the oracle is retrieved
			Window oracle = null;
			final List<A4Tuple> curr = AlloyUtil.getTuples(solution, "Current_window");
			for (final A4Tuple t : curr) {
				if (t.arity() != 3) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong arity of current window");
				}
				if (t.atom(2).equals(tuple.atom(2))) {
					String window = t.atom(1).split("\\$")[0];
					if (window.equals("General")) {
						break;
					}
					window = window.substring(7);
					Window target_w = null;
					for (final Instance_window w : this.instance.getWindows()) {
						if (w.getInstance().getId().equals(window)) {
							target_w = w.getInstance();
							break;
						}
					}

					if (target_w == null) {
						throw new Exception("AlloyTestCaseGenerator - analyzeTuples: error detecting oracle.");
					}
					// we create a new window
					oracle = new Window(target_w.getId(), target_w.isModal(), target_w.getLabel(), target_w.isRoot());
					for (final Action_widget aw : target_w.getActionWidgets()) {
						oracle.addActionWidget(aw);
					}
					for (final Input_widget iw : target_w.getInputWidgets()) {
						final List<A4Tuple> values = AlloyUtil.getTuples(solution, "Input_widget_" + iw.getId());
						String v = "";
						for (final A4Tuple value : values) {
							if (value.arity() != 3) {
								throw new Exception(
										"AlloyTestCaseGenerator - analyzeTuples: error retriving input widget value.");
							}
							if (value.atom(2).equals(tuple.atom(2))) {
								final int value_index = Integer.valueOf(value.atom(1).split("\\$")[1]);
								// TODO: manage values
								v = String.valueOf(value_index);
							}
						}

						final Input_widget iww = new Input_widget(iw.getId(), iw.getLabel(), v, iw.getClasss());
						oracle.addInputWidget(iww);
					}
					for (final Selectable_widget sw : target_w.getSelectableWidgets()) {
						// TODO
						oracle.addSelectableWidget(sw);
					}

					break;
				}
			}

			final List<A4Tuple> params = AlloyUtil.getTuples(solution, tuple.atom(1));

			if (tuple.atom(1).startsWith("Go")) {
				if (params.size() != 1) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for go.");
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

				final Go action = new Go(target_w, oracle);
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Fill")) {
				if (params.size() != 2) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for fill.");
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
				final Fill action = new Fill(target_iw, oracle, String.valueOf(value_index));
				actions.set(time_index - 1, action);
				continue;
			}

			if (tuple.atom(1).startsWith("Click")) {
				if (params.size() != 1) {
					throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong number of tuples for click.");
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

				final Click action = new Click(target_aw, oracle);
				actions.set(time_index - 1, action);
				continue;
			}
			// TODO:select
		}

		final GUITestCase test = new GUITestCase(actions);
		return test;
	}
}
