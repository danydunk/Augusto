package test.usi.gui;

import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Window;

public class GUIStructureMaker {

	// single form, no confirmation
	public static GUI instance1() {

		final GUI gui = new GUI();
		try {
			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// edges
			gui.addStaticEdge(aw1.getId(), w2.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w1.getId());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}

	// multiple form, no confirmation
	public static GUI instance2() {

		final GUI gui = new GUI();
		try {
			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "form", "class", 1, 1, false);
			final Action_widget aw6 = new Action_widget("aw6", "next", "class", 1, 1);
			final Action_widget aw7 = new Action_widget("aw7", "back", "class", 1, 1);
			final Input_widget iw4 = new Input_widget("iw4", "field3", "class", 1, 1, "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "class", 1, 1, "");
			w4.addWidget(aw6);
			w4.addWidget(aw7);
			w4.addWidget(iw4);
			w4.addWidget(iw5);
			gui.addWindow(w4);
			// edges
			gui.addStaticEdge(aw1.getId(), w4.getId());
			gui.addStaticEdge(aw6.getId(), w2.getId());
			gui.addStaticEdge(aw7.getId(), w1.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w4.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}

	// single form, confirmation
	public static GUI instance3() {

		final GUI gui = new GUI();
		try {
			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "confirm", "class", 1, 1, true);
			final Action_widget aw6 = new Action_widget("aw6", "ok", "class", 1, 1);
			final Action_widget aw7 = new Action_widget("aw7", "back", "class", 1, 1);
			w4.addWidget(aw6);
			w4.addWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addStaticEdge(aw1.getId(), w2.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w1.getId());
			gui.addStaticEdge(aw3.getId(), w4.getId());
			gui.addStaticEdge(aw7.getId(), w2.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}

	// multiple form, confirmation
	public static GUI instance4() {

		final GUI gui = new GUI();
		try {
			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "form", "class", 1, 1, false);
			final Action_widget aw6 = new Action_widget("aw6", "next", "class", 1, 1);
			final Action_widget aw7 = new Action_widget("aw7", "back", "class", 1, 1);
			final Input_widget iw4 = new Input_widget("iw4", "field3", "class", 1, 1, "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "class", 1, 1, "");
			w4.addWidget(aw6);
			w4.addWidget(aw7);
			w4.addWidget(iw4);
			w4.addWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", "confirm", "class", 1, 1, true);
			final Action_widget aw8 = new Action_widget("aw8", "ok", "class", 1, 1);
			final Action_widget aw9 = new Action_widget("aw9", "back", "class", 1, 1);
			w5.addWidget(aw8);
			w5.addWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addStaticEdge(aw1.getId(), w4.getId());
			gui.addStaticEdge(aw6.getId(), w2.getId());
			gui.addStaticEdge(aw7.getId(), w1.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w4.getId());
			gui.addStaticEdge(aw3.getId(), w5.getId());
			gui.addStaticEdge(aw9.getId(), w2.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}

	// single form, confirmation + other add
	public static GUI instance5() {

		final GUI gui = new GUI();
		try {

			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "confirm", "class", 1, 1, true);
			final Action_widget aw6 = new Action_widget("aw6", "ok", "class", 1, 1);
			final Action_widget aw7 = new Action_widget("aw7", "back", "class", 1, 1);
			w4.addWidget(aw6);
			w4.addWidget(aw7);
			gui.addWindow(w4);
			// edges
			gui.addStaticEdge(aw1.getId(), w2.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w1.getId());
			gui.addStaticEdge(aw3.getId(), w4.getId());
			gui.addStaticEdge(aw7.getId(), w2.getId());
			// w1B
			final Window w1b = new Window("w1b", "init1", "class", 1, 1, false);
			final Action_widget aw1b = new Action_widget("aw1b", "add", "class", 1, 1);
			final Action_widget aw2b = new Action_widget("aw2b", "test", "class", 1, 1);
			w1b.addWidget(aw1b);
			w1b.addWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2", "class", 1, 1, false);
			final Action_widget aw3b = new Action_widget("aw3b", "next", "class", 1, 1);
			final Action_widget aw4b = new Action_widget("aw4b", "back", "class", 1, 1);
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "class", 1, 1, "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "class", 1, 1, "");
			w2b.addWidget(aw3b);
			w2b.addWidget(aw4b);
			w2b.addWidget(iw1b);
			w2b.addWidget(iw2b);
			gui.addWindow(w2b);
			// edges
			gui.addStaticEdge(aw1b.getId(), w2b.getId());
			gui.addStaticEdge(aw4b.getId(), w1b.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}

	// multiple form, confirmation + other add
	public static GUI instance6() {

		final GUI gui = new GUI();
		try {

			// w1
			final Window w1 = new Window("w1", "init", "class", 1, 1, false);
			final Action_widget aw1 = new Action_widget("aw1", "add", "class", 1, 1);
			final Action_widget aw2 = new Action_widget("aw2", "test", "class", 1, 1);
			w1.addWidget(aw1);
			w1.addWidget(aw2);
			gui.addWindow(w1);
			// w2
			final Window w2 = new Window("w2", "form", "class", 1, 1, false);
			final Action_widget aw3 = new Action_widget("aw3", "next", "class", 1, 1);
			final Action_widget aw4 = new Action_widget("aw4", "back", "class", 1, 1);
			final Input_widget iw1 = new Input_widget("iw1", "field1", "class", 1, 1, "");
			final Input_widget iw2 = new Input_widget("iw2", "field2", "class", 1, 1, "");
			w2.addWidget(aw3);
			w2.addWidget(aw4);
			w2.addWidget(iw1);
			w2.addWidget(iw2);
			gui.addWindow(w2);
			// w3
			final Window w3 = new Window("w3", "other", "class", 1, 1, false);
			final Action_widget aw5 = new Action_widget("aw5", "add", "class", 1, 1);
			final Input_widget iw3 = new Input_widget("iw3", "field3", "class", 1, 1, "");
			w3.addWidget(aw5);
			w3.addWidget(iw3);
			gui.addWindow(w3);
			// w4
			final Window w4 = new Window("w4", "form", "class", 1, 1, false);
			final Action_widget aw6 = new Action_widget("aw6", "next", "class", 1, 1);
			final Action_widget aw7 = new Action_widget("aw7", "back", "class", 1, 1);
			final Input_widget iw4 = new Input_widget("iw4", "field3", "class", 1, 1, "");
			final Input_widget iw5 = new Input_widget("iw5", "field4", "class", 1, 1, "");
			w4.addWidget(aw6);
			w4.addWidget(aw7);
			w4.addWidget(iw4);
			w4.addWidget(iw5);
			gui.addWindow(w4);
			// w5
			final Window w5 = new Window("w5", "confirm", "class", 1, 1, true);
			final Action_widget aw8 = new Action_widget("aw8", "ok", "class", 1, 1);
			final Action_widget aw9 = new Action_widget("aw9", "back", "class", 1, 1);
			w5.addWidget(aw8);
			w5.addWidget(aw9);
			gui.addWindow(w5);
			// edges
			gui.addStaticEdge(aw1.getId(), w4.getId());
			gui.addStaticEdge(aw6.getId(), w2.getId());
			gui.addStaticEdge(aw7.getId(), w1.getId());
			gui.addStaticEdge(aw2.getId(), w3.getId());
			gui.addStaticEdge(aw4.getId(), w4.getId());
			gui.addStaticEdge(aw3.getId(), w5.getId());
			gui.addStaticEdge(aw9.getId(), w2.getId());
			// w1B
			final Window w1b = new Window("w1b", "init1", "class", 1, 1, false);
			final Action_widget aw1b = new Action_widget("aw1b", "add", "class", 1, 1);
			final Action_widget aw2b = new Action_widget("aw2b", "test", "class", 1, 1);
			w1b.addWidget(aw1b);
			w1b.addWidget(aw2b);
			gui.addWindow(w1b);
			// w2B
			final Window w2b = new Window("w2b", "form2", "class", 1, 1, false);
			final Action_widget aw3b = new Action_widget("aw3b", "next", "class", 1, 1);
			final Action_widget aw4b = new Action_widget("aw4b", "back", "class", 1, 1);
			final Input_widget iw1b = new Input_widget("iw1b", "field1", "class", 1, 1, "");
			final Input_widget iw2b = new Input_widget("iw2b", "field2", "class", 1, 1, "");
			w2b.addWidget(aw3b);
			w2b.addWidget(aw4b);
			w2b.addWidget(iw1b);
			w2b.addWidget(iw2b);
			gui.addWindow(w2b);
			// w3B
			final Window w3b = new Window("w3b", "form2", "class", 1, 1, false);
			final Action_widget aw5b = new Action_widget("aw5b", "next", "class", 1, 1);
			final Action_widget aw6b = new Action_widget("aw6b", "back", "class", 1, 1);
			final Input_widget iw3b = new Input_widget("iw3b", "field1", "class", 1, 1, "");
			w3b.addWidget(aw5b);
			w3b.addWidget(aw6b);
			w3b.addWidget(iw3b);
			gui.addWindow(w3b);
			// edges
			gui.addStaticEdge(aw1b.getId(), w3b.getId());
			gui.addStaticEdge(aw6b.getId(), w1b.getId());
			gui.addStaticEdge(aw5b.getId(), w2b.getId());
			gui.addStaticEdge(aw4b.getId(), w3b.getId());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return gui;
	}
}
