package test.integration;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.xml.XMLUtil;

public class SystemTest {

	@Test
	public void test1() {

		System.out.println("test1");
		try {
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
			match.getSemantics().generate_run_commands();
			final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(match, 1, 60000);

			final List<GUITestCase> tests = generator.generateTestCases();

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
