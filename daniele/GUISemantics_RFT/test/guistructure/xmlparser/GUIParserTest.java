package test.guistructure.xmlparser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.Window;
import usi.guistructure.xmlparser.GUIParser;
import usi.xml.XMLUtil;

public class GUIParserTest {

	@Test
	public void testParserGUI1() throws Exception {

		final Document doc = XMLUtil.read(new File("./files/for_test/xml/GUI.xml")
		.getAbsolutePath());
		Assert.assertNotNull(doc);
		final GUI gui = GUIParser.parse(doc);

		assertEquals("Number of Windows", 3, gui.getWindows().size());
		Window w1 = null;
		Window w2 = null;
		Window w3 = null;
		for (final Window w : gui.getWindows()) {
			if ("w1".equals(w.getId())) {
				w1 = w;
			}
			if ("w2".equals(w.getId())) {
				w2 = w;
			}
			if ("w3".equals(w.getId())) {
				w3 = w;
			}
		}

		Assert.assertNotNull(w1);
		Assert.assertNotNull(w2);
		Assert.assertNotNull(w3);

		// Test Window's titles
		assertEquals("Title windows", "Init", w1.getLabel());
		assertEquals("Title windows", "Form", w2.getLabel());
		assertEquals("Title windows", "Other", w3.getLabel());

		assertEquals("Container actions", 2, w1.getActionWidgets().size());
		assertEquals("Container input", 0, w1.getInputWidgets().size());

		assertEquals("Container actions", 2, w2.getActionWidgets().size());

		Action_widget aw1 = null;
		Action_widget aw2 = null;
		Action_widget aw3 = null;
		Action_widget aw4 = null;

		for (final Action_widget aw : gui.getAction_widgets()) {
			if ("aw1".equals(aw.getId())) {
				aw1 = aw;
			}
			if ("aw2".equals(aw.getId())) {
				aw2 = aw;
			}
			if ("aw3".equals(aw.getId())) {
				aw3 = aw;
			}
			if ("aw4".equals(aw.getId())) {
				aw4 = aw;
			}
		}

		Input_widget iw1 = null;

		for (final Input_widget iw : gui.getInput_widgets()) {
			if ("iw1".equals(iw.getId())) {
				iw1 = iw;
			}

		}

		// Test labels of Action and Input
		assertEquals("Label actions", "save", aw3.getLabel());
		assertEquals("Label actions", "field1", iw1.getLabel());

		// Number of input of w2
		assertEquals("Container input", 2, w2.getInputWidgets().size());

		// There is a relation from AW1 to W2
		assertNotNull(aw1);
		final Collection<Window> wc = gui.getForwardLinks(aw1);
		assertEquals("Number of Edges", 1, wc.size());
		assertEquals("To windpws", "w2", wc.iterator().next().getId());

		// No edge from AW3
		assertNotNull(aw3);
		final Collection<Window> wc3 = gui.getForwardLinks(aw3);
		assertTrue(wc3.isEmpty());

		// No edge from AW4
		assertNotNull(aw4);
		final Collection<Window> wc4 = gui.getForwardLinks(aw4);
		assertTrue(wc4.isEmpty());

	}

}
