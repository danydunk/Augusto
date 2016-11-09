package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import usi.gui.GUIParser;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.instance.Instance_GUI_pattern;
import usi.gui.functionality.instance.Instance_window;
import usi.gui.structure.GUI;
import usi.pattern.GUIPatternParser;
import usi.pattern.structure.GUI_Pattern;
import usi.xml.XMLUtil;

public class PatternRecognitionTest {

	@Test
	public void test1() {

		System.out.println("test1");
		try {

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/crud.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-full_newripper.xml")
			.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out
					.println(www.getInstance().getId() + " - " + www.getPattern().getId());
				}
				// assertEquals(2, gg.getWindows().size());
				cont++;
			}

			assertEquals(1, res.size());
			for (final Instance_GUI_pattern gg : res) {
				assertEquals(2, gg.getWindows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {

		System.out.println("test2");
		try {

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/crud.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/buddi_newripper.xml")
			.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			assertEquals(4, res.size());

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
