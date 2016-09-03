package usi.gui.functionality;

import java.util.ArrayList;
import java.util.List;

import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.GUITestCaseResult;
import usi.gui.semantic.testcase.Go;
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

	public GUIFunctionality_validate(final Instance_GUI_pattern instancePattern, final GUI gui) {

		this.instancePattern = instancePattern;
		this.gui = gui;
	}

	public void validate() throws Exception {

		this.instancePattern.getSemantics().generate_run_commands();
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(this.instancePattern);
		final List<GUITestCase> testcases = generator.generateTestCases();

		final List<String> windows_to_visit = new ArrayList<>();
		final List<String> aw_to_click = new ArrayList<>();
		final List<String> iw_to_fill = new ArrayList<>();
		final List<String> sw_to_select = new ArrayList<>();
		for (final Window w : this.instancePattern.getGui().getWindows()) {
			windows_to_visit.add(w.getId());
			for (final Action_widget aw : w.getActionWidgets()) {
				aw_to_click.add(aw.getId());
			}
			for (final Input_widget iw : w.getInputWidgets()) {
				iw_to_fill.add(iw.getId());
			}
			for (final Selectable_widget sw : w.getSelectableWidgets()) {
				sw_to_select.add(sw.getId());
			}
		}

		for (final GUITestCase tc : testcases) {
			for (final GUIAction act : tc.getActions()) {
				if (act.getWindow() != null && windows_to_visit.contains(act.getWindow().getId())) {
					windows_to_visit.remove(act.getWindow().getId());
				}
				if (act instanceof Go) {
					final Go go = (Go) act;
					if (windows_to_visit.contains(go.getWindow().getId())) {
						windows_to_visit.remove(go.getWindow().getId());
					}
				} else if (act instanceof Click) {
					final Click click = (Click) act;
					if (aw_to_click.contains(click.getWidget().getId())) {
						aw_to_click.remove(click.getWidget().getId());
					}
				} else if (act instanceof Select) {
					final Fill fill = (Fill) act;
					if (iw_to_fill.contains(fill.getWidget().getId())) {
						iw_to_fill.remove(fill.getWidget().getId());
					}

				} else if (act instanceof Fill) {
					final Select select = (Select) act;
					if (sw_to_select.contains(select.getWidget().getId())) {
						sw_to_select.remove(select.getWidget().getId());
					}
				}
			}
		}

		final List<String> new_run_commands = new ArrayList<>();
		for (final String winid : windows_to_visit) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Current_window.is_in.t = Window_" + winid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String awid : aw_to_click) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Click and Track.op.t.clicked = Action_widget_"
					+ awid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String iwid : iw_to_fill) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Fill and Track.op.t.filled = Input_widget_"
					+ iwid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		for (final String swid : sw_to_select) {
			String new_run = "run{System and ";
			new_run += "(some t:Time| Track.op.t in Select and Track.op.t.wid = Selectable_widget_"
					+ swid + ")";
			new_run += "}";
			new_run_commands.add(new_run);
		}

		final Alloy_Model mod = new Alloy_Model(
				this.instancePattern.getSemantics().getSignatures(), this.instancePattern
						.getSemantics().getFacts(), this.instancePattern.getSemantics()
						.getPredicates(), this.instancePattern.getSemantics().getFunctions(),
				this.instancePattern.getSemantics().getOpenStatements());
		for (final String run : new_run_commands) {
			mod.addRun_command(run);
		}
		final Instance_GUI_pattern clone = this.instancePattern.clone();
		clone.setSpecificSemantics(SpecificSemantics.instantiate(mod));

		final AlloyTestCaseGenerator generator2 = new AlloyTestCaseGenerator(clone);
		testcases.addAll(generator2.generateTestCases());

		final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime(),
				this.gui);

		final List<GUITestCaseResult> results = new ArrayList<>();
		for (final GUITestCase tc : testcases) {
			results.add(runner.runTestCase(tc));
		}

		final OracleChecker oracle = new OracleChecker();
		final List<String> testcases_out = new ArrayList<>();
		for (final GUITestCaseResult res : results) {
			oracle.check(res);
			testcases_out.add(oracle.getDescriptionOfLastOracleCheck());
		}
		ExperimentManager.dumpTCresult(testcases_out);
	}
}
