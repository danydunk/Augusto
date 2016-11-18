package test.rft;

import resources.test.rft.runAllHelper;
import src.usi.configuration.ExperimentManager;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class runAll extends runAllHelper {

	/**
	 * Script Name : <b>runAll</b> Generated : <b>Nov 18, 2016 12:11:26 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/11/18
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		boolean error = false;
		try {
			new Error_window_test().testMain(args);
			new Execute_action().testMain(args);
			new GO_select_test().testMain(args);
			new Initial_actions_test().testMain(args);
			new OracleTest().testMain(args);
			new Refinement_buddi_crud().testMain(args);
			new Refinement_upmfull_crud().testMain(args);
			new Save_window_xml().testMain(args);
			new TestCaseGeneration_upm_full().testMain(args);
		} catch (final AssertionError ee) {
			error = true;
			System.out.println("ASSERTION ERROR");
			ee.printStackTrace();
		} catch (final Exception e) {
			error = true;
			System.out.println("ERROR");
			e.printStackTrace();
		} finally {
			ExperimentManager.cleanUP();
			if (error) {
				System.exit(-1);
			}
		}
	}
}
