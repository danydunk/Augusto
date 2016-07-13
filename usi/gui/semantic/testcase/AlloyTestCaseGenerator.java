package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.alloy.entity.Function;
import usi.gui.semantic.alloy.entity.Predicate;
import usi.gui.semantic.alloy.entity.Signature;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;

public class AlloyTestCaseGenerator {

	private long RUN_INITIAL_TIMEOUT = 120000; // 2 minute
	private int MAX_RUN = 5;

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

	public AlloyTestCaseGenerator(final Instance_GUI_pattern instance, final int max_run,
			final long initial_timout) {

		this.instance = instance;
		this.MAX_RUN = max_run;
		this.RUN_INITIAL_TIMEOUT = initial_timout;
	}

	public AlloyTestCaseGenerator(final Instance_GUI_pattern instance) {

		this.instance = instance;
	}

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each run command is run for a maximum of max_run
	 * times. The timeout of each run command is initial_timeout. When a command
	 * goes in timeout is rerun with a double timeout. If a command is unsat,
	 * the scope is dubled.
	 *
	 * @return
	 * @throws Exception
	 */
	public List<GUITestCase> generateTestCases() throws Exception {

		final SpecificSemantics model = this.instance.getSemantics();

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
			if (t.hasExceptions()) {
				System.out.println();
			}
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

	/**
	 * Function that generates GUI test cases running the run commands contained
	 * in a specific semantics. Each run command is run for a maximum of max_run
	 * times. The timeout of each run command is initial_timeout. When a command
	 * goes in timeout is rerun with a double timeout. If a command is unsat,
	 * the scope is dubled.
	 *
	 * @return
	 * @throws Exception
	 */
	public List<GUITestCase> generateMinimalTestCases() throws Exception {

		final SpecificSemantics model = this.instance.getSemantics();

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
				for (final Input_widget iw : this.instance.getGui().getInput_widgets()) {
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
				for (final Action_widget aw : this.instance.getGui().getAction_widgets()) {
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

	// public SpecificSemantics validateRequired(final Instance_GUI_pattern
	// instancePattern,
	// A4Solution solution, final GUITestCase tc, boolean result) throws
	// Exception {

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

	// public SpecificSemantics validateRequired(final Instance_GUI_pattern
	// instancePattern,
	// A4Solution solution, final GUITestCase tc, boolean result) throws
	// Exception {

	// 1rst this/Track<:op={Track$0->Go$0->Time$1, Track$0->Click$0->Time$2,
	// Track$0->Click$1->Time$3}
	// 2nd as this/Click<:clicked={Click$0->Action_widget_aw1$0,
	// Click$1->Action_widget_aw3$0}
	// with this/Fill={}

	public static SpecificSemantics validateRequired(final Instance_GUI_pattern instancePattern,
			final A4Solution solution, final GUITestCase tc) throws Exception {

		final SpecificSemantics originalSemantic = instancePattern.getSemantics();

		final int timeToSearch = tc.getActions().size() - 2; // last time - 1//

		SpecificSemantics semanticSolution = null;

		final List<Fact> facts = new ArrayList<>(originalSemantic.getFacts());

		// 1- take last time
		// 2- take the state at the t-1
		// 3- create the constrain
		final List<A4Tuple> tracks = AlloyUtil.getTuples(solution, "Track");

		// List with the filled Inputs
		final List<String> inputs = new ArrayList<>(tc.getActions().size());

		// Input affected at last time - 1
		String actionAffected = null;

		// We iterates over track
		for (final A4Tuple tuple : tracks) {
			if (tuple.arity() != 3) {
				throw new Exception("AlloyTestCaseGenerator - analyzeTuples: wrong arity of track");
			}
			final String action = tuple.atom(1);

			final int time_index = Integer.valueOf(tuple.atom(2).split("\\$")[1]);
			// {Track$0->Go$0->Time$1
			if (time_index == timeToSearch) {
				// if the time of the track is the same we search tmax-1
				final List<A4Tuple> clicks = AlloyUtil.getTuples(solution, "Click");
				// Click$0->Action_widget_aw1$0
				for (final A4Tuple click : clicks) {
					if (click.atom(0).equals(action)) {
						actionAffected = click.atom(1);
						break;
					}
				}
			} else if (time_index < timeToSearch) {
				// for the previous times, we search for fills at that time
				final List<A4Tuple> fills = AlloyUtil.getTuples(solution, "Fill");
				// Click$0->Action_widget_aw1$0
				for (final A4Tuple fill : fills) {
					if (fill.atom(0).equals(action)) {
						final String inputAffected = fill.atom(1);
						System.out.println("Input found " + inputAffected);
						//
						final String idIn = inputAffected.split("\\$")[0];
						inputs.set(time_index, idIn);

					}
				}

			}

		}

		for (int i = 0; i < inputs.size(); i++) {
			//
			final String idIn = inputs.get(i);
			if (idIn != null) {
				final String factContent = "all t: Time |  (#" + idIn + ".content.t="
						+ timeToSearch + ") => click_semantics [" + actionAffected + ", t]";
				final Fact fact = new Fact("", factContent);
				System.out.println("Creating " + factContent);
				facts.add(fact);
			}
		}

		// We create the fact

		final List<Signature> signatures = new ArrayList<>(originalSemantic.getSignatures());
		final List<Predicate> predicates = new ArrayList<>(originalSemantic.getPredicates());
		final List<Function> functions = new ArrayList<>(originalSemantic.getFunctions());
		final List<String> opens = new ArrayList<>(originalSemantic.getOpenStatements());

		semanticSolution = new SpecificSemantics(signatures, facts, predicates, functions, opens);

		return semanticSolution;
	}

	// inner class used to run threads for parallelism
	final class RunCommandThread extends Thread {

		final private Command run_command;
		final private Module alloy_model;
		private A4Solution solution;
		private boolean exception;
		private final boolean minimal;

		public RunCommandThread(final Command run, final Module alloy) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.minimal = false;
		}

		public RunCommandThread(final Command run, final Module alloy, final boolean minimal) {

			this.run_command = run;
			this.alloy_model = alloy;
			this.exception = false;
			this.minimal = minimal;
		}

		@Override
		public void run() {

			try {
				int time_scope = -1;

				if (this.minimal) {
					time_scope = 2;
				} else {
					time_scope = this.run_command.overall;
				}

				Sig time = null;
				for (final Sig s : this.alloy_model.getAllSigs()) {
					if (s.label.equals("this/Time")) {
						time = s;
					}
				}
				if (time == null) {
					this.exception = true;
					return;
				}
				CommandScope scope = new CommandScope(time, false, time_scope);

				ConstList<CommandScope> scope_list = this.run_command.scope.make(1, scope);

				Command run = new Command(this.run_command.pos, this.run_command.label,
						this.run_command.check, this.run_command.overall,
						this.run_command.bitwidth, this.run_command.maxseq,
						this.run_command.expects, scope_list,
						this.run_command.additionalExactScopes, this.run_command.formula,
						this.run_command.parent);

				long timeout = AlloyTestCaseGenerator.this.RUN_INITIAL_TIMEOUT;

				for (int x = 0; x < AlloyTestCaseGenerator.this.MAX_RUN; x++) {
					System.out.println("STARTING COMMAND: " + run.toString() + " RUN " + (x + 1));
					final A4Solution solution = AlloyUtil
							.runCommand(this.alloy_model, run, timeout);

					if (solution == null) {
						timeout += AlloyTestCaseGenerator.this.RUN_INITIAL_TIMEOUT;
						System.out.println("RUN " + (x + 1) + " COMMAND:" + run.toString()
								+ " timeout. New timeout =" + timeout);
					} else if (!solution.satisfiable()) {

						time_scope++;
						scope = new CommandScope(time, false, time_scope);

						scope_list = this.run_command.scope.make(1, scope);

						run = new Command(this.run_command.pos, this.run_command.label,
								this.run_command.check, this.run_command.overall,
								this.run_command.bitwidth, this.run_command.maxseq,
								this.run_command.expects, scope_list,
								this.run_command.additionalExactScopes, this.run_command.formula,
								this.run_command.parent);

						if (x + 1 < AlloyTestCaseGenerator.this.MAX_RUN) {
							System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
									+ " unsat. Time scope = " + time_scope);
						}

					} else {
						System.out.println("RUN " + (x + 1) + " COMMAND: " + run.toString()
								+ " found solution.");
						this.solution = solution;
						break;
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
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
}
