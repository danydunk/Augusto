package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.GO_select_testHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.GUI;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class GO_select_test extends GO_select_testHelper {

	/**
	 * Script Name : <b>GO_select_test</b> Generated : <b>Aug 3, 2016 8:03:13
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/08/03
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		if (args.length == 1) {
			PathsManager.setProjectRoot(args[0].toString());
		}
		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm_notempty.properties");
		ExperimentManager.init();

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/crud.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
		final Instance_GUI_pattern r = res.get(0);
		// // manual refinement
		// final Window view = new Window("w999", "view", "class", 1, 1, 1, 1,
		// false);
		//
		// for (final Input_widget iww : gui.getWindow("w8").getInputWidgets())
		// {
		// if (iww instanceof Option_input_widget) {
		// final Option_input_widget oiw = (Option_input_widget) iww;
		// view.addWidget(new Option_input_widget(iww.getId() + "9",
		// iww.getLabel(), iww
		// .getClasss(), iww.getX(), iww.getY(), 1, 1, oiw.getSize(), oiw
		// .getSelected()));
		// } else {
		// view.addWidget(new Input_widget(iww.getId() + "9", iww.getLabel(),
		// iww.getClasss(),
		// iww.getX(), iww.getY(), 1, 1, iww.getValue()));
		// }
		// }
		//
		// final Action_widget ok = new Action_widget("aw999", "ok", "class", 1,
		// 1, 1, 1);
		// view.addWidget(ok);
		//
		// gui.addWindow(view);
		// final Instance_window iw = new
		// Instance_window(pattern.getWindow("view"), view);
		// r.addWindow(iw);
		// iw.addAW_mapping(pattern.getWindow("view").getActionWidgets().get(0),
		// view.getActionWidgets());
		// iw.addIW_mapping(pattern.getWindow("view").getInputWidgets().get(0),
		// view.getInputWidgets());
		//
		// r.getGui().addWindow(view);
		r.getGui().addDynamicEdge("aw21", "w8");
		// r.getGui().addDynamicEdge("aw23", "w999");
		// r.getGui().addDynamicEdge("aw999", "w2");
		r.getGui().addDynamicEdge("aw22", "w2");
		r.getGui().addDynamicEdge("aw62", "w2");

		r.generateSpecificSemantics();
		// final String run =
		// "run{System and (one t, t': Time, s: Select, f: Fill | T/gt[t',t] and s in Track.op.t and f in Track.op.t' and f.with in Invalid)} for 8";
		final String run2 = "run{System and (one t, t': Time, s: Select, f: Fill | T/gt[t',t] and s in Track.op.t and f in Track.op.t')} for 8";

		// r.getSemantics().addRun_command(run);
		r.getSemantics().addRun_command(run2);

		final List<GUITestCase> tests = AlloyTestCaseGenerator.generateTestCasesMinimal(r, 8);
		if (tests.size() > 1) {
			throw new Exception("");
		}

		final TestCaseRunner runner = new TestCaseRunner(gui);
		runner.runTestCase(tests.get(0));

		ExperimentManager.cleanUP();
	}
}
