package test.gui.pattern.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import usi.gui.pattern.Boolean_regexp;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.xml.XMLUtil;

public class GUIPatternParserTest {

	@Test
	public void testParserGUIPattern1() throws Exception {

		final Document doc = XMLUtil
				.read(new File("./files/xml/add_pattern.xml").getAbsolutePath());
		Assert.assertNotNull(doc);

		final GUI_Pattern gui = GUIPatternParser.parse(doc);

		// TO DO: check semantics
		// Assert.assertNotNull(gui.getAlloy_metamodel());
		assertEquals("Number of Windows", 4, gui.getWindows().size());

		Pattern_window w1 = null;
		Pattern_window w2 = null;
		Pattern_window w3 = null;
		for (final Pattern_window w : gui.getWindows()) {
			if ("initial".equals(w.getId())) {
				w1 = w;
			}
			if ("form_1".equals(w.getId())) {
				w2 = w;
			}
			if ("confirmation".equals(w.getId())) {
				w3 = w;
			}
		}

		Assert.assertNotNull(w1);
		Assert.assertNotNull(w2);
		Assert.assertNotNull(w3);

		// Test Window's titles
		assertEquals("Title windows", ".*", w1.getLabel());
		assertEquals("Title windows", ".*", w2.getLabel());
		assertEquals("Title windows", ".*", w3.getLabel());

		assertEquals("Modal windows", Boolean_regexp.ANY, w1.getModal());
		assertEquals("Modal windows", Boolean_regexp.TRUE, w3.getModal());

		assertEquals("Alloy corresponding", "Initial", w1.getAlloyCorrespondence());

		assertEquals("actions", 1, w1.getActionWidgets().size());
		assertEquals("input", 0, w1.getInputWidgets().size());

		// Test labels of Action and Input
		Pattern_action_widget paw = null;
		for (final Pattern_window pww : gui.getWindows()) {
			for (final Pattern_action_widget paw2 : pww.getActionWidgets()) {
				if ("paw3".equals(paw2.getId())) {
					paw = paw2;
				}
			}
		}
		Pattern_input_widget piw = null;
		for (final Pattern_window pww : gui.getWindows()) {
			for (final Pattern_input_widget piw2 : pww.getInputWidgets()) {
				if ("piw1".equals(piw2.getId())) {
					piw = piw2;
				}
			}
		}
		assertEquals("Label actions", ".*(back|cancel|back).*", paw.getLabel());
		assertEquals("Label actions", ".*", piw.getLabel());

		// Test forward link
		final Collection<Pattern_window> wc3 = gui.getStaticForwardLinks(paw.getId());
		assertEquals(1, wc3.size());
		assertTrue(wc3.contains(w1));

		// Test backwardLinks
		final Collection<Pattern_action_widget> pawfrom1 = gui.getStaticBackwardLinks(w1.getId());
		assertEquals(1, pawfrom1.size());
		assertTrue(pawfrom1.contains(paw));

	}
}
