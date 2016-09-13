package test.rft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Error_window_testHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.xml.XMLUtil;

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
	 */
	public void testMain(final Object[] args) {

		try {
			ConfigurationManager.load("./files/for_test/config/upm_notempty.properties");
			ExperimentManager.init();

			// we load the GUI structure
			final Document doc = XMLUtil.read(new File(
					"./files/for_test/xml/upm-full_newripper.xml").getAbsolutePath());
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

			final Window form = gui.getWindow("w10");
			final Input_widget iw1 = (Input_widget) form.getWidget("iw15");
			final Fill f1 = new Fill(form, null, iw1, "test");
			acts.add(f1);

			final Action_widget aw3 = (Action_widget) form.getWidget("aw82");
			final Click c3 = new Click(form, null, aw3);
			acts.add(c3);

			final GUITestCase tc = new GUITestCase(null, acts, "run");

			final TestCaseRunner runner = new TestCaseRunner(ConfigurationManager.getSleepTime());
			runner.runTestCase(tc);

		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");

		}
	}
}
