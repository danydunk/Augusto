package usi.gui.functionality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.GUITestCaseResult;
import usi.gui.semantic.testcase.OracleChecker;
import usi.gui.semantic.testcase.Select;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class GUIFunctionality_validate {

	private final Instance_GUI_pattern instancePattern;
	private final GUI gui;
	private List<String> windows_to_visit = new ArrayList<>();
	private final List<String> aw_to_click;
	private final List<String> iw_to_fill;
	private final List<String> sw_to_select;
	private final List<GUITestCaseResult> completely_executed_tcs;
	private final List<String> covered_edges;
	private SpecificSemantics working_sem;
	// map that constains as key the run command to cover a edge positively or
	// negatively and as values the run commands to cover all the possibles
	// semantic cases
	private Map<String, List<String>> edges_cases;

	// number of times a run command can be executed
	final int MAX_RUN = 3;
	final int batch_size = 7;

	public GUIFunctionality_validate(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.covered_edges = new ArrayList<>();
		this.completely_executed_tcs = new ArrayList<>();
		this.instancePattern = instancePattern;
		this.gui = gui;
		this.windows_to_visit = new ArrayList<>();
		this.aw_to_click = new ArrayList<>();
		this.iw_to_fill = new ArrayList<>();
		this.sw_to_select = new ArrayList<>();
		this.init();
		this.generate_run_commands(instancePattern.getSemantics());
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

	private List<GUITestCaseResult> runTestCases(final List<GUITestCase> testcases)
			throws Exception {

		final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime(),
				this.gui);
		final List<GUITestCaseResult> results = new ArrayList<>();
		for (final GUITestCase tc : testcases) {

			results.add(runner.runTestCase(tc));
		}
		return results;
	}

	private List<String> execute() throws Exception {

		System.gc();
		final List<String> out = new ArrayList<>();

		SpecificSemantics working_sem_bis = new SpecificSemantics(this.working_sem.getSignatures(),
				this.working_sem.getFacts(), this.working_sem.getPredicates(),
				this.working_sem.getFunctions(), this.working_sem.getOpenStatements());

		for (final String run : this.working_sem.getRun_commands()) {
			working_sem_bis.addRun_command(run);
		}
		final Instance_GUI_pattern work_instance = this.instancePattern.clone();
		work_instance.setSpecificSemantics(working_sem_bis);

		for (int cont = 0; cont < this.MAX_RUN; cont++) {
			final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(work_instance);
			final List<GUITestCase> testcases = generator.generateTestCases();

			final List<GUITestCase> testcases_filtered = new ArrayList<>();
			final List<GUITestCaseResult> results = new ArrayList<>();

			// we filter out the already run test cases
			for (final GUITestCase tc : testcases) {
				final GUITestCaseResult res = this.wasTestCasePreviouslyExecuted(tc);
				if (res != null) {
					results.add(res);
				} else {
					testcases_filtered.add(tc);
				}
			}

			results.addAll(this.runTestCases(testcases_filtered));

			final OracleChecker oracle = new OracleChecker(this.gui);

			final List<GUITestCaseResult> to_rerun = new ArrayList<>();

			for (final GUITestCaseResult res : results) {
				if (oracle.check(res, false) == 0) {
					// if the testcase is not run completely
					to_rerun.add(res);
					// we dont need the result
					final GUITestCase tc = new GUITestCase(null, res.getActions_executed(), res
							.getTc().getRunCommand());
					final GUITestCaseResult new_res = new GUITestCaseResult(tc,
							res.getActions_executed(), res.getResults(),
							res.getActions_actually_executed());
					working_sem_bis = SpecificSemantics.instantiate(AlloyUtil
							.getTCaseModelOpposite(working_sem_bis, res.getTc().getActions()));
					this.completely_executed_tcs.add(new_res);

				} else {
					// we dont need the result
					final GUITestCase tc = new GUITestCase(null, res.getTc().getActions(), res
							.getTc().getRunCommand());
					final GUITestCaseResult new_res = new GUITestCaseResult(tc,
							res.getActions_executed(), res.getResults(),
							res.getActions_actually_executed());
					this.completely_executed_tcs.add(new_res);
				}

				final String edge = this.getEdgeFromSemanticCase(res.getTc().getRunCommand());
				if (edge != null && !this.covered_edges.contains(edge)) {
					this.covered_edges.add(edge);
				}

				out.add(this.printTCdescription(res.getTc())
						+ oracle.getDescriptionOfLastOracleCheck());
			}

			System.out.println(to_rerun.size() + " TESTCASES WERE NOT RUN COMPLETELY.");

			if (to_rerun.size() == 0) {
				break;
			}

			working_sem_bis = new SpecificSemantics(working_sem_bis.getSignatures(),
					working_sem_bis.getFacts(), working_sem_bis.getPredicates(),
					working_sem_bis.getFunctions(), working_sem_bis.getOpenStatements());
			for (final GUITestCaseResult run : to_rerun) {
				working_sem_bis.addRun_command(run.getTc().getRunCommand());
			}
			work_instance.setSpecificSemantics(working_sem_bis);
		}
		return out;
	}

	public void validate() throws Exception {

		final double[] cov_before = ExperimentManager.getCoverage();
		String coverage = "COVERAGE ACHIEVED DURING REFINEMENT:" + System.lineSeparator()
				+ "statement " + cov_before[0] + ", branch " + cov_before[1];

		ExperimentManager.resetCoverage();

		final List<Fact> facts = this.instancePattern.getSemantics().getFacts();
		// fact to eliminate final redundandt actions
		final Fact new_fact = new Fact(
				"filter_redundant_actions",
				"all t: Time | not (Track.op.t in Select and Track.op.(T/next[t]) in Select and Track.op.t.wid = Track.op.(T/next[t]).wid)"
						+ System.lineSeparator()
						+ "all t: Time | not (Track.op.t in Fill and Track.op.(T/next[t]) in Fill and Track.op.t.filled = Track.op.(T/next[t]).filled)");
		facts.add(new_fact);

		this.working_sem = new SpecificSemantics(this.instancePattern.getSemantics()
				.getSignatures(), facts, this.instancePattern.getSemantics().getPredicates(),
				this.instancePattern.getSemantics().getFunctions(), this.instancePattern
				.getSemantics().getOpenStatements());

		final List<String> testcases_out = new ArrayList<>();
		System.out.println("COVERING SEMANTIC CASES.");

		List<String> run_commands = this.getAllSemanticCases();

		System.out.println(run_commands.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
				+ this.batch_size + ".");

		int batch_num = 0;
		while (((batch_num * this.batch_size)) < run_commands.size()) {
			System.out.println("BATCH " + (batch_num + 1));
			this.working_sem = new SpecificSemantics(this.working_sem.getSignatures(),
					this.working_sem.getFacts(), this.working_sem.getPredicates(),
					this.working_sem.getFunctions(), this.working_sem.getOpenStatements());

			for (int cont = 0; ((batch_num * this.batch_size) + cont) < run_commands.size()
					&& cont < this.batch_size; cont++) {
				final String run = run_commands.get(((batch_num * this.batch_size) + cont));
				this.working_sem.addRun_command(run);
			}

			testcases_out.addAll(this.execute());
			batch_num++;
		}

		System.out.println("COVERING PAIRWISE.");
		for (final String s : this.covered_edges) {
			System.out.println(s);
		}

		run_commands = new ArrayList<>();
		// covered_edges at this point contains all the edges that can be
		// covered
		for (int x = 0; x < this.covered_edges.size(); x++) {
			for (int y = x + 1; y < this.covered_edges.size(); y++) {
				run_commands.add(this.combineRunCommands(this.covered_edges.get(x),
						this.covered_edges.get(y)));
			}
		}

		System.out.println(run_commands.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
				+ this.batch_size + ".");
		// we need to reduce the scope because one run goes in out of memeory
		// final int old_tc_size = ConfigurationManager.getTestcaseLength();
		// ConfigurationManager.setTestcaseLength(12);
		batch_num = 0;
		while (((batch_num * this.batch_size)) < run_commands.size()) {
			System.out.println("BATCH " + (batch_num + 1));
			this.working_sem = new SpecificSemantics(this.working_sem.getSignatures(),
					this.working_sem.getFacts(), this.working_sem.getPredicates(),
					this.working_sem.getFunctions(), this.working_sem.getOpenStatements());

			for (int cont = 0; ((batch_num * this.batch_size) + cont) < run_commands.size()
					&& cont < this.batch_size; cont++) {
				final String run = run_commands.get(((batch_num * this.batch_size) + cont));
				this.working_sem.addRun_command(run);
			}

			testcases_out.addAll(this.execute());
			batch_num++;
		}
		System.out.println("COVERING OTHER STRUCTURAL ELEMENTS.");
		// ConfigurationManager.setTestcaseLength(old_tc_size);

		run_commands = this.getAdditionalRunCommands(this.completely_executed_tcs);
		System.out.println(run_commands.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
				+ this.batch_size + ".");

		batch_num = 0;
		while (((batch_num * this.batch_size)) < run_commands.size()) {
			System.out.println("BATCH " + (batch_num + 1));
			this.working_sem = new SpecificSemantics(this.working_sem.getSignatures(),
					this.working_sem.getFacts(), this.working_sem.getPredicates(),
					this.working_sem.getFunctions(), this.working_sem.getOpenStatements());

			for (int cont = 0; ((batch_num * this.batch_size) + cont) < run_commands.size()
					&& cont < this.batch_size; cont++) {
				final String run = run_commands.get(((batch_num * this.batch_size) + cont));
				this.working_sem.addRun_command(run);
			}

			testcases_out.addAll(this.execute());
			batch_num++;
		}

		final double[] cov_after = ExperimentManager.getCoverage();

		coverage += System.lineSeparator() + "COVERAGE ACHIEVED DURING VALIDATION:"
				+ System.lineSeparator() + "statement " + cov_after[0] + ", branch " + cov_after[1];
		ExperimentManager.dumpTCresult(testcases_out, coverage);

	}

	// function that returns additional run commands to cover uncovered
	// structural elements
	private List<String> getAdditionalRunCommands(final List<GUITestCaseResult> testcases) {

		for (final GUITestCaseResult tcr : testcases) {
			final GUITestCase tc = tcr.getTc();
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

	private String combineRunCommands(final String run1, final String run2) {

		String run1_mod = run1.replace("run {System and {", "");
		String run2_mod = run2.replace("run {System and {", "");
		run1_mod = run1_mod.replace("} }", "");
		run2_mod = run2_mod.replace("} }", "");
		return "run {System and {(" + run1_mod + ") and (" + run2_mod + ")} }";
	}

	private String printTCdescription(final GUITestCase tc) {

		String out = "TESTCASE SIZE = " + tc.getActions().size();
		int cont = 1;
		for (final GUIAction act : tc.getActions()) {
			out += System.lineSeparator();
			out += "ACTION " + cont;
			out += System.lineSeparator();

			if (act instanceof Click) {
				out += "CLICK " + act.getWidget().getId() + " - " + act.getWidget().getLabel();
			} else if (act instanceof Fill) {
				final Fill f = (Fill) act;
				out += "FILL " + f.getWidget().getId() + " - " + act.getWidget().getDescriptor()
						+ " WITH " + f.getInput();
			} else if (act instanceof Select) {
				final Select s = (Select) act;
				out += "SELECT " + s.getWidget().getId() + " WITH " + s.getIndex();
			}
			cont++;
		}
		out += System.lineSeparator();
		return out;
	}

	protected void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

		this.edges_cases = new HashMap<>();

		final String click = "some t: Time, aw: Action_widget | Track.op.(T/next[t]) in Click and Track.op.(T/next[t]).clicked = aw and";
		final String click_edge = "some t: Time, aw: Action_widget, c: Click | click [aw, t, T/next[t], c] and";
		for (final String prec : sem.getClickSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {" + click_edge + " (" + prec
					+ ") and click_semantics[aw, t]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {" + click_edge + " (" + prec
					+ ") and not(click_semantics[aw, t])} }";
			final List<String> negative_semantic_cases = new ArrayList<>();
			this.edges_cases.put(positive_edge, positive_semantic_cases);
			this.edges_cases.put(negative_edge, negative_semantic_cases);

			final String pred = click + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getClickSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				positive_semantic_cases.add(run_command);
				continue;
			}
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
				if (cont == (n - 1)) {
					// the last one is the positive case
					positive_semantic_cases.add(run_command);
				} else {
					negative_semantic_cases.add(run_command);
				}
			}
		}

		final String fill = "some t: Time, iw: Input_widget, v: Value | Track.op.(T/next[t]) in Fill and Track.op.(T/next[t]).filled = iw and Track.op.(T/next[t]).with = v and";
		final String fill_edge = "some t: Time, iw: Input_widget, v: Value, f: Fill | fill [iw, t, T/next[t], v, f] and";

		for (final String prec : sem.getFillSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {" + fill_edge + " (" + prec
					+ ") and fill_semantics[iw, t, v]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {" + fill_edge + " (" + prec
					+ ") and not(fill_semantics[iw, t, v])} }";
			final List<String> negative_semantic_cases = new ArrayList<>();
			this.edges_cases.put(positive_edge, positive_semantic_cases);
			this.edges_cases.put(negative_edge, negative_semantic_cases);

			final String pred = fill + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getFillSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				positive_semantic_cases.add(run_command);
				continue;
			}
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
				if (cont == (n - 1)) {
					// the last one is the positive case
					positive_semantic_cases.add(run_command);
				} else {
					negative_semantic_cases.add(run_command);
				}
			}
		}

		final String select = "some t: Time, sw: Selectable_widget, o: Object | Track.op.(T/next[t]) in Select and Track.op.(T/next[t]).wid = sw and Track.op.(T/next[t]).selected = o and";
		final String select_edge = "some t: Time, sw: Selectable_widget, o: Object, s: Select | select [sw, t, T/next[t], o, s] and";

		for (final String prec : sem.getSelectSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {" + select_edge + " (" + prec
					+ ") and select_semantics[sw, t, o]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {" + select_edge + " (" + prec
					+ ") and not(select_semantics[sw, t, o])} }";
			final List<String> negative_semantic_cases = new ArrayList<>();
			this.edges_cases.put(positive_edge, positive_semantic_cases);
			this.edges_cases.put(negative_edge, negative_semantic_cases);

			final String pred = select + " (" + prec + ") and (";
			// the number of possible combinations
			final List<String> cases = sem.getSelectSemantics().getCases().get(prec);
			if (cases.size() == 1 && cases.get(0).trim().equals("2=(1+1)")) {
				final String run_command = "run {System and {" + click + " (" + prec + ")} }";
				positive_semantic_cases.add(run_command);
				continue;
			}
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
				if (cont == (n - 1)) {
					// the last one is the positive case
					positive_semantic_cases.add(run_command);
				} else {
					negative_semantic_cases.add(run_command);
				}
			}
		}
	}

	protected List<String> getAllSemanticCases() {

		final List<String> out = new ArrayList<>();
		for (final String key : this.edges_cases.keySet()) {
			out.addAll(this.edges_cases.get(key));
		}
		return out;
	}

	private GUITestCaseResult wasTestCasePreviouslyExecuted(final GUITestCase tc) {

		for (final GUITestCaseResult tc2 : this.completely_executed_tcs) {
			if (tc.isSame(tc2.getTc())) {
				return tc2;
			}
		}

		return null;
	}

	private String getEdgeFromSemanticCase(final String sem_case) {

		for (final String key : this.edges_cases.keySet()) {
			if (this.edges_cases.get(key).contains(sem_case)) {
				return key;
			}
		}
		return null;
	}
}
