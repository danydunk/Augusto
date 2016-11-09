package test.gui.semantic.testcase;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import usi.gui.GUIParser;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.testcase.GUITestCaseParser;
import usi.testcase.GUITestCaseWriter;
import usi.testcase.structure.Click;
import usi.testcase.structure.Fill;
import usi.testcase.structure.GUIAction;
import usi.testcase.structure.GUITestCase;
import usi.testcase.structure.Select;
import usi.xml.XMLUtil;

public class GUITestCaseReadWriteTest {

	@Test
	public void test1() throws Exception {

		Document doc = XMLUtil.read(new File("./files/for_test/xml/GUI.xml").getAbsolutePath());
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
		final GUITestCase tc = new GUITestCase(null, acts, "test");
		doc = GUITestCaseWriter.writeGUITestCase(tc);
		final GUITestCase tc2 = GUITestCaseParser.parse(doc);
		assertEquals(tc.getRunCommand(), tc2.getRunCommand());
		assertEquals(tc.getActions().size(), tc2.getActions().size());
	}

	@Test
	public void test2() throws Exception {

		Document doc = XMLUtil.read(new File("./files/for_test/xml/upm-full_newripper.xml")
				.getAbsolutePath());
		Assert.assertNotNull(doc);
		final GUI gui = GUIParser.parse(doc);
		final Click click = new Click(gui.getWindow("w1"), null, (Action_widget) gui
				.getWindow("w1").getWidget("aw1"));

		final List<GUIAction> acts = new ArrayList<>();
		acts.add(click);

		final GUITestCase tc = new GUITestCase(null, acts, "");
		doc = GUITestCaseWriter.writeGUITestCase(tc);
		usi.xml.XMLUtil.save("./tc.xml", doc);

	}

}
