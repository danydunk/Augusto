package test.rft;

import resources.test.rft.runAllHelper;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ExperimentManager;

import com.rational.test.ft.UserStoppedScriptError;
import com.rational.test.ft.script.RationalTestScriptError;

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
			System.out.println("EMPTY FILL TEST");
			new Emptyfill_test().testMain(args);
			System.out.println("APPLICATION HELPER TEST");
			new ApplicationHelperTest().testMain(args);
			System.out.println("COBERTURA TEST");
			new CoberturaTest().testMain(args);
			System.out.println("ERROR WINDOW TEST");
			new Error_window_test().testMain(args);
			System.out.println("EXECUTE ACTION TEST");
			new Execute_action().testMain(args);
			System.out.println("GO SELECT TEST");
			new GO_select_test().testMain(args);
			System.out.println("INITIAL ACTIONS TEST");
			new Initial_actions_test().testMain(args);
			System.out.println("ORACLE TEST");
			new OracleTest().testMain(args);
			// System.out.println("REFINEMENT BUDDI INVALID TEST");
			// new Refinement_buddi_crud_invalid().testMain(args);
			System.out.println("REFINEMENT BUDDI TEST");
			new Refinement_buddi_crud().testMain(args);
			System.out.println("REFINEMENT UPM TEST");
			new Refinement_upmfull_crud().testMain(args);
			System.out.println("SAVE WINDOW TEST");
			new Save_window_xml().testMain(args);
			System.out.println("TEST CASE GENERATION UPM TEST");
			new TestCaseGeneration_upm_full().testMain(args);
		} catch (final UserStoppedScriptError a) {
			error = true;
			System.out.println("RFT STOPPED");
			a.printStackTrace();
		} catch (final RationalTestScriptError a) {
			error = true;
			System.out.println("RFT ERROR");
			a.printStackTrace();
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
			ApplicationHelper application = null;
			try {
				application = ApplicationHelper.getInstance();
				if (application.isRunning()) {
					application.closeApplication();
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				if (error) {
					System.exit(-1);
				}
			}

			if (error) {
				System.exit(-1);
			}
		}
	}
}
