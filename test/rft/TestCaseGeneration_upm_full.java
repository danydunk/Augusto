package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.TestCaseGeneration_upm_fullHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
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
public class TestCaseGeneration_upm_full extends TestCaseGeneration_upm_fullHelper {

	/**
	 * Script Name : <b>TestCaseGeneration_upm_full</b> Generated : <b>Sep 5,
	 * 2016 6:48:02 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/09/05
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm_tc.properties");
		ExperimentManager.init();

		// we load the GUI structure
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/UPMmatch.xml");
		final Instance_GUI_pattern match = Instance_GUI_patternParser.parse(doc);

		if (match == null) {
			throw new Exception();
		}

		final GUIFunctionality_validate validator = new GUIFunctionality_validate(match, gui);
		final List<GUITestCaseResult> testcases = validator.validate();

		ExperimentManager.cleanUP();

		if (testcases.size() != 35) {
			throw new Exception();
		}
	}
}
