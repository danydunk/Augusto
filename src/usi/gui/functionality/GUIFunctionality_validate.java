package src.usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import src.usi.configuration.ConfigurationManager;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.semantic.FunctionalitySemantics;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class GUIFunctionality_validate {

	private final Instance_GUI_pattern instancePattern;
	private final GUI gui;
	private List<String> windows_to_visit = new ArrayList<>();
	private final List<String> aw_to_click;
	private final List<String> iw_to_fill;
	private final List<String> sw_to_select;
	private final List<GUITestCase> generated_tcs;
	private final Vector<GUITestCaseResult> out;
	protected List<String> semantic_cases;
	private List<String> semantic_pairwaise_cases;
	private final Table<String, String, String> pairwise;

	// number of times a run command can be executed
	final int batch_size = ConfigurationManager.getMultithreadingBatchSize();

	public GUIFunctionality_validate(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.out = new Vector<>();
		this.generated_tcs = new ArrayList<>();
		this.instancePattern = instancePattern;
		this.gui = gui;
		this.windows_to_visit = new ArrayList<>();
		this.aw_to_click = new ArrayList<>();
		this.iw_to_fill = new ArrayList<>();
		this.sw_to_select = new ArrayList<>();
		this.semantic_pairwaise_cases = new ArrayList<>();

		this.init();

		final List<String> edges = new ArrayList<>();
		for (final Action_widget aw : instancePattern.getGui().getAction_widgets()) {
			if (instancePattern.getPAW_for_AW(aw.getId()) == null) {
				continue;
			}
			for (final Window w : instancePattern.getGui().getDynamicForwardLinks(aw.getId())) {
				final String edge = aw.getId() + " -> " + w.getId();
				edges.add(edge);
			}
			for (final Window w : instancePattern.getGui().getStaticForwardLinks(aw.getId())) {
				final String edge = aw.getId() + " -> " + w.getId();
				edges.add(edge);
			}
		}
		this.generate_run_commands(instancePattern.getSemantics());

		this.pairwise = HashBasedTable.create();

		for (int x = 0; x < edges.size(); x++) {

			final String edge1 = edges.get(x);

			final String dest1 = edge1.split(" -> ")[1];
			final String aw1 = edge1.split(" -> ")[0];

			for (int y = x; y < edges.size(); y++) {

				final String edge2 = edges.get(y);

				final String dest2 = edge2.split(" -> ")[1];
				final String aw2 = edge2.split(" -> ")[0];

				String run = "run {System and (some t1,t2: Time | t2 in T/nexts[t1] and #Track.op.(t1) = 1 and Track.op.(t1) in Click and #Track.op.(t2) = 1 and Track.op.(t2) in Click and ";
				run += "Track.op.(t1).clicked = Action_widget_" + aw1
						+ " and Track.op.(t2).clicked = Action_widget_" + aw2
						+ " and Current_window.is_in.(t1) = Window_" + dest1
						+ " and Current_window.is_in.(t2) = Window_" + dest2
						+ " and click_semantics[Action_widget_" + aw1
						+ ", T/prev[t1]] and click_semantics[Action_widget_" + aw2
						+ ", T/prev[t2]])}";
				this.pairwise.put(edge1, edge2, run);

				run = "run {System and (some t1,t2: Time | t1 in T/nexts[t2] and #Track.op.(T/next[t1]) = 1 and Track.op.(T/next[t1]) in Click and #Track.op.(T/next[t2]) = 1 and Track.op.(T/next[t2]) in Click and ";
				run += "Track.op.(T/next[t1]).clicked = Action_widget_" + aw1
						+ " and Track.op.(T/next[t2]).clicked = Action_widget_" + aw2
						+ " and Current_window.is_in.(T/next[t1]) = Window_" + dest1
						+ " and Current_window.is_in.(T/next[t2]) = Window_" + dest2
						+ " and click_semantics[Action_widget_" + aw1
						+ ", t1] and click_semantics[Action_widget_" + aw2 + ", t2])}";
				this.pairwise.put(edge2, edge1, run);

			}
		}
	}

	protected void init() {

		for (final Window w : this.instancePattern.getGui().getWindows()) {
			this.windows_to_visit.add(w.getId());
			for (final Action_widget aw : w.getActionWidgets()) {
				if (this.instancePattern.getPAW_for_AW(aw.getId()) != null) {
					this.aw_to_click.add(aw.getId());
				}
			}
			for (final Input_widget iw : w.getInputWidgets()) {
				if (iw instanceof Option_input_widget) {
					final Option_input_widget oiw = (Option_input_widget) iw;
					if (oiw.getSize() == 0) {
						continue;
					}
				}
				if (this.instancePattern.getPIW_for_IW(iw.getId()) != null) {
					this.iw_to_fill.add(iw.getId());
				}
			}
			for (final Selectable_widget sw : w.getSelectableWidgets()) {
				if (this.instancePattern.getPSW_for_SW(sw.getId()) != null) {
					this.sw_to_select.add(sw.getId());
				}
			}
		}
	}

	public List<GUITestCaseResult> validate() throws Exception {

		final long beginTime = System.currentTimeMillis();

		final GUITestCaseRunner runner = new GUITestCaseRunner();
		runner.start();
		System.out.println("COVERING SEMANTIC CASES.");

		for (int x = 0; x < this.semantic_cases.size(); x++) {
			System.out.println((x + 1) + " " + this.semantic_cases.get(x));
		}

		System.out.println(this.semantic_cases.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
				+ this.batch_size + ".");

		int batch_num = 0;
		while (((batch_num * this.batch_size)) < this.semantic_cases.size()) {
			System.out.println("BATCH " + (batch_num + 1));
			this.instancePattern.getSemantics().clearRunCommands();

			for (int cont = 0; ((batch_num * this.batch_size) + cont) < this.semantic_cases.size()
					&& cont < this.batch_size; cont++) {
				final String run = this.semantic_cases.get(((batch_num * this.batch_size) + cont));
				this.instancePattern.getSemantics().addRun_command(run);
			}
			System.out.println(this.instancePattern.getSemantics());
			final List<GUITestCase> tcs = AlloyTestCaseGenerator
					.generateTestCases(this.instancePattern);
			for (final GUITestCase tc : tcs) {
				if (tc != null) {
					this.generated_tcs.add(tc);
					runner.tcs.add(tc);
				}
			}
			batch_num++;
		}

		System.out.println("COVERING NEGATIVE CASES.");

		for (int x = 0; x < this.semantic_pairwaise_cases.size(); x++) {
			System.out.println((x + 1) + " " + this.semantic_pairwaise_cases.get(x));
		}

		System.out.println(this.semantic_pairwaise_cases.size()
				+ " TESTCASES. RUNNING THEM IN BATCHES OF " + this.batch_size + ".");

		batch_num = 0;
		while (((batch_num * this.batch_size)) < this.semantic_pairwaise_cases.size()) {
			System.out.println("BATCH " + (batch_num + 1));

			this.instancePattern.getSemantics().clearRunCommands();
			for (int cont = 0; ((batch_num * this.batch_size) + cont) < this.semantic_pairwaise_cases
					.size() && cont < this.batch_size; cont++) {
				final String run = this.semantic_pairwaise_cases
						.get(((batch_num * this.batch_size) + cont));
				this.instancePattern.getSemantics().addRun_command(run);
			}
			final List<GUITestCase> tcs = AlloyTestCaseGenerator
					.generateTestCases(this.instancePattern);
			for (final GUITestCase tc : tcs) {
				if (tc != null) {
					this.generated_tcs.add(tc);
					runner.tcs.add(tc);
				}
			}
			batch_num++;
		}

		if (ConfigurationManager.getPairwiseTestcase()) {
			System.out.println("COVERING PAIRWISE.");
			batch_num = 0;
			System.out.println(this.pairwise.values().size()
					+ " TESTCASES. RUNNING THEM IN BATCHES OF " + this.batch_size + ".");

			while (true) {
				this.filterPairwise(this.generated_tcs);

				final List<String> run_commands = this.getNPairwiseTests(this.batch_size);
				if (run_commands.size() == 0) {
					break;
				}
				for (int x = 0; x < run_commands.size(); x++) {
					System.out.println((x + 1) + " " + run_commands.get(x));
				}

				System.out.println("BATCH " + (batch_num + 1));

				this.instancePattern.getSemantics().clearRunCommands();
				for (final String run : run_commands) {
					this.instancePattern.getSemantics().addRun_command(run);
				}

				final List<GUITestCase> tcs = AlloyTestCaseGenerator
						.generateTestCases(this.instancePattern);
				for (final GUITestCase tc : tcs) {
					if (tc != null) {
						this.generated_tcs.add(tc);
						runner.tcs.add(tc);
					}
				}
				batch_num++;

			}
		}

		System.out.println("COVERING REMAINING STRUCTURAL ELEMENTS.");
		// ConfigurationManager.setTestcaseLength(old_tc_size);

		final List<String> run_commands = this.getAdditionalRunCommands(this.generated_tcs);
		for (int x = 0; x < run_commands.size(); x++) {
			System.out.println((x + 1) + " " + run_commands.get(x));
		}
		System.out.println(run_commands.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
				+ this.batch_size + ".");

		batch_num = 0;
		while (((batch_num * this.batch_size)) < run_commands.size()) {
			System.out.println("BATCH " + (batch_num + 1));

			this.instancePattern.getSemantics().clearRunCommands();

			for (int cont = 0; ((batch_num * this.batch_size) + cont) < run_commands.size()
					&& cont < this.batch_size; cont++) {
				final String run = run_commands.get(((batch_num * this.batch_size) + cont));
				this.instancePattern.getSemantics().addRun_command(run);
			}
			final List<GUITestCase> tcs = AlloyTestCaseGenerator
					.generateTestCases(this.instancePattern);
			for (final GUITestCase tc : tcs) {
				if (tc != null) {

					this.generated_tcs.add(tc);
					runner.tcs.add(tc);
				}
			}
			batch_num++;
		}
		runner.can_terminate = true;
		runner.join();
		if (runner.exception != null) {
			runner.exception.printStackTrace();
			throw new Exception("Error in runner");
		}

		final long tottime = (System.currentTimeMillis() - beginTime) / 1000;
		System.out.println("VALIDATION ELAPSED TIME: " + tottime);
		return this.out;
	}

	// function that returns additional run commands to cover uncovered
	// structural elements
	private List<String> getAdditionalRunCommands(final List<GUITestCase> testcases) {

		for (final GUITestCase tc : testcases) {
			for (final GUIAction act : tc.getActions()) {
				if (act.getWindow() != null
						&& this.windows_to_visit.contains(act.getWindow().getId())) {
					this.windows_to_visit.remove(act.getWindow().getId());
				}
				if (act instanceof Click) {
					final Click click = (Click) act;
					if (this.aw_to_click.contains(click.getWidget().getId())) {
						this.aw_to_click.remove(click.getWidget().getId());
					}
				} else if (act instanceof Fill) {
					final Fill fill = (Fill) act;
					if (this.iw_to_fill.contains(fill.getWidget().getId())) {
						this.iw_to_fill.remove(fill.getWidget().getId());
					}

				} else if (act instanceof Select) {
					final Select select = (Select) act;
					if (this.sw_to_select.contains(select.getWidget().getId())) {
						this.sw_to_select.remove(select.getWidget().getId());
					}
				}
			}
		}

		final List<String> new_run_commands = new ArrayList<>();
		for (final String winid : this.windows_to_visit) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Current_window.is_in.t = Window_" + winid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String awid : this.aw_to_click) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Click and Track.op.t.clicked = Action_widget_"
					+ awid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String iwid : this.iw_to_fill) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Fill and Track.op.t.filled = Input_widget_"
					+ iwid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String swid : this.sw_to_select) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Select and Track.op.t.wid = Selectable_widget_"
					+ swid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		return new_run_commands;
	}

	protected void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

		final List<String> negative_commands = new ArrayList<>();
		final List<String> positive_commands = new ArrayList<>();

		this.semantic_pairwaise_cases = new ArrayList<>();
		this.semantic_cases = new ArrayList<>();

		final String click = "some t: Time | #Track.op.(t) = 1 and Track.op.(t) in Click and";
		final String click_edge = "click [Track.op.t.clicked, T/prev[t], t, Track.op.(t)] and";
		for (final String prec : sem.getClickSemantics().getCases().keySet()) {

			final String negative_edge = click_edge + " (" + prec
					+ ") and not(click_semantics[Track.op.(t).clicked, T/prev[t]])";
			final String positive_edge = click_edge + " (" + prec
					+ ") and (click_semantics[Track.op.(t).clicked, T/prev[t]])";
			positive_commands.add(positive_edge);

			final String pred = click + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getClickSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				this.semantic_cases.add(run_command);
				continue;
			}
			negative_commands.add(negative_edge);

			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} }";
				this.semantic_cases.add(run_command);

			}

		}

		final String fill = "some t: Time | #Track.op.(T/next[t]) = 1 and Track.op.(T/next[t]) in Fill and";
		final String fill_edge = "fill [Track.op.(T/next[t]).filled, t, T/next[t], Track.op.(T/next[t]).with, Track.op.(T/next[t])] and";

		for (final String prec : sem.getFillSemantics().getCases().keySet()) {

			final String negative_edge = fill_edge
					+ " ("
					+ prec
					+ ") and not(fill_semantics[Track.op.(T/next[t]).filled, t, Track.op.(T/next[t]).with])";
			final String positive_edge = fill_edge
					+ " ("
					+ prec
					+ ") and (fill_semantics[Track.op.(T/next[t]).filled, t, Track.op.(T/next[t]).with])";
			positive_commands.add(positive_edge);

			final String pred = fill + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getFillSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				this.semantic_cases.add(run_command);
				continue;
			}
			negative_commands.add(negative_edge);

			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} }";

				this.semantic_cases.add(run_command);

			}
		}

		final String select = "some t: Time | #Track.op.(T/next[t]) = 1 and Track.op.(T/next[t]) in Select and";
		final String select_edge = "select [Track.op.(T/next[t]).wid, t, T/next[t], Track.op.(T/next[t]).selected_o, Track.op.(T/next[t])] and";

		for (final String prec : sem.getSelectSemantics().getCases().keySet()) {

			final String negative_edge = select_edge
					+ " ("
					+ prec
					+ ") and not(select_semantics[Track.op.(T/next[t]).wid, t, Track.op.(T/next[t]).selected_o])";
			final String positive_edge = select_edge
					+ " ("
					+ prec
					+ ") and (select_semantics[Track.op.(T/next[t]).wid, t, Track.op.(T/next[t]).selected_o])";
			positive_commands.add(positive_edge);

			final String pred = select + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getSelectSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				this.semantic_cases.add(run_command);
				continue;
			}
			negative_commands.add(negative_edge);

			final double n = Math.pow(2, cases.size());

			for (int cont = 0; cont < n; cont++) {
				String binary = Integer.toBinaryString(cont);
				for (int c = 0; c < (cases.size() - binary.length()); c++) {
					binary = "0" + binary;
				}
				String sem_pred = "";
				for (int cont2 = cases.size() - 1; cont2 >= 0; cont2--) {
					if (cont2 > (binary.length() - 1) || binary.charAt(cont2) == '0') {
						sem_pred = sem_pred + "not (" + cases.get(cont2) + ")";
					} else {
						sem_pred = sem_pred + cases.get(cont2);
					}
					if (cont2 > 0) {
						sem_pred = sem_pred + " and ";
					}
				}
				final String run_command = "run {System and {" + pred + sem_pred + ")} }";

				this.semantic_cases.add(run_command);
			}
		}

		final List<String> edges = new ArrayList<>();
		for (final Action_widget aw : this.instancePattern.getGui().getAction_widgets()) {
			if (this.instancePattern.getPAW_for_AW(aw.getId()) == null) {
				continue;
			}
			for (final Window w : this.instancePattern.getGui().getDynamicForwardLinks(aw.getId())) {
				final String edge = aw.getId() + " -> " + w.getId();
				edges.add(edge);
			}
		}

		for (int x = 0; x < negative_commands.size(); x++) {
			for (int y = 0; y < positive_commands.size(); y++) {

				final String edge1 = negative_commands.get(x);
				String edge2 = positive_commands.get(y);
				edge2 = edge2.replace("[t]", "[t2]");
				edge2 = edge2.replace("(t)", "(t2)");
				edge2 = edge2.replace(".t)", ".t2)");
				edge2 = edge2.replace("(t.", "(t2.");
				edge2 = edge2.replace(" t ", " t2 ");
				edge2 = edge2.replace(".t ", ".t2 ");
				edge2 = edge2.replace(".t.", ".t2.");
				edge2 = edge2.replace(" t]", " t2]");
				edge2 = edge2.replace(", t,", ", t2,");

				final String run1 = "run {System and {some t,t2: Time | t2 in T/nexts[t] and "
						+ edge1 + " and " + edge2 + "}}";
				final String run2 = "run {System and {some t,t2: Time | t in T/nexts[t2] and "
						+ edge1 + " and " + edge2 + "}}";
				this.semantic_pairwaise_cases.add(run1);
				this.semantic_pairwaise_cases.add(run2);
			}
		}
	}

	private void filterPairwise(final List<GUITestCase> tcs) {

		for (final GUITestCase tc : tcs) {
			final List<String> covered_edges = new ArrayList<>();
			for (final GUIAction act : tc.getActions()) {
				if (!(act instanceof Click)) {
					continue;
				}
				final Click c = (Click) act;
				final String aw = c.getWidget().getId();
				final String sw = c.getWindow().getId();
				final String dw = c.getOracle().getId();
				covered_edges.add(aw + " -> " + dw);
				if (sw.equals(dw)) {
					covered_edges.add("!" + aw);

				}
			}
			for (int x = 0; x < covered_edges.size(); x++) {
				final String edge1 = covered_edges.get(x);
				for (int y = x + 1; y < covered_edges.size(); y++) {
					final String edge2 = covered_edges.get(y);
					this.pairwise.remove(edge1, edge2);
					// this.pairwise.remove(edge2, edge1);
				}
			}
		}
	}

	private List<String> getNPairwiseTests(final int n) throws Exception {

		final List<String> to_remove = new ArrayList<>();
		final List<String> out = new ArrayList<>();
		loop: for (final String c : this.pairwise.columnKeySet()) {
			for (final String r : this.pairwise.rowKeySet()) {
				if (this.pairwise.get(r, c) != null) {
					out.add(this.pairwise.get(r, c));
					to_remove.add(r + " #### " + c);
					if (out.size() == n) {
						break loop;
					}
				}
			}
		}
		for (final String rem : to_remove) {
			// System.out.println(rem.split(" ### ")[0]);
			// System.out.println(rem.split(" ### ")[1]);

			if (this.pairwise.remove(rem.split(" #### ")[0], rem.split(" #### ")[1]) == null) {
				throw new Exception("GUIFuncitonality_validate: error in getNPairwiseTests.");
			}
		}
		return out;
	}

	public class GUITestCaseRunner extends Thread {

		public Queue<GUITestCase> tcs;
		public boolean can_terminate;
		public Exception exception;

		public GUITestCaseRunner() {

			this.tcs = new ConcurrentLinkedQueue<>();
			this.can_terminate = false;
			this.exception = null;
		}

		@Override
		public void run() {

			while (true) {
				try {
					final GUITestCase obj = this.tcs.poll();
					if (obj == null) {
						if (this.can_terminate) {

							// the queue is empty and the termination signal is
							// arrived
							break;
						} else {
							Thread.sleep(1000);
							continue;
						}
					}
					final TestCaseRunner runner = new TestCaseRunner(
							GUIFunctionality_validate.this.gui);
					System.out.println("TC " + obj.getActions().size());
					final GUITestCaseResult res = runner.runTestCase(obj);

					GUIFunctionality_validate.this.out.add(res);
				} catch (final Exception e) {
					// e.printStackTrace();
					this.exception = e;
					return;
				}
			}
		}
	}
}
