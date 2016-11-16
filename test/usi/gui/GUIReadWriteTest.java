package test.usi.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import src.usi.gui.GUIParser;
import src.usi.gui.GUIWriter;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Window;
import src.usi.xml.XMLUtil;

public class GUIReadWriteTest {

	@Test
	public void testParserGUI1() throws Exception {

		Document doc = XMLUtil.read(GUIReadWriteTest.class
				.getResourceAsStream("/files/for_test/xml/GUI.xml"));
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

		final List<Action_widget> aws = new ArrayList<>();
		for (final Window w : gui.getWindows()) {
			aws.addAll(w.getActionWidgets());
		}

		for (final Action_widget aw : aws) {
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
		final List<Input_widget> iws = new ArrayList<>();
		for (final Window w : gui.getWindows()) {
			iws.addAll(w.getInputWidgets());
		}

		for (final Input_widget iw : iws) {
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
		final Collection<Window> wc = gui.getStaticForwardLinks(aw1.getId());
		assertEquals("Number of Edges", 1, wc.size());
		assertEquals("To windpws", "w2", wc.iterator().next().getId());

		// No edge from AW3
		assertNotNull(aw3);
		final Collection<Window> wc3 = gui.getStaticForwardLinks(aw3.getId());
		assertTrue(wc3.isEmpty());

		// No edge from AW4
		assertNotNull(aw4);
		final Collection<Window> wc4 = gui.getStaticForwardLinks(aw4.getId());
		assertTrue(wc4.isEmpty());

		doc = GUIWriter.writeGUI(gui);
		final GUI gui2 = GUIParser.parse(doc);
		assertEquals(gui.getWindows().size(), gui2.getWindows().size());
		assertEquals(gui.getAction_widgets().size(), gui2.getAction_widgets().size());
		assertEquals(gui.getInput_widgets().size(), gui2.getInput_widgets().size());
		assertEquals(gui.getSelectable_widgets().size(), gui2.getSelectable_widgets().size());
	}

}
