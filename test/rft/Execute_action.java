package test.rft;

import java.io.File;
import java.util.List;

import resources.test.rft.Execute_actionHelper;
import usi.application.ActionManager;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GuiStateManager;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Window;
import usi.testcase.structure.Click;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Execute_action extends Execute_actionHelper {

	/**
	 * Script Name : <b>Execute_action</b> Generated : <b>Jul 8, 2016 2:48:51
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/07/08
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			ApplicationHelper application = null;
			final String conf_file = "files" + File.separator + "for_test" + File.separator
					+ "config" + File.separator + "upm.properties";
			ConfigurationManager.load(conf_file);
			ExperimentManager.init();
			application = ApplicationHelper.getInstance();
			application.startApplication();
			final GuiStateManager gui = GuiStateManager.getInstance();

			List<Window> windows = gui.readGUI();

			final Action_widget aw = windows.get(0).getActionWidgets().get(0);

			final Click click = new Click(windows.get(0), null, aw);
			ActionManager.executeAction(click);
			windows = gui.readGUI();

			if (!windows.get(0).getLabel().contains("Universal Password Manager")) {
				throw new Exception();
			}
			application.closeApplication();
		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
