package src.usi;

import java.io.File;
import java.io.PrintWriter;

import resources.src.usi.RunTestsuiteHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.gui.GUIParser;
import src.usi.gui.structure.GUI;
import src.usi.testcase.GUITestCaseParser;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.OracleChecker;
import src.usi.testcase.TestCaseRunner;
import src.usi.testcase.structure.GUITestCase;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class RunTestsuite extends RunTestsuiteHelper {

	/**
	 * Script Name : <b>RunTestsuite</b> Generated : <b>Jan 30, 2017 5:32:45
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2017/01/30
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		String dir = null;
		String settings = null;
		// switch (args.length) {
		// case 2:
		// // settings =
		// settings = (String) args[1];
		// // settings =
		// // "C:\\workspace\\Augusto\\results_buddi_final\\aut.properties";
		// dir = (String) args[0];
		// // dir =
		// //
		// "C:\\workspace\\Augusto\\results_buddi_final\\CRUD_match_1\\testcases";
		// break;
		//
		// default:
		// System.out.println("Error: wrong number of parameters.");
		// return;
		// }
		settings = "C:\\workspace\\Augusto\\pdfsam_results\\pdfsam_results\\results_20170804_0951\\aut.properties";
		dir = "C:\\workspace\\Augusto\\pdfsam_results\\pdfsam_results\\results_20170804_0951\\CRUD_NO_READ_match_1\\testcases";
		ConfigurationManager.load(settings);
		ExperimentManager.init();
		final GUI gui = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));

		final TestCaseRunner runner = new TestCaseRunner(gui);
		final OracleChecker checker = new OracleChecker(gui);

		final File fold = new File(dir);
		final File[] files = fold.listFiles();
		for (final File f : files) {
			if (f.isFile() && f.getName().startsWith("testcase_") && f.getName().endsWith(".xml")) {
				System.out.println("RUNNING " + f.getName());
				final GUITestCase tc = GUITestCaseParser.parse(XMLUtil.read(f.getAbsolutePath()));
				final GUITestCaseResult res = runner.runTestCase(tc);

				checker.check(res);
				final PrintWriter writer = new PrintWriter(dir + File.separator
						+ f.getName().replace(".xml", "_results.txt"), "UTF-8");
				writer.print(res.getTc().toString() + System.getProperty("line.separator")
						+ checker.getDescriptionOfLastOracleCheck());
				writer.close();
				System.out.println("FINISHED RUNNING " + f.getName());
				Thread.sleep(1000);
			}
		}
	}
}
