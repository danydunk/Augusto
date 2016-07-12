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
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import test.gui.GUIStructureMaker;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.functionality.mapping.Instance_window;
import usi.gui.pattern.Boolean_regexp;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.SpecificSemantics;
import usi.guisemantic.alloy.AlloyUtil;
import usi.guisemantic.alloy.Alloy_Model;
import usi.guisemantic.testcase.AlloyTestCaseGenerator;
import usi.guisemantic.testcase.GUITestCase;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class AlloyTestCaseGenerationTest {

	public class Action_widget_test extends Action_widget {

		public Action_widget_test(final String id, final String label) throws Exception {

			super(id, label, "class", 1, 1);
		}
	}

	public class Input_widget_test extends Input_widget {

		public Input_widget_test(final String id, final String label, final String value)
				throws Exception {

			super(id, label, "class", 1, 1, value);
		}
	}

	public class Window_test extends Window {

		public Window_test(final String id, final String label) throws Exception {

			super(id, label, "class", 1, 1, false);
		}

		public Window_test(final String id, final boolean b, final String label) throws Exception {

			super(id, label, "class", 1, 1, b);
		}

		public Window_test(final String id, final boolean b, final String label, final boolean root)
				throws Exception {

			super(id, label, "class", 1, 1, b);
			super.setRoot(root);
		}
	}

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
		final Window w1 = new Window_test("w1", "init");
		final Action_widget aw1 = new Action_widget_test("aw1", "add");
		final Action_widget aw2 = new Action_widget_test("aw2", "test");
		w1.addWidget(aw1);
		w1.addWidget(aw2);
		gui.addWindow(w1);
		// w2
		final Window w2 = new Window_test("w2", "form");
		final Action_widget aw3 = new Action_widget_test("aw3", "next");
		final Action_widget aw4 = new Action_widget_test("aw4", "back");
		final Input_widget iw1 = new Input_widget_test("iw1", "field1", "");
		final Input_widget iw2 = new Input_widget_test("iw2", "field2", "");
		w2.addWidget(aw3);
		w2.addWidget(aw4);
		w2.addWidget(iw1);
		w2.addWidget(iw2);
		gui.addWindow(w2);
		// w3
		final Window w3 = new Window_test("w3", "other");
		final Action_widget aw5 = new Action_widget_test("aw5", "add");
		final Input_widget iw3 = new Input_widget_test("iw3", "field3", "");
		w3.addWidget(aw5);
		w3.addWidget(iw3);
		gui.addWindow(w3);
		// edges
		gui.addStaticEdge(aw1.getId(), w2.getId());
		gui.addStaticEdge(aw2.getId(), w3.getId());
		gui.addStaticEdge(aw4.getId(), w1.getId());

		final GUI_Pattern pattern = new GUI_Pattern();
		// pw1
		final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "Initial",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
				Cardinality.SOME, "Trigger");
		pw1.addWidget(paw1);
		pattern.addWindow(pw1);
		// pw2
		final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "Form",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
				Cardinality.ONE, "Cancel");
		final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME,
				"Input_widget", null);
		pw2.addWidget(paw2);
		pw2.addWidget(paw3);
		pw2.addWidget(piw1);
		pattern.addWindow(pw2);
		// pw3
		final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "Confirm",
				Boolean_regexp.TRUE, Boolean_regexp.ANY);
		final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
				Cardinality.ONE, "Cancel");
		pw3.addWidget(paw4);
		pw3.addWidget(paw5);
		pattern.addWindow(pw3);
		// edges
		pattern.addEdge(paw1, pw2);
		pattern.addEdge(paw3, pw1);
		pattern.addEdge(paw2, pw3);
		pattern.addEdge(paw5, pw2);

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
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in);
		final List<GUITestCase> tests = generator.generateTestCases(1, 30000);
		assertEquals(4, tests.size());
	}

	/**
	 * Scenario taken from test 2
	 *
	 * @throws Exception
	 */
	@Test
	public void test3_RefineSemanticSpecification() throws Exception {

		// Concrete GUI, 3 forms
		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = new GUI_Pattern();
		// pw1
		final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "Initial",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
				Cardinality.SOME, "Trigger");
		pw1.addWidget(paw1);
		pattern.addWindow(pw1);
		// pw2
		final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "Form",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
				Cardinality.ONE, "Cancel");
		final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME,
				"Input_widget", null);
		pw2.addWidget(paw2);
		pw2.addWidget(paw3);
		pw2.addWidget(piw1);
		pattern.addWindow(pw2);
		// pw3
		final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "Confirm",
				Boolean_regexp.TRUE, Boolean_regexp.ANY);
		final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
				Cardinality.ONE, "Cancel");
		pw3.addWidget(paw4);
		pw3.addWidget(paw5);
		pattern.addWindow(pw3);
		// edges
		pattern.addEdge(paw1, pw2);
		pattern.addEdge(paw3, pw1);
		pattern.addEdge(paw2, pw3);
		pattern.addEdge(paw5, pw2);

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

		final Map<Window, Pattern_window> winMap = in.getWindows_mapping();

		// pw3 (Confirmation window) was not associated to a windows because it
		// was not discovered
		// let's assert this:
		for (final Window win : winMap.keySet()) {
			final String pwi = winMap.get(win).getId();
			assertNotSame(pw3, pwi);
		}

		Window w2 = null;
		for (final Window w : gui.getWindows()) {
			if (w.getId().equals("w2")) {
				w2 = w;
				break;
			}
		}

		final SpecificSemantics semantic4discoveringPw3 = AlloyTestCaseGenerator
				.semantic4DiscoverWindow(in, //
						w2, // sourceWindow, //
						pw3, // pattern_TargetWindow,//
						paw2// patternActionWidget
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
	}

	@Test
	public void test4_addingRedefinition() throws Exception {

		// Concrete GUI, 3 forms
		final GUI gui = GUIStructureMaker.instance1();

		final GUI_Pattern pattern = new GUI_Pattern();
		// pw1
		final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "Initial",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
				Cardinality.SOME, "Trigger");
		pw1.addWidget(paw1);
		pattern.addWindow(pw1);
		// pw2
		final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "Form",
				Boolean_regexp.ANY, Boolean_regexp.ANY);
		final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
				Cardinality.ONE, "Cancel");
		final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME,
				"Input_widget", null);
		pw2.addWidget(paw2);
		pw2.addWidget(paw3);
		pw2.addWidget(piw1);
		pattern.addWindow(pw2);
		// pw3
		final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "Confirm",
				Boolean_regexp.TRUE, Boolean_regexp.ANY);
		final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
				Cardinality.ONE, "Ok");
		final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
				Cardinality.ONE, "Cancel");
		pw3.addWidget(paw4);
		pw3.addWidget(paw5);
		pattern.addWindow(pw3);
		// edges
		pattern.addEdge(paw1, pw2);
		pattern.addEdge(paw3, pw1);
		pattern.addEdge(paw2, pw3);
		pattern.addEdge(paw5, pw2);

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

		final Map<Window, Pattern_window> winMap = in.getWindows_mapping();

		// pw3 (Confirmation window) was not associated to a windows because it
		// was not discovered
		// let's assert this:
		for (final Window win : winMap.keySet()) {
			final String pwi = winMap.get(win).getId();
			assertNotSame(pw3, pwi);
		}

		Window w2 = null;
		for (final Window w : gui.getWindows()) {
			if (w.getId().equals("w2")) {
				w2 = w;
				break;
			}
		}

		final SpecificSemantics semantic4discoveringPw3 = AlloyTestCaseGenerator
				.semantic4DiscoverWindow(in, //
						w2, // sourceWindow, //
						pw3, // pattern_TargetWindow,//
						paw2// patternActionWidget
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

		in.generateSpecificSemantics();
		final AlloyTestCaseGenerator generator = new AlloyTestCaseGenerator(in);
		final List<GUITestCase> tests = generator.generateTestCases(1, 30000);

		assertEquals(4, tests.size());

		final SpecificSemantics constrainSemantic = AlloyTestCaseGenerator.validateRequired(in,
				solution, tests.get(0));

		assertNotNull(constrainSemantic);
	}
}
