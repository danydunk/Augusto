package test.rft;

import java.util.List;

import resources.test.rft.Execute_actionHelper;
import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GuiStateManager;
import usi.gui.structure.Action_widget;
import usi.gui.structure.Window;
import usi.guisemantic.testcase.Click;

import com.rational.test.ft.object.interfaces.RootTestObject;

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
			final String conf_file = "files" + System.getProperty("file.separator") + "for_test"
					+ System.getProperty("file.separator") + "config"
					+ System.getProperty("file.separator") + "upm.properties";
			ConfigurationManager.load(conf_file);
			ExperimentManager.init();
			application = new ApplicationHelper();
			final RootTestObject root = application.startApplication();
			final GuiStateManager gui = new GuiStateManager(root);
			List<Window> windows = gui.getCurrentWindows();
			Thread.sleep(1000);
			final Action_widget aw = windows.get(0).getActionWidgets().get(0);

			final Click click = new Click(aw, null);
			final ActionManager manager = new ActionManager(gui, 500);
			manager.executeAction(click);
			windows = gui.getCurrentWindows();
			if (!windows.get(0).getLabel().contains("New Password Database")) {
				throw new Exception();
			}
			application.closeApplication();
		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
