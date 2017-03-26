package test.rft;

import resources.test.rft.ApplicationHelperTestHelper;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class ApplicationHelperTest extends ApplicationHelperTestHelper {

	/**
	 * Script Name : <b>ApplicationHelperTest</b> Generated : <b>Nov 23, 2016
	 * 12:48:46 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/11/23
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		if (args.length == 1) {
			PathsManager.setProjectRoot(args[0].toString());
		}
		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "\\files\\for_test\\config\\upm_notempty.properties");
		ExperimentManager.init();
		final ApplicationHelper app = ApplicationHelper.getInstance();
		app.startApplication();
		if (!app.isRunning()) {
			throw new Exception();
		}
		Thread.sleep(1000);
		app.closeApplication();
		if (app.isRunning()) {
			throw new Exception();
		}
		Thread.sleep(1000);
		app.startApplication();
		if (!app.isRunning()) {
			throw new Exception();
		}
		Thread.sleep(1000);
		app.forceClose();
		if (app.isRunning()) {
			throw new Exception();
		}
		ExperimentManager.cleanUP();

	}
}
