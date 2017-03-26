package test.rft;

import java.util.List;

import resources.test.rft.CoberturaTestHelper;
import src.usi.application.ActionManager;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.structure.Click;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class CoberturaTest extends CoberturaTestHelper {

	/**
	 * Script Name : <b>CoberturaTest</b> Generated : <b>Nov 25, 2016 12:16:17
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/11/25
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		if (args.length == 1) {
			PathsManager.setProjectRoot(args[0].toString());
		}
		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm.properties");
		ExperimentManager.init();

		ApplicationHelper application = null;

		application = ApplicationHelper.getInstance();
		application.startApplication();
		final GuiStateManager gui = GuiStateManager.getInstance();

		final List<Window> windows = gui.readGUI();

		final Action_widget aw = windows.get(0).getActionWidgets().get(0);

		final Click click = new Click(windows.get(0), null, aw);
		ActionManager.executeAction(click);

		application.closeApplication();
		final double[] cov = ExperimentManager.getCoverage();
		if (cov[0] == 0 || cov[1] == 0) {
			throw new Exception();
		}
		ExperimentManager.cleanUP();
	}
}
