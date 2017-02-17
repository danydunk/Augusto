package src.usi.gui.functionality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.usi.configuration.ConfigurationManager;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.semantic.FunctionalitySemantics;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.semantic.alloy.structure.Fact;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;

public class GUIFunctionality_validate {

	private final Instance_GUI_pattern instancePattern;
	private final GUI gui;
	private List<String> windows_to_visit = new ArrayList<>();
	private final List<String> aw_to_click;
	private final List<String> iw_to_fill;
	private final List<String> sw_to_select;
	private final List<GUITestCaseResult> completely_executed_tcs;
	private SpecificSemantics working_sem;
	// map that constains as key the run command to cover a edge positively or
	// negatively and as values the run commands to cover all the possibles
	// semantic cases
	private Map<String, List<String>> edges_cases;
	List<String> edges;

	// number of times a run command can be executed
	final int MAX_RUN = 1;
	final int batch_size = ConfigurationManager.getMultithreadingBatchSize();

	public GUIFunctionality_validate(final Instance_GUI_pattern instancePattern, final GUI gui)
			throws Exception {

		this.completely_executed_tcs = new ArrayList<>();
		this.instancePattern = instancePattern;
		this.gui = gui;
		this.windows_to_visit = new ArrayList<>();
		this.aw_to_click = new ArrayList<>();
		this.iw_to_fill = new ArrayList<>();
		this.sw_to_select = new ArrayList<>();
		this.init();
		this.generate_run_commands(instancePattern.getSemantics());

		this.edges = new ArrayList<>();
		for (final Action_widget aw : instancePattern.getGui().getAction_widgets()) {
			if (instancePattern.getPAW_for_AW(aw.getId()) == null) {
				continue;
			}
			for (final Window w : instancePattern.getGui().getDynamicForwardLinks(aw.getId())) {
				final String edge = aw.getId() + " -> " + w.getId();
				this.edges.add(edge);
			}
			for (final Window w : instancePattern.getGui().getStaticForwardLinks(aw.getId())) {
				final String edge = aw.getId() + " -> " + w.getId();
				this.edges.add(edge);
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

	private List<GUITestCaseResult> runTestCases(final List<GUITestCase> testcases)
			throws Exception {

		final TestCaseRunner runner = new TestCaseRunner(this.gui);
		final List<GUITestCaseResult> results = new ArrayList<>();
		for (final GUITestCase tc : testcases) {
			final GUITestCaseResult res = runner.runTestCase(tc);
			// final GUITestCaseResult res2 =
			// this.instancePattern.updateTCResult(res);
			// if (res2 != null) {
			// res = res2;
			// }
			results.add(res);
		}
		return results;
	}

	private List<GUITestCaseResult> execute() throws Exception {

		final List<GUITestCaseResult> out = new ArrayList<>();

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
			List<GUITestCase> testcases = generator.generateTestCases();

			final List<GUITestCase> testcases_filtered = new ArrayList<>();
			final List<GUITestCaseResult> results = new ArrayList<>();

			// we dont need the alloy result
			final List<GUITestCase> tests = new ArrayList<>();
			for (final GUITestCase tc : testcases) {
				if (tc != null) {
					final GUITestCase tc2 = new GUITestCase(null, tc.getActions(),
							tc.getRunCommand());
					tests.add(tc2);
				}

			}
			testcases = tests;

			// we filter out the already run test cases
			for (final GUITestCase tc : testcases) {
				final GUITestCaseResult res = this.wasTestCasePreviouslyExecuted(tc);
				if (res != null) {
					results.add(res);
				} else {
					testcases_filtered.add(tc);
				}
			}
			final List<GUITestCaseResult> r = this.runTestCases(testcases_filtered);
			results.addAll(r);
			out.addAll(r);
			final List<GUITestCaseResult> to_rerun = new ArrayList<>();

			for (final GUITestCaseResult res : results) {
				if (res.getActions_executed().size() < res.getTc().getActions().size()) {
					// if the testcase is not run completely
					to_rerun.add(res);
					// we don't need the result
					final GUITestCase tc = new GUITestCase(null, res.getActions_executed(), res
							.getTc().getRunCommand());
					final GUITestCaseResult new_res = new GUITestCaseResult(tc,
							res.getActions_executed(), res.getResults(),
							res.getActions_actually_executed());
					// working_sem_bis = SpecificSemantics.instantiate(AlloyUtil
					// .getTCaseModelOpposite(working_sem_bis,
					// res.getTc().getActions()));
					this.completely_executed_tcs.add(new_res);

				} else {
					this.completely_executed_tcs.add(res);
				}

			}
			if (to_rerun.size() == 0) {
				break;
			}
			System.out.println(to_rerun.size() + " TESTCASES WERE NOT RUN COMPLETELY.");

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

	public List<GUITestCaseResult> validate() throws Exception {

		// we add a fact to filter redundant actions
		final List<Fact> facts = this.instancePattern.getSemantics().getFacts();
		final Fact new_fact = new Fact(
				"filter_redundant_actions",
				"no t: Time | #Track.op.t = 1 and Track.op.t in Select and Track.op.(T/prev[t]) in Select and Track.op.(T/prev[t]).wid = Track.op.t.wid"
						+ System.lineSeparator()
						+ "no t: Time | #Track.op.t = 1 and Track.op.t in Fill and Track.op.(T/prev[t]) in Fill and Track.op.(T/prev[t]).filled = Track.op.t.filled"
						+ System.lineSeparator()
						+ "no t: Time | #Track.op.t = 1 and Track.op.t in Click and Track.op.(T/prev[t]) in Click and Track.op.t.clicked = Track.op.(T/prev[t]).clicked");

		facts.add(new_fact);
		final SpecificSemantics sem = new SpecificSemantics(this.instancePattern.getSemantics()
				.getSignatures(), facts, this.instancePattern.getSemantics().getPredicates(),
				this.instancePattern.getSemantics().getFunctions(), this.instancePattern
				.getSemantics().getOpenStatements());
		this.instancePattern.setSpecificSemantics(sem);

		final List<GUITestCaseResult> out = new ArrayList<>();

		this.working_sem = new SpecificSemantics(this.instancePattern.getSemantics()
				.getSignatures(), facts, this.instancePattern.getSemantics().getPredicates(),
				this.instancePattern.getSemantics().getFunctions(), this.instancePattern
				.getSemantics().getOpenStatements());

		System.out.println("COVERING SEMANTIC CASES.");

		List<String> run_commands = this.getAllSemanticCases();
		for (int x = 0; x < run_commands.size(); x++) {
			System.out.println((x + 1) + " " + run_commands.get(x));
		}

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
			final List<GUITestCaseResult> results = this.execute();
			for (final GUITestCaseResult r : results) {
				this.working_sem = SpecificSemantics.instantiate(AlloyUtil.getTCaseModelOpposite(
						this.working_sem, r.getActions_executed()));
			}

			out.addAll(results);
			batch_num++;
		}

		if (ConfigurationManager.getPairwiseTestcase()) {
			System.out.println("COVERING PAIRWISE.");

			run_commands = new ArrayList<>();

			for (int x = 0; x < this.edges.size(); x++) {
				for (int y = x + 1; y < this.edges.size(); y++) {
					final String edge1 = this.edges.get(x);
					final String edge2 = this.edges.get(y);
					final String[] first = edge1.split(" -> ");
					final String[] second = edge2.split(" -> ");

					run_commands.addAll(this.getEdgeCommand(first[1], first[0], second[1],
							second[0]));
				}
			}
			for (int x = 0; x < run_commands.size(); x++) {
				System.out.println((x + 1) + " " + run_commands.get(x));
			}
			System.out.println(run_commands.size() + " TESTCASES. RUNNING THEM IN BATCHES OF "
					+ this.batch_size + ".");
			// we need to reduce the scope because one run goes in out of
			// memeory
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

				final List<GUITestCaseResult> results = this.execute();
				for (final GUITestCaseResult r : results) {
					this.working_sem = SpecificSemantics.instantiate(AlloyUtil
							.getTCaseModelOpposite(this.working_sem, r.getActions_executed()));
				}

				out.addAll(results);
				batch_num++;
			}
		}

		System.out.println("COVERING REMAINING STRUCTURAL ELEMENTS.");
		// ConfigurationManager.setTestcaseLength(old_tc_size);

		run_commands = this.getAdditionalRunCommands(this.completely_executed_tcs);
		for (int x = 0; x < run_commands.size(); x++) {
			System.out.println((x + 1) + " " + run_commands.get(x));
		}
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

			final List<GUITestCaseResult> results = this.execute();
			for (final GUITestCaseResult r : results) {
				this.working_sem = SpecificSemantics.instantiate(AlloyUtil.getTCaseModelOpposite(
						this.working_sem, r.getActions_executed()));
			}

			out.addAll(results);
			batch_num++;
		}

		return out;
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

	// private String combineRunCommands(final String run1, final String run2) {
	//
	// String run1_mod = run1.replace("run {System and {", "");
	// String run2_mod = run2.replace("run {System and {", "");
	// run1_mod = run1_mod.replace("} }", "");
	// run2_mod = run2_mod.replace("} }", "");
	// return "run {System and {(" + run1_mod + ") and (" + run2_mod + ")} }";
	// }

	protected void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

		this.edges_cases = new HashMap<>();

		final String click = "some t: Time | #Track.op.(T/next[t]) = 1 and Track.op.(T/next[t]) in Click and";
		final String click_edge = "some t: Time | click [Track.op.(T/next[t]).clicked, t, T/next[t], Track.op.(T/next[t])] and";
		for (final String prec : sem.getClickSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {" + click_edge + " (" + prec
					+ ") and click_semantics[Track.op.(T/next[t]).clicked, t]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {" + click_edge + " (" + prec
					+ ") and not(click_semantics[Track.op.(T/next[t]).clicked, t])} }";
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

		final String fill = "some t: Time | #Track.op.(T/next[t]) = 1 and Track.op.(T/next[t]) in Fill and";
		final String fill_edge = "some t: Time | fill [Track.op.(T/next[t]).filled, t, T/next[t], Track.op.(T/next[t]).with, Track.op.(T/next[t])] and";

		for (final String prec : sem.getFillSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {"
					+ fill_edge
					+ " ("
					+ prec
					+ ") and fill_semantics[Track.op.(T/next[t]).filled, t, Track.op.(T/next[t]).with]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {"
					+ fill_edge
					+ " ("
					+ prec
					+ ") and not(fill_semantics[Track.op.(T/next[t]).filled, t, Track.op.(T/next[t]).with])} }";
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

		final String select = "some t: Time | #Track.op.(T/next[t]) = 1 and Track.op.(T/next[t]) in Select and";
		final String select_edge = "some t: Time | select [Track.op.(T/next[t]).wid, t, T/next[t], Track.op.(T/next[t]).selected_o, Track.op.(T/next[t])] and";

		for (final String prec : sem.getSelectSemantics().getCases().keySet()) {

			final String positive_edge = "run {System and {"
					+ select_edge
					+ " ("
					+ prec
					+ ") and select_semantics[Track.op.(T/next[t]).wid, t, Track.op.(T/next[t]).selected_o]} }";
			final List<String> positive_semantic_cases = new ArrayList<>();
			final String negative_edge = "run {System and {"
					+ select_edge
					+ " ("
					+ prec
					+ ") and not(select_semantics[Track.op.(T/next[t]).wid, t, Track.op.(T/next[t]).selected_o])} }";
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

	// private String getEdgeFromSemanticCase(final String sem_case) {
	//
	// for (final String key : this.edges_cases.keySet()) {
	// if (this.edges_cases.get(key).contains(sem_case)) {
	// return key;
	// }
	// }
	// return null;
	// }

	private List<String> getEdgeCommand(final String dest1, final String aw1, final String dest2,
			final String aw2) throws Exception {

		final boolean first = this.instancePattern.getGui().isDynamicEdge(aw1, dest1);
		final boolean second = this.instancePattern.getGui().isDynamicEdge(aw2, dest2);

		final List<String> out = new ArrayList<>();
		String run = "run {System and (some t1,t2: Time | #Track.op.(T/next[t1]) = 1 and Track.op.(T/next[t1]) in Click and #Track.op.(T/next[t2]) = 1 and Track.op.(T/next[t2]) in Click and ";
		run += "Track.op.(T/next[t1]).clicked = Action_widget_" + aw1
				+ " and Track.op.(T/next[t2]).clicked = Action_widget_" + aw2
				+ " and Current_window.is_in.(T/next[t1]) = Window_" + dest1
				+ " and Current_window.is_in.(T/next[t2]) = Window_" + dest2
				+ " and click_semantics[Action_widget_" + aw1
				+ ", t1] and click_semantics[Action_widget_" + aw2 + ", t2])}";
		out.add(run);

		if (first) {
			run = "run {System and (some t1,t2: Time | #Track.op.(T/next[t1]) = 1 and Track.op.(T/next[t1]) in Click and #Track.op.(T/next[t2]) = 1 and Track.op.(T/next[t2]) in Click and ";
			run += "Track.op.(T/next[t1]).clicked = Action_widget_" + aw1
					+ " and Track.op.(T/next[t2]).clicked = Action_widget_" + aw2
					+ " and Current_window.is_in.(T/next[t2]) = Window_" + dest2
					+ " and not (click_semantics[Action_widget_" + aw1
					+ ", t1]) and click_semantics[Action_widget_" + aw2 + ", t2])}";
			out.add(run);
		}

		if (second) {
			run = "run {System and (some t1,t2: Time | #Track.op.(T/next[t1]) = 1 and Track.op.(T/next[t1]) in Click and #Track.op.(T/next[t2]) = 1 and Track.op.(T/next[t2]) in Click and ";
			run += "Track.op.(T/next[t1]).clicked = Action_widget_" + aw1
					+ " and Track.op.(T/next[t2]).clicked = Action_widget_" + aw2
					+ " and Current_window.is_in.(T/next[t1]) = Window_" + dest1
					+ " and click_semantics[Action_widget_" + aw1
					+ ", t1] and not(click_semantics[Action_widget_" + aw2 + ", t2]))}";
			out.add(run);
		}
		if (first && second) {
			run = "run {System and (some t1,t2: Time | #Track.op.(T/next[t1]) = 1 and Track.op.(T/next[t1]) in Click and #Track.op.(T/next[t2]) = 1 and Track.op.(T/next[t2]) in Click and ";
			run += "Track.op.(T/next[t1]).clicked = Action_widget_" + aw1
					+ " and Track.op.(T/next[t2]).clicked = Action_widget_" + aw2
					+ " and not (click_semantics[Action_widget_" + aw1
					+ ", t1]) and not(click_semantics[Action_widget_" + aw2 + ", t2]))}";
			out.add(run);
		}
		return out;
	}
}
