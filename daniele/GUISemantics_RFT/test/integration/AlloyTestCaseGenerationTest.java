package test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import test.gui.GUIPatternMaker;
import test.gui.GUIStructureMaker;
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
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyTestCaseGenerationTest {

	@Test
	public void test1() {

		try {
			String gui_s = "";
			String s;

			BufferedReader br = new BufferedReader(new FileReader("./files/alloy/GUI_general.als"));
			while ((s = br.readLine()) != null) {
				gui_s += s + System.getProperty("line.separator");
			}
			br.close();

			br = new BufferedReader(new FileReader("./files/alloy/ADD.als"));
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
			final For_test generator = new For_test(inst, 1, 50000);
			final List<GUITestCase> tests = generator.generateTestCases();
			assertEquals(3, tests.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() throws Exception {

		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = GUIPatternMaker.instance1();

		pattern.loadSemantics("ADD.als");
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
		in.getSemantics().generate_run_commands();
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in, 1, 40000);
		final List<GUITestCase> tests = generator.generateTestCases();
		assertEquals(4, tests.size());
	}

	@Test
	public void test4_addingRedefinition() throws Exception {

		// Concrete GUI, 3 forms
		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = GUIPatternMaker.instance1();

		// Pattern 3 Windows

		pattern.loadSemantics("ADD.als");

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

		final SpecificSemantics spec = in.getSemantics();

		System.out.println("Sign " + spec.getSignatures().size());

		System.out.println("Facts " + spec.getFacts().size());

		// pw3 (Confirmation window) was not associated to a windows because it
		// was not discovered
		// let's assert this:
		for (final Window win : in.getGui().getWindows()) {
			final String pwi = in.getPW_for_W(win.getId()).getId();
			assertNotSame("pw3", pwi);
		}

		Window w2 = null;
		Action_widget aw3 = null;
		for (final Window w : gui.getWindows()) {
			if (w.getId().equals("w2")) {
				w2 = w;
			}
			for (final Action_widget aw : w.getActionWidgets()) {
				if (aw.getId().equals("aw3")) {
					aw3 = aw;
				}
			}
		}

		Pattern_window pw3 = null;

		for (final Pattern_window pww : pattern.getWindows()) {
			if (pww.getId().equals("pw3")) {
				pw3 = pww;
			}

		}

		final Wrapper wr = new Wrapper(in, gui);

		final SpecificSemantics semantic4discoveringPw3 = wr.semantic4DiscoverWindow(
				in.getSemantics(), w2, // sourceWindow,
				pw3, // pattern_TargetWindow,//
				aw3// ActionWidget
				);

		assertNotNull(semantic4discoveringPw3);

		assertTrue(semantic4discoveringPw3.getFacts().size() > in.getSemantics().getFacts().size());
		assertTrue(semantic4discoveringPw3.getSignatures().size() > in.getSemantics()
				.getSignatures().size());

		// final AlloyTestCaseGenerator generator = new
		// AlloyTestCaseGenerator(in);
		// final List<GUITestCase> tests = generator.generateTestCases(1,
		// 30000);
		// assertEquals(4, tests.size());

		final String alloy_model = semantic4discoveringPw3.toString();
		System.out.println("START ALLOY MODEL");
		System.out.println(semantic4discoveringPw3);
		System.out.println("END ALLOY MODEL");

		final Module compiled = AlloyUtil.compileAlloyModel(alloy_model);

		assertNotNull(compiled);

		final List<Command> run_commands = compiled.getAllCommands();
		System.out.println(run_commands);
		// TODO: See that Alloy transform the commands.
		final List<Command> runSystem = run_commands.stream()
				.filter(e -> e.toString().equals("Run run$1 for 4")).collect(Collectors.toList());

		assertTrue(runSystem.size() > 0);

		final A4Solution solution = AlloyUtil.runCommand(compiled, runSystem.get(0));
		System.out.println("Has solution: " + solution);

		assertTrue(solution.satisfiable());

		in.setSpecificSemantics(semantic4discoveringPw3);
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in, 1, 30000);
		final List<GUITestCase> tests = generator.generateTestCases();

		assertEquals(1, tests.size());

		final SpecificSemantics constrainSemantic = AlloyTestCaseGenerator.validateRequired(in,
				solution, tests.get(0));

		assertNotNull(constrainSemantic);
	}

	public class Wrapper extends GUIFunctionality_refine {

		public Wrapper(final Instance_GUI_pattern instance, final GUI gui) {

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
