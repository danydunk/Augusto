package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import usi.guifunctionality.GUIFunctionality_search;
import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guifunctionality.mapping.Instance_window;
import usi.guipattern.GUI_Pattern;
import usi.guipattern.parser.GUIPatternParser;
import usi.guistructure.GUI;
import usi.guistructure.converter.GUIExtractionTools;
import usi.guistructure.converter.GUIStructureConverter;
import usi.guistructure.converter.interfaces.IConverter;
import usi.guistructure.parser.GUIParser;
import usi.xml.XMLUtil;

public class PatternRecognitionTest {

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

			// int cont = 0;
			// for(Instance_GUI_pattern gg : res) {
			// System.out.println("found "+cont+"
			// size="+gg.getWindows().size());
			// for(Instance_window www : gg.getWindows())
			// System.out.println(www.getInstance().getId()+" -
			// "+www.getPattern().getId());
			// //assertEquals(2, gg.getWindows().size());
			//
			// cont++;
			// }

			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	// this test can sometimes find a true positive or a false positive
	@Test
	public void test2() {

		System.out.println("test2");
		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./files/for_test/xml/upm-full.xml", out);

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-full.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out.println(www.getInstance().getId() + " - " + www.getPattern().getId());
				}

				// assertEquals(2, gg.getWindows().size());

				cont++;
			}

			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w9":
					ww1 = ww;
					break;
				case "w8":
					ww2 = ww;
					break;
				case "w1":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null || ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {

		System.out.println("test3");
		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-buddi-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-buddi-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./files/for_test/xml/buddi.xml", out);

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/buddi.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out.println(www.getInstance().getId() + " - " + www.getPattern().getId());
				}
				// assertEquals(2, gg.getWindows().size());
				cont++;
			}

			assertEquals(3, res.size());
			for (final Instance_GUI_pattern gg : res) {
				assertEquals(2, gg.getWindows().size());
			}
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	// 1 out of 1 correct
	@Test
	public void test1_labelfree() {

		System.out.println("test1_labelfree");

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
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern_labelfree.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-small.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out.println(www.getInstance().getId() + " - " + www.getPattern().getId());
					// assertEquals(2, gg.getWindows().size());
				}

				cont++;
			}

			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	// 1 true positive and 1 false positive
	@Test
	public void test2_labelfree() {

		System.out.println("test2_labelfree");

		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-upmfull-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./files/for_test/xml/upm-full.xml", out);

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern_labelfree.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-full.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out.println(www.getInstance().getId() + " - " + www.getPattern().getId());
					// assertEquals(2, gg.getWindows().size());
				}

				cont++;
			}

			// assertEquals(1, res.size());
			// assertEquals(2, res.get(0).getWindows().size());
			//
			//
			// Instance_window ww1 = null;
			// Instance_window ww2 = null;
			//
			// for(Instance_window ww : res.get(0).getWindows()) {
			// switch(ww.getInstance().getId()) {
			// case "w9":
			// ww1 = ww;
			// break;
			// case "w8":
			// ww2 = ww;
			// break;
			// }
			// }
			// assertTrue(ww1 != null);
			// assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	// 4 true positive, (1 true positive cannot be found with the labels)
	@Test
	public void test3_labelfree() {

		System.out.println("test3_labelfree");
		try {
			final Document g = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-buddi-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./files/for_test/xml/guiripping-buddi-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./files/for_test/xml/buddi.xml", out);

			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern_labelfree.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/buddi.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			int cont = 0;
			for (final Instance_GUI_pattern gg : res) {
				System.out.println("found " + cont + " size=" + gg.getWindows().size());
				for (final Instance_window www : gg.getWindows()) {
					System.out.println(www.getInstance().getId() + " - " + www.getPattern().getId());
					// assertEquals(2, gg.getWindows().size());
				}

				cont++;
			}

			// assertEquals(4, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
