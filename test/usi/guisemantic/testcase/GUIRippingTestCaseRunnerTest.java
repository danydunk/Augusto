package usi.guisemantic.testcase;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;

import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guifunctionality.mapping.Instance_window;
import usi.guipattern.Boolean_regexp;
import usi.guipattern.Cardinality;
import usi.guipattern.GUI_Pattern;
import usi.guipattern.Pattern_action_widget;
import usi.guipattern.Pattern_input_widget;
import usi.guipattern.Pattern_window;
import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.converter.GUIExtractionTools;
import usi.guistructure.converter.GUIStructureConverter;
import usi.guistructure.converter.interfaces.IConverter;
import usi.guistructure.parser.GUIParser;
import usi.xml.XMLUtil;

public class GUIRippingTestCaseRunnerTest {

	@Test
	public void test() {

		final List<GUIAction> actions = new ArrayList<>();
		try {

			// we load the GUI
			final Document g = XMLUtil
					.read(new File("./resources/for_test/xml/guiripping-upmsmall-GUI.xml").getAbsolutePath());
			final Document efg = XMLUtil
					.read(new File("./resources/for_test/xml/guiripping-upmsmall-EFG.xml").getAbsolutePath());

			final IConverter c = GUIStructureConverter.getConverter(GUIExtractionTools.GUIRipping);
			final Object[] inputs = new Object[2];
			inputs[0] = g;
			inputs[1] = efg;
			final Document out = c.convert(inputs);
			// we save the GUI model
			XMLUtil.save("./resources/for_test/xml/upm-small.xml", out);

			// we load the GUI structure
			final Document doc = XMLUtil.read(new File("./resources/for_test/xml/upm-small.xml").getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIAction act1 = new Click(new Action_widget("w1593185608", ""), null);
			final GUIAction act2 = new Click(new Action_widget("w1005214592", ""), gui.getWindows().get(0));
			final GUIAction act3 = new Fill(new Input_widget("w2007498480", "", ""), gui.getWindows().get(0), "text");
			actions.add(act1);
			actions.add(act2);
			actions.add(act3);
			final GUITestCase test = new GUITestCase(actions);

			final Instance_GUI_pattern inst = new Instance_GUI_pattern(gui, new GUI_Pattern());
			final Pattern_window pw1 = new Pattern_window("pw1", "", Cardinality.LONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_window pw2 = new Pattern_window("pw2", "", Cardinality.LONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw = new Pattern_action_widget("paw1", "", Cardinality.LONE, "");
			final Pattern_input_widget piw = new Pattern_input_widget("piw1", "", Cardinality.LONE, "", "");
			pw1.addActionWidget(paw);
			pw2.addInputWidget(piw);
			final Instance_window iw1 = new Instance_window(pw1, gui.getWindows().get(1));
			final Instance_window iw2 = new Instance_window(pw2, gui.getWindows().get(0));
			iw1.addAW_mapping(paw, gui.getWindows().get(1).getActionWidgets());
			iw2.addIW_mapping(piw, gui.getWindows().get(0).getInputWidgets());
			inst.addWindow(iw1);
			inst.addWindow(iw2);

			final GUIRippingTestCaseRunner runner = new GUIRippingTestCaseRunner("./resources/for_test",
					"./resources/for_test/apps/upmsmall/upm-small.jar:"
							+ "./resources/for_test/apps/upmsmall/commons-logging-1.1.jar:"
							+ "./resources/for_test/apps/upmsmall/commons-codec-1.3.jar:"
							+ "./resources/for_test/apps/upmsmall/applejavaextensions-1.4.jar:"
							+ "./resources/for_test/apps/upmsmall/bcprov-jdk14-145.jar",
					"com._17od.upm.gui.MainWindow", "./resources/for_test/xml/guiripping-upmsmall-EFG.xml",
					"./resources/for_test/xml/guiripping-upmsmall-GUI.xml");
			// {
			//
			// @Override
			// void createFiles() {
			//
			// this.efg_path =
			// "./resources/for_test/xml/guiripping-upmsmall-EFG.xml";
			// this.gui_path =
			// "./resources/for_test/xml/guiripping-upmsmall-GUI.xml";
			// }
			// };

			assertTrue(runner.runGUITestCase(test));
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
