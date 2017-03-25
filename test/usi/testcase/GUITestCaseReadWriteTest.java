package test.usi.testcase;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.testcase.GUITestCaseParser;
import src.usi.testcase.GUITestCaseWriter;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.Fill;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import src.usi.xml.XMLUtil;

public class GUITestCaseReadWriteTest {

	@Test
	public void test1() throws Exception {

		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/GUI.xml");
		Assert.assertNotNull(doc);
		final GUI gui = GUIParser.parse(doc);
		final Click click = new Click(gui.getWindow("w1"), gui.getWindow("w2"), (Action_widget) gui
				.getWindow("w1").getWidget("aw1"));
		final Fill fill = new Fill(gui.getWindow("w2"), gui.getWindow("w2"), (Input_widget) gui
				.getWindow("w2").getWidget("iw1"), "test");
		final Select select = new Select(gui.getWindow("w2"), gui.getWindow("w2"),
				(Selectable_widget) gui.getWindow("w2").getWidget("sw1"), 1, true);
		final List<GUIAction> acts = new ArrayList<>();
		acts.add(click);
		acts.add(fill);
		acts.add(select);
		final GUITestCase tc = new GUITestCase(acts, "test");
		doc = GUITestCaseWriter.writeGUITestCase(tc);
		final GUITestCase tc2 = GUITestCaseParser.parse(doc);
		assertEquals(tc.getActions().size(), tc2.getActions().size());
	}
}
