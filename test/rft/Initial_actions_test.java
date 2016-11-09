package test.rft;

import java.io.File;
import java.util.List;

import resources.test.rft.Initial_actions_testHelper;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GuiStateManager;
import usi.gui.structure.Window;

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
	 */
	public void testMain(final Object[] args) {

		try {
			ApplicationHelper application = null;
			final String conf_file = "files" + File.separator + "for_test" + File.separator
					+ "config" + File.separator + "upm_ini_actions.properties";
			ConfigurationManager.load(conf_file);
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
		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
