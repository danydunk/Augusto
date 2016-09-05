package test.rft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import resources.test.rft.OracleTestHelper;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.semantic.testcase.GUITestCaseResult;
import usi.gui.semantic.testcase.OracleChecker;
import usi.gui.semantic.testcase.TestCaseRunner;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class OracleTest extends OracleTestHelper {

	/**
	 * Script Name : <b>OracleTest</b> Generated : <b>Aug 29, 2016 6:19:09
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/08/29
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			final OracleChecker oracle = new OracleChecker(new GUI());
			ApplicationHelper application = null;
			final String conf_file = "files" + File.separator + "for_test" + File.separator
					+ "config" + File.separator + "upm.properties";
			ConfigurationManager.load(conf_file);
			ExperimentManager.init();

			Window target = new Window("w5", "", "", 1, 1, false);
			Selectable_widget sw = new Selectable_widget("sw3", "", "", 1, 1, 0, -1);
			// Action_widget aw = new Action_widget("aw1", "", "", 1, 1);
			target.addWidget(sw);
			// target.addWidget(aw);

			application = ApplicationHelper.getInstance();
			application.startApplication();
			final GuiStateManager gui = GuiStateManager.getInstance();
			final List<Window> windows = gui.readGUI();
			application.closeApplication();

			Action_widget aw = windows.get(0).getActionWidgets().get(0);
			Click click = new Click(windows.get(0), target, aw);
			List<GUIAction> acts = new ArrayList<>();
			acts.add(click);
			GUITestCase tc = new GUITestCase(null, acts);

			final TestCaseRunner runner = new TestCaseRunner(500, new GUI());
			GUITestCaseResult res = runner.runTestCase(tc);

			if (oracle.check(res) != 1) {
				throw new Exception();
			}

			target = new Window("w8", "", "", 1, 1, false);
			sw = new Selectable_widget("sw5", "", "", 1, 1, 1, 0);
			// aw = new Action_widget("aw1", "", "", 1, 1);
			target.addWidget(sw);
			// target.addWidget(aw);

			aw = windows.get(0).getActionWidgets().get(0);
			click = new Click(windows.get(0), target, aw);
			acts = new ArrayList<>();
			acts.add(click);
			tc = new GUITestCase(null, acts);

			res = runner.runTestCase(tc);

			if (oracle.check(res) != -1) {
				throw new Exception();
			}

		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
