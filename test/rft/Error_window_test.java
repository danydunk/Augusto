package test.rft;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Error_window_testHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Error_window_test extends Error_window_testHelper {

	/**
	 * Script Name : <b>Error_window_test</b> Generated : <b>Aug 22, 2016
	 * 5:26:36 AM</b> Description : Functional Test Script Original Host : WinNT
	 * Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/08/22
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "\\files\\for_test\\config\\upm_notempty.properties");
		ExperimentManager.init();

		// we load the GUI structure
		final Document doc = XMLUtil.read(PathsManager.getProjectRoot()
				+ "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);
		final List<GUIAction> acts = new ArrayList<>();

		final Window pass_w = gui.getWindow("w1");
		final Action_widget aw1 = (Action_widget) pass_w.getWidget("aw1");
		final Click c1 = new Click(pass_w, null, aw1);
		acts.add(c1);

		final Window initial = gui.getWindow("w2");
		final Action_widget aw2 = (Action_widget) initial.getWidget("aw20");
		final Click c2 = new Click(initial, null, aw2);
		acts.add(c2);

		final Window form = gui.getWindow("w8");
		final Input_widget iw1 = (Input_widget) form.getWidget("iw17");
		final Fill f1 = new Fill(form, null, iw1, "test");
		acts.add(f1);

		final Action_widget aw3 = (Action_widget) form.getWidget("aw62");
		final Click c3 = new Click(form, null, aw3);
		acts.add(c3);

		final GUITestCase tc = new GUITestCase(null, acts, "run");

		final TestCaseRunner runner = new TestCaseRunner(gui);
		final GUITestCaseResult res = runner.runTestCase(tc);

		ExperimentManager.cleanUP();

		if (!res.getResults().get(res.getResults().size() - 1).getId().equals(form.getId())) {
			throw new Exception();
		}

	}
}
