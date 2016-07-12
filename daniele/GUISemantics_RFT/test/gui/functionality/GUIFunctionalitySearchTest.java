package test.gui.functionality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

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

public class GUIFunctionalitySearchTest {

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
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

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
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);

			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test4() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test5() {

		try {
			final GUI gui = GUIStructureMaker.instance3();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test6() {

		try {
			final GUI gui = GUIStructureMaker.instance3();
			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);
			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test7() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test8() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(4, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test9() {

		try {
			final GUI gui = GUIStructureMaker.instance5();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(1).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(2, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			ww1 = null;
			ww2 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test10() {

		try {
			final GUI gui = GUIStructureMaker.instance6();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);
			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				case "w3b":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(4, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test11() {

		try {
			final GUI gui = GUIStructureMaker.instance1();
			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

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
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test14() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test15() {

		try {
			final GUI gui = GUIStructureMaker.instance3();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test18() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(4, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test19() {

		try {
			final GUI gui = GUIStructureMaker.instance5();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(2, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);

			assertEquals(3, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test20() {

		try {
			final GUI gui = GUIStructureMaker.instance6();

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*",
					Cardinality.SOME, "");
			pw1.addWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "", null);
			pw2.addWidget(paw2);
			pw2.addWidget(paw3);
			pw2.addWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "",
					Boolean_regexp.TRUE, Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*",
					Cardinality.ONE, "");
			pw3.addWidget(paw4);
			pw3.addWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "",
					Boolean_regexp.ANY, Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*",
					Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*",
					Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*",
					Cardinality.SOME, "", null);
			pw4.addWidget(paw6);
			pw4.addWidget(paw7);
			pw4.addWidget(piw2);
			pattern.addWindow(pw4);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw2, pw4);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw6, pw4);
			pattern.addEdge(paw6, pw3);
			pattern.addEdge(paw7, pw4);
			pattern.addEdge(paw7, pw2);
			pattern.addEdge(paw5, pw4);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				case "w3b":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(4, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
