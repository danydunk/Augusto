package test.usi.gui.functionality.instance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_GUI_patternParser;
import src.usi.gui.functionality.instance.Instance_GUI_patternWriter;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.xml.XMLUtil;

public class Instance_GUI_pattern_ReadWriteTest {

	@Test
	public void test() throws Exception {

		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/GUI.xml");
		Assert.assertNotNull(doc);
		final GUI loadedgui = GUIParser.parse(doc);
		final GUI gui = new GUI();
		gui.addWindow(loadedgui.getWindow("w2"));
		gui.addWindow(loadedgui.getWindow("w3"));

		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/CRUD.xml");
		Assert.assertNotNull(doc);

		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		final Instance_window w = new Instance_window(pattern.getWindow("initial"),
				gui.getWindow("w2"));
		final List<Action_widget> aws = new ArrayList<>();
		aws.add((Action_widget) gui.getWindow("w2").getWidget("aw3"));
		w.addAW_mapping((Pattern_action_widget) pattern.getWindow("initial").getWidget("paw1"), aws);

		final List<Selectable_widget> sws = new ArrayList<>();
		sws.add((Selectable_widget) gui.getWindow("w2").getWidget("sw1"));
		w.addSW_mapping((Pattern_selectable_widget) pattern.getWindow("initial").getWidget("psw1"),
				sws);

		final List<Input_widget> iws = new ArrayList<>();
		iws.add((Input_widget) gui.getWindow("w3").getWidget("iw3"));

		final Instance_window w2 = new Instance_window(pattern.getWindow("form"),
				gui.getWindow("w3"));
		w2.addIW_mapping((Pattern_input_widget) pattern.getWindow("form").getWidget("piw1"), iws);

		final List<Instance_window> ws = new ArrayList<>();
		ws.add(w);
		ws.add(w2);
		final Instance_GUI_pattern inst = new Instance_GUI_pattern(gui, pattern, ws);
		inst.generateSpecificSemantics();
		doc = Instance_GUI_patternWriter.writeInstanceGUIPattern(inst);
		final Instance_GUI_pattern inst2 = Instance_GUI_patternParser.parse(doc);
		assertEquals(inst.getWindows().size(), inst2.getWindows().size());
		assertEquals(inst.getGui().getWindows().size(), inst2.getGui().getWindows().size());
		assertEquals(inst.getWindows().get(0).getAWS_for_PAW("paw1"), inst.getWindows().get(0)
				.getAWS_for_PAW("paw1"));
		assertEquals(inst.getWindows().get(0).getSWS_for_PSW("psw1"), inst.getWindows().get(0)
				.getSWS_for_PSW("psw1"));
		assertEquals(inst.getWindows().get(0).getIWS_for_PIW("piw1"), inst.getWindows().get(0)
				.getIWS_for_PIW("pIw1"));
		assertEquals(inst.getWindows().get(1).getAWS_for_PAW("paw1"), inst.getWindows().get(1)
				.getAWS_for_PAW("paw1"));
		assertEquals(inst.getWindows().get(1).getSWS_for_PSW("psw1"), inst.getWindows().get(1)
				.getSWS_for_PSW("psw1"));
		assertEquals(inst.getWindows().get(1).getIWS_for_PIW("piw1").size(),
				inst.getWindows().get(1).getIWS_for_PIW("piw1").size());
	}
}
