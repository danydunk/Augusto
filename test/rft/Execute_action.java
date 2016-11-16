package test.rft;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import resources.test.rft.Execute_actionHelper;
import src.usi.application.ActionManager;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.Window;
import src.usi.testcase.structure.Click;

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

			Files.copy(Execute_action.class
					.getResourceAsStream("/files/for_test/config/upm.properties"), Paths.get(System
					.getProperty("user.dir") + File.separator + "conf.properties"),
					REPLACE_EXISTING);

			ConfigurationManager.load(System.getProperty("user.dir") + File.separator
					+ "conf.properties");
			ExperimentManager.init();
			Files.delete(Paths.get(System.getProperty("user.dir") + File.separator
					+ "conf.properties"));

			ApplicationHelper application = null;

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
