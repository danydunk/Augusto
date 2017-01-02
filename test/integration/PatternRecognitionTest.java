package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.GUI;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.xml.XMLUtil;

public class PatternRecognitionTest {

	@Test
	public void test1() {

		System.out.println("test1");
		try {

			// we load a gui pattern
			Document doc = XMLUtil.read(PathsManager.getProjectRoot()
					+ "/files/guipatterns/crud.xml");
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
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
			Document doc = XMLUtil.read(PathsManager.getProjectRoot()
					+ "/files/guipatterns/crud.xml");
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/buddi.xml");
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			assertEquals(5, res.size());
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

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {

		System.out.println("test3");
		try {

			// we load a gui pattern
			Document doc = XMLUtil.read(PathsManager.getProjectRoot()
					+ "/files/guipatterns/crud.xml");
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/rachota.xml");
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			assertEquals(2, res.size());
			assertEquals("sw8", res.get(0).getSWS_for_PSW("psw1").get(0).getId());
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

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
