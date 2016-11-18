package test.rft;

import java.util.List;

import resources.test.rft.Initial_actions_testHelper;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Window;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Initial_actions_test extends Initial_actions_testHelper {

	/**
	 * Script Name : <b>Initial_actions_test</b> Generated : <b>Nov 7, 2016
	 * 9:31:37 AM</b> Description : Functional Test Script Original Host : WinNT
	 * Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/11/07
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		ApplicationHelper application = null;

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm_ini_actions.properties");
		ExperimentManager.init();

		application = ApplicationHelper.getInstance();
		application.startApplication();
		final GuiStateManager gui = GuiStateManager.getInstance();

		final List<Window> windows = gui.readGUI();

		if (windows.get(0).getActionWidgets().size() == 2) {
			application.closeApplication();
			throw new Exception();
		}

		application.closeApplication();

		ExperimentManager.cleanUP();
	}
}
