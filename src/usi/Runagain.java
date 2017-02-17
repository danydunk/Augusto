package src.usi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.w3c.dom.Document;

import resources.src.usi.RunagainHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_validate;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_GUI_patternParser;
import src.usi.gui.structure.GUI;
import src.usi.testcase.GUITestCaseResult;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Runagain extends RunagainHelper {

	public static void main(final Object[] args) throws Exception {

		final Main r = new Main();
		r.testMain(args);
	}

	/**
	 * Script Name : <b>Main</b> Generated : <b>Jul 7, 2016 7:55:06 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/07/07
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		ConfigurationManager.load();
		ExperimentManager.init();
		final String out_folder = "./results_20170129_0813/";
		// we set the stdout as a log file
		final PrintStream generallog = new PrintStream(new FileOutputStream(out_folder + "out.log"));
		final PrintStream generalerr = new PrintStream(new FileOutputStream(out_folder + "out.err"));
		try {
			System.setOut(generallog);
			System.setErr(generalerr);

			// ExperimentManager.moveFile(ConfigurationManager.getLoadedFilePath(),
			// out_folder);
			// GUI
			final GUI gui = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));
			// ExperimentManager.moveFile(ConfigurationManager.getGUIFile(),
			// out_folder);

			final String match_folder = out_folder + "/" + "AUTH_match_1/";

			final PrintStream vallog = new PrintStream(new FileOutputStream(match_folder
					+ "testcases/validator.log"));
			System.setOut(vallog);
			final Document doc = XMLUtil.read(match_folder + "/match.xml");
			final Instance_GUI_pattern instance = Instance_GUI_patternParser.parse(doc);
			;

			final GUIFunctionality_validate validator = new GUIFunctionality_validate(instance, gui);
			final List<GUITestCaseResult> results = validator.validate();
			ExperimentManager.dumpTCresults(match_folder, results, gui);
			vallog.close();

			System.setOut(generallog);
			// validation coverage
			final double[] cov_after = ExperimentManager.getCoverage();
			System.out.println("COVERAGE ACHIEVED DURING VALIDATION:" + System.lineSeparator()
					+ "statement " + cov_after[0] + ", branch " + cov_after[1]);
			generallog.close();
			ExperimentManager.moveFile(System.getProperty("user.dir") + File.separator + "aut.log",
					out_folder);
			ExperimentManager.cleanUP();

		} catch (final Exception e) {
			System.setOut(generallog);
			System.out.println("ERROR");
			e.printStackTrace();
		} finally {
			ExperimentManager.cleanUP();
		}
	}
}
