package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import test.gui.pattern.GUIPatternMaker;
import test.gui.structure.GUIStructureMaker;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_window;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.testcase.AlloyTestCaseGenerator;
import usi.gui.semantic.testcase.GUITestCase;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyTestCaseGenerationTest {

	@Test
	public void test1() {

		try {
			String gui_s = "";
			String s;

			BufferedReader br = new BufferedReader(new FileReader(
					"./files/alloy/GUI_general_old.als"));
			while ((s = br.readLine()) != null) {
				gui_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./files/alloy/ADD_old.als"));
			String func_s = "";
			while ((s = br.readLine()) != null) {
				func_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./files/for_test/alloy/specific.als"));
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

			final Instance_GUI_pattern inst = new Instance_GUI_pattern(new GUI(),
					new GUI_Pattern(), new ArrayList<Instance_window>()) {

				@Override
				public void generateSpecificSemantics() throws Exception {

					this.semantics = spec_sem;
				}
			};
			inst.generateSpecificSemantics();

			class For_test extends AlloyTestCaseGenerator {

				public For_test(final Instance_GUI_pattern instance, final int max_run,
						final long timeout) {

					super(instance, max_run, timeout);
					// TODO Auto-generated constructor stub
				}

				@Override
				protected GUITestCase analyzeTuples(final A4Solution solution) throws Exception {

					return null;
				}
			}
			inst.getSemantics().generate_run_commands();
			inst.getSemantics().addRun_command("run {System}");
			inst.getSemantics().addRun_command("run {System} for 7");
			inst.getSemantics().addRun_command("run {System} for 7 but 5 Time");

			final For_test generator = new For_test(inst, 1, 40000);
			final List<GUITestCase> tests = generator.generateTestCases();
			assertEquals(6, tests.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() throws Exception {

		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = GUIPatternMaker.instance1();
		pattern.GUI_SEMANTICS_PATH = "./files/alloy/GUI_general_old.als";
		pattern.loadSemantics("ADD_old.als");
		// final Alloy_Model model = AlloyUtil.loadAlloyModelFromFile(new
		// File("./files/alloy/ADD.als"));
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
		System.out.println(in.getSemantics());

		in.getSemantics().generate_run_commands();
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in, 2, 100000);
		final List<GUITestCase> tests = generator.generateTestCases();
		assertEquals(4, tests.size());
	}

	public class Wrapper extends GUIFunctionality_refine {

		public Wrapper(final Instance_GUI_pattern instance, final GUI gui) throws Exception {

			super(instance, gui);
			// TODO Auto-generated constructor stub
		}

		@Override
		public SpecificSemantics semantic4DiscoverWindow(final SpecificSemantics originalSemantic,
				final Window sourceWindow, final Pattern_window pattern_TargetWindow,
				final Action_widget actionWidget) throws Exception {

			return super.semantic4DiscoverWindow(originalSemantic, sourceWindow,
					pattern_TargetWindow, actionWidget);
		}
	}
}
