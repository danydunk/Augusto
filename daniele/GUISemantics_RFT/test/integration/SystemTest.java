package test.integration;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import usi.guifunctionality.GUIFunctionality_search;
import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guipattern.GUI_Pattern;
import usi.guipattern.parser.GUIPatternParser;
import usi.guisemantic.testcase.AlloyTestCaseGenerator;
import usi.guisemantic.testcase.GUIRippingTestCaseRunner;
import usi.guisemantic.testcase.GUITestCase;
import usi.guistructure.GUI;
import usi.guistructure.converter.GUIExtractionTools;
import usi.guistructure.converter.GUIStructureConverter;
import usi.guistructure.converter.interfaces.IConverter;
import usi.guistructure.parser.GUIParser;
import usi.xml.XMLUtil;

public class SystemTest {

	@Test
	public void test1() {

		System.out.println("test1");
		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmsmall-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmsmall-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./files/for_test/xml/upm-small.xml", out);

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-small.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			final Instance_GUI_pattern match = res.get(0);
			match.generateSpecificSemantics();
			final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(match);
			final List<GUITestCase> tests = generator.generateTestCases(1, 30000);

			final GUIRippingTestCaseRunner runner = new GUIRippingTestCaseRunner("./files/for_test",
					"./files/for_test/apps/upmsmall/upm-small.jar:"
							+ "./files/for_test/apps/upmsmall/commons-logging-1.1.jar:"
							+ "./files/for_test/apps/upmsmall/commons-codec-1.3.jar:"
							+ "./files/for_test/apps/upmsmall/applejavaextensions-1.4.jar:"
							+ "./files/for_test/apps/upmsmall/bcprov-jdk14-145.jar",
					"com._17od.upm.gui.MainWindow", "./files/for_test/xml/guiripping-upmsmall-EFG2.xml",
					"./files/for_test/xml/guiripping-upmsmall-GUI.xml");
			// {
			//
			// @Override
			// void createFiles() {
			//
			// this.efg_path =
			// "./files/for_test/xml/guiripping-upmsmall-EFG.xml";
			// this.gui_path =
			// "./files/for_test/xml/guiripping-upmsmall-GUI.xml";
			// }
			// };

			for (final GUITestCase test : tests) {
				runner.runGUITestCase(test);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
