package usi.guifunctionality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

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
import usi.guistructure.Window;

public class GUIFunctionalitySearchTest {

	@Test
	public void test1() {

		try {
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
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
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
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", true, "confirm", false);
			final Action_widget aw6 = new Action_widget("aw6", "ok");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w2);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w1);
			gui.addEdge(aw3, w4);
			gui.addEdge(aw7, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
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
			// w4
			final Window w4 = new Window("w4", true, "confirm", false);
			final Action_widget aw6 = new Action_widget("aw6", "ok");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w2);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w1);
			gui.addEdge(aw3, w4);
			gui.addEdge(aw7, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", true, "confirm", false);
			final Action_widget aw8 = new Action_widget("aw8", "ok");
			final Action_widget aw9 = new Action_widget("aw9", "back");
			w5.addActionWidget(aw8);
			w5.addActionWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);
			gui.addEdge(aw3, w5);
			gui.addEdge(aw9, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
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
			w2.addInputWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", true, "confirm", false);
			final Action_widget aw8 = new Action_widget("aw8", "ok");
			final Action_widget aw9 = new Action_widget("aw9", "back");
			w5.addActionWidget(aw8);
			w5.addActionWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);
			gui.addEdge(aw3, w5);
			gui.addEdge(aw9, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", true, "confirm", false);
			final Action_widget aw6 = new Action_widget("aw6", "ok");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w2);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w1);
			gui.addEdge(aw3, w4);
			gui.addEdge(aw7, w2);

			// w1B
			final Window w1b = new Window("w1b", "init1");
			final Action_widget aw1b = new Action_widget("aw1b", "add");
			final Action_widget aw2b = new Action_widget("aw2b", "test");
			w1b.addActionWidget(aw1b);
			w1b.addActionWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2");
			final Action_widget aw3b = new Action_widget("aw3b", "next");
			final Action_widget aw4b = new Action_widget("aw4b", "back");
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "");
			w2b.addActionWidget(aw3b);
			w2b.addActionWidget(aw4b);
			w2b.addInputWidget(iw1b);
			w2b.addInputWidget(iw2b);
			gui.addWindow(w2b);
			// edges
			gui.addEdge(aw1b, w2b);
			gui.addEdge(aw4b, w1b);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// edges
			pattern.addEdge(paw1, pw2);
			pattern.addEdge(paw3, pw1);
			pattern.addEdge(paw2, pw3);
			pattern.addEdge(paw5, pw2);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
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

			assertEquals(2, res.size());
			assertEquals(2, res.get(1).getWindows().size());

			ww1 = null;
			ww2 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", true, "confirm", false);
			final Action_widget aw8 = new Action_widget("aw8", "ok");
			final Action_widget aw9 = new Action_widget("aw9", "back");
			w5.addActionWidget(aw8);
			w5.addActionWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);
			gui.addEdge(aw3, w5);
			gui.addEdge(aw9, w2);

			// w1B
			final Window w1b = new Window("w1b", "init1");
			final Action_widget aw1b = new Action_widget("aw1b", "add");
			final Action_widget aw2b = new Action_widget("aw2b", "test");
			w1b.addActionWidget(aw1b);
			w1b.addActionWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2");
			final Action_widget aw3b = new Action_widget("aw3b", "next");
			final Action_widget aw4b = new Action_widget("aw4b", "back");
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "");
			w2b.addActionWidget(aw3b);
			w2b.addActionWidget(aw4b);
			w2b.addInputWidget(iw1b);
			w2b.addInputWidget(iw2b);
			gui.addWindow(w2b);
			// w3B
			final Window w3b = new Window("w3b", "form2");
			final Action_widget aw5b = new Action_widget("aw5b", "next");
			final Action_widget aw6b = new Action_widget("aw6b", "back");
			final Input_widget iw3b = new Input_widget("iw3b", "field1", "");
			w3b.addActionWidget(aw5b);
			w3b.addActionWidget(aw6b);
			w3b.addInputWidget(iw3b);
			gui.addWindow(w3b);
			// edges
			gui.addEdge(aw1b, w3b);
			gui.addEdge(aw6b, w1b);
			gui.addEdge(aw5b, w2b);
			gui.addEdge(aw4b, w3b);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			assertEquals(3, res.get(1).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
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

			assertEquals(4, res.get(0).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

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
	public void test11() {

		try {
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
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", true, "confirm", false);
			final Action_widget aw6 = new Action_widget("aw6", "ok");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w2);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w1);
			gui.addEdge(aw3, w4);
			gui.addEdge(aw7, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			w2.addInputWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", true, "confirm", false);
			final Action_widget aw8 = new Action_widget("aw8", "ok");
			final Action_widget aw9 = new Action_widget("aw9", "back");
			w5.addActionWidget(aw8);
			w5.addActionWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);
			gui.addEdge(aw3, w5);
			gui.addEdge(aw9, w2);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			// w4
			final Window w4 = new Window("w4", true, "confirm", false);
			final Action_widget aw6 = new Action_widget("aw6", "ok");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addEdge(aw1, w2);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w1);
			gui.addEdge(aw3, w4);
			gui.addEdge(aw7, w2);

			// w1B
			final Window w1b = new Window("w1b", "init1");
			final Action_widget aw1b = new Action_widget("aw1b", "add");
			final Action_widget aw2b = new Action_widget("aw2b", "test");
			w1b.addActionWidget(aw1b);
			w1b.addActionWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2");
			final Action_widget aw3b = new Action_widget("aw3b", "next");
			final Action_widget aw4b = new Action_widget("aw4b", "back");
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "");
			w2b.addActionWidget(aw3b);
			w2b.addActionWidget(aw4b);
			w2b.addInputWidget(iw1b);
			w2b.addInputWidget(iw2b);
			gui.addWindow(w2b);
			// edges
			gui.addEdge(aw1b, w2b);
			gui.addEdge(aw4b, w1b);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			assertEquals(2, res.get(1).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
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

			assertEquals(3, res.get(0).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

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
	public void test20() {

		try {
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
			// w4
			final Window w4 = new Window("w4", "form");
			final Action_widget aw6 = new Action_widget("aw6", "next");
			final Action_widget aw7 = new Action_widget("aw7", "back");
			final Input_widget iw4 = new Input_widget("iw4", "field3", "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "");
			w4.addActionWidget(aw6);
			w4.addActionWidget(aw7);
			w4.addInputWidget(iw4);
			w4.addInputWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", true, "confirm", false);
			final Action_widget aw8 = new Action_widget("aw8", "ok");
			final Action_widget aw9 = new Action_widget("aw9", "back");
			w5.addActionWidget(aw8);
			w5.addActionWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addEdge(aw1, w4);
			gui.addEdge(aw6, w2);
			gui.addEdge(aw7, w1);
			gui.addEdge(aw2, w3);
			gui.addEdge(aw4, w4);
			gui.addEdge(aw3, w5);
			gui.addEdge(aw9, w2);

			// w1B
			final Window w1b = new Window("w1b", "init1");
			final Action_widget aw1b = new Action_widget("aw1b", "add");
			final Action_widget aw2b = new Action_widget("aw2b", "test");
			w1b.addActionWidget(aw1b);
			w1b.addActionWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2");
			final Action_widget aw3b = new Action_widget("aw3b", "next");
			final Action_widget aw4b = new Action_widget("aw4b", "back");
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "");
			w2b.addActionWidget(aw3b);
			w2b.addActionWidget(aw4b);
			w2b.addInputWidget(iw1b);
			w2b.addInputWidget(iw2b);
			gui.addWindow(w2b);
			// w3B
			final Window w3b = new Window("w3b", "form2");
			final Action_widget aw5b = new Action_widget("aw5b", "next");
			final Action_widget aw6b = new Action_widget("aw6b", "back");
			final Input_widget iw3b = new Input_widget("iw3b", "field1", "");
			w3b.addActionWidget(aw5b);
			w3b.addActionWidget(aw6b);
			w3b.addInputWidget(iw3b);
			gui.addWindow(w3b);
			// edges
			gui.addEdge(aw1b, w3b);
			gui.addEdge(aw6b, w1b);
			gui.addEdge(aw5b, w2b);
			gui.addEdge(aw4b, w3b);

			final GUI_Pattern pattern = new GUI_Pattern();
			// pw1
			final Pattern_window pw1 = new Pattern_window("pw1", ".*", Cardinality.SOME, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw1 = new Pattern_action_widget("paw1", ".*add.*", Cardinality.SOME, "");
			pw1.addActionWidget(paw1);
			pattern.addWindow(pw1);
			// pw2
			final Pattern_window pw2 = new Pattern_window("pw2", ".*", Cardinality.ONE, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw2 = new Pattern_action_widget("paw2", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw3 = new Pattern_action_widget("paw3", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*", Cardinality.SOME, "", null);
			pw2.addActionWidget(paw2);
			pw2.addActionWidget(paw3);
			pw2.addInputWidget(piw1);
			pattern.addWindow(pw2);
			// pw3
			final Pattern_window pw3 = new Pattern_window("pw3", ".*", Cardinality.LONE, "", Boolean_regexp.TRUE,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw4 = new Pattern_action_widget("paw4", ".*ok.*", Cardinality.ONE, "");
			final Pattern_action_widget paw5 = new Pattern_action_widget("paw5", ".*back.*", Cardinality.ONE, "");
			pw3.addActionWidget(paw4);
			pw3.addActionWidget(paw5);
			pattern.addWindow(pw3);
			// pw4
			final Pattern_window pw4 = new Pattern_window("pw4", ".*", Cardinality.SET, "", Boolean_regexp.ANY,
					Boolean_regexp.ANY);
			final Pattern_action_widget paw6 = new Pattern_action_widget("paw6", ".*next.*", Cardinality.ONE, "");
			final Pattern_action_widget paw7 = new Pattern_action_widget("paw7", ".*back.*", Cardinality.ONE, "");
			final Pattern_input_widget piw2 = new Pattern_input_widget("piw2", ".*", Cardinality.SOME, "", null);
			pw4.addActionWidget(paw6);
			pw4.addActionWidget(paw7);
			pw4.addInputWidget(piw2);
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
			assertEquals(3, res.get(1).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
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

			assertEquals(4, res.get(0).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

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

}
