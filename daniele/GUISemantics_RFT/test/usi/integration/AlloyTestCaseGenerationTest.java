package usi.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import usi.guifunctionality.GUIFunctionality_search;
import usi.guifunctionality.mapping.Instance_GUI_pattern;
import usi.guifunctionality.mapping.Instance_window;
import usi.guipattern.Boolean_regexp;
import usi.guipattern.Cardinality;
import usi.guipattern.GUI_Pattern;
import usi.guipattern.Pattern_action_widget;
import usi.guipattern.Pattern_input_widget;
import usi.guipattern.Pattern_window;
import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.SpecificSemantics;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.Alloy_Model;
import usi.guisemantic.testcase.AlloyTestCaseGenerator;
import usi.guisemantic.testcase.GUITestCase;
import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.Window;

public class AlloyTestCaseGenerationTest {

	@Test
	public void test1() {

		try {
			String gui_s = "";
			String s;

			BufferedReader br = new BufferedReader(new FileReader("./resources/alloy/GUI_general.als"));
			while ((s = br.readLine()) != null) {
				gui_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./resources/alloy/ADD.als"));
			String func_s = "";
			while ((s = br.readLine()) != null) {
				func_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./resources/for_test/alloy/specific.als"));
			String spec = "";
			while ((s = br.readLine()) != null) {
				spec += s + System.getProperty("line.separator");
			}
			br.close();

			final String model = gui_s + func_s;
			final String spec_model = model + spec;

			final Alloy_Model func = AlloyUtil.loadAlloyModelFromString(model);
			final FunctionalitySemantics func_sem = FunctionalitySemantics.instantiate(func);

			final Alloy_Model func_spec = AlloyUtil.loadAlloyModelFromString(spec_model);
			final SpecificSemantics spec_sem = SpecificSemantics.instantiate(func_spec);

			final Instance_GUI_pattern inst = new Instance_GUI_pattern(new GUI(), new GUI_Pattern(),
					new ArrayList<Instance_window>()) {

				@Override
				public void generateSpecificSemantics() throws Exception {

					this.semantics = spec_sem;
				}
			};
			inst.generateSpecificSemantics();

			class For_test extends AlloyTestCaseGenerator {

				public For_test(final Instance_GUI_pattern instance) {
					super(instance);
					// TODO Auto-generated constructor stub
				}

				@Override
				protected GUITestCase analyzeTuples(final A4Solution solution) throws Exception {

					return null;
				}
			}
			final For_test generator = new For_test(inst);
			final List<GUITestCase> tests = generator.generateTestCases(1, 30000);
			assertEquals(3, tests.size());
		} catch (

		final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() throws Exception {

		final GUI gui = new GUI();
		// w1
		final Window w1 = new Window("w1", "init");
		final Action_widget aw1 = new Action_widget("aw1", "add");
		final Action_widget aw2 = new Action_widget("aw2", "test");
		w1.addActionWidget(aw1);
		w1.addActionWidget(aw2);
		gui.addWindow(w1);
		// w2
		final Window w2 = new Window("w2", "form");
		final Action_widget aw3 = new Action_widget("aw3", "next");
		final Action_widget aw4 = new Action_widget("aw4", "back");
		final Input_widget iw1 = new Input_widget("iw1", "field1", "");
		final Input_widget iw2 = new Input_widget("iw2", "field2", "");
		w2.addActionWidget(aw3);
		w2.addActionWidget(aw4);
		w2.addInputWidget(iw1);
		w2.addInputWidget(iw2);
		gui.addWindow(w2);
		// w3
		final Window w3 = new Window("w3", "other");
		final Action_widget aw5 = new Action_widget("aw5", "add");
		final Input_widget iw3 = new Input_widget("iw3", "field3", "");
		w3.addActionWidget(aw5);
		w3.addInputWidget(iw3);
		gui.addWindow(w3);
		// edges
		gui.addEdge(aw1, w2);
		gui.addEdge(aw2, w3);
		gui.addEdge(aw4, w1);

		final GUI_Pattern pattern = new GUI_Pattern();
		// pw1
		final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "Initial", Boolean_regexp.ANY,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "Trigger");
		pw1.addActionWidget(paw1);
		pattern.addWindow(pw1);
		// pw2
		final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "Form", Boolean_regexp.ANY,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "Ok");
		final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "Cancel");
		final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "Input_widget",
				null);
		pw2.addActionWidget(paw2);
		pw2.addActionWidget(paw3);
		pw2.addInputWidget(piw1);
		pattern.addWindow(pw2);
		// pw3
		final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "Confirm", Boolean_regexp.TRUE,
				Boolean_regexp.ANY);
		final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "Ok");
		final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "Cancel");
		pw3.addActionWidget(paw4);
		pw3.addActionWidget(paw5);
		pattern.addWindow(pw3);
		// edges
		pattern.addEdge(paw1, pw2);
		pattern.addEdge(paw3, pw1);
		pattern.addEdge(paw2, pw3);
		pattern.addEdge(paw5, pw2);

		pattern.loadSemantics("ADD.als");
		// final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new
		// File("./resources/alloy/ADD.als"));
		// pattern.setSemantics(FunctionalitySemantics.instantiate(model));

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);
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

		final Instance_GUI_pattern in = res.get(0);

		in.generateSpecificSemantics();
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in);
		final List<GUITestCase> tests = generator.generateTestCases(1, 30000);
		assertEquals(4, tests.size());
	}
}
