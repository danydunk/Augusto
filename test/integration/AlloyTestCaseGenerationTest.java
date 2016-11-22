package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import src.usi.gui.functionality.GUIFunctionality_validate;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.GUI;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.semantic.FunctionalitySemantics;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import src.usi.semantic.alloy.Alloy_Model;
import src.usi.testcase.AlloyTestCaseGenerator;
import src.usi.testcase.structure.GUITestCase;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyTestCaseGenerationTest {

	@Test
	public void test1() {

		try {
			String gui_s = "";
			String s;

			BufferedReader br = new BufferedReader(new FileReader(
					"./files/for_test/alloy/GUI_general_old.als"));
			while ((s = br.readLine()) != null) {
				gui_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./files/for_test/alloy/ADD.als"));
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

			final Instance_GUI_pattern inst = new Instance_GUI_pattern(new GUI(), new GUI_Pattern(
					"test"), new ArrayList<Instance_window>()) {

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
				protected GUITestCase analyzeTuples(final A4Solution solution, final String run,
						final List<String> v) throws Exception {

					return null;
				}
			}
			final Instance_GUI_pattern inst2 = new Instance_GUI_pattern(new GUI(), new GUI_Pattern(
					"test"), new ArrayList<Instance_window>());
			final Wrapper2 wr = new Wrapper2(inst2, null);
			final List<String> runs = wr.generate(inst.getSemantics());
			for (final String run : runs) {
				inst.getSemantics().addRun_command(run);
			}

			inst.getSemantics().addRun_command("run {System}");
			inst.getSemantics().addRun_command("run {System} for 7");
			inst.getSemantics().addRun_command("run {System} for 7 but 5 Time");

			final For_test generator = new For_test(inst, 1, 40000);
			final List<GUITestCase> tests = generator.generateTestCases();
			assertEquals(13, tests.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	class Wrapper2 extends GUIFunctionality_validate {

		public Wrapper2(final Instance_GUI_pattern instancePattern, final GUI gui) throws Exception {

			super(instancePattern, gui);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void init() {

		}

		@Override
		public void generate_run_commands(final FunctionalitySemantics sem) throws Exception {

			if (sem == null) {
				return;
			} else {
				super.generate_run_commands(sem);
			}
		}

		public List<String> generate(final FunctionalitySemantics sem) throws Exception {

			super.generate_run_commands(sem);
			return super.getAllSemanticCases();
		}
	}
}