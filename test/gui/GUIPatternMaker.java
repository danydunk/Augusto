package test.gui;

import usi.gui.pattern.Boolean_regexp;
import usi.gui.pattern.Cardinality;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_window;

public class GUIPatternMaker {

	// single form, confirmation
	public static GUI_Pattern instance1() {

		final GUI_Pattern pattern = new GUI_Pattern();
		try {
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
			final Pattern_input_widget piw1 = new Pattern_input_widget("piw1", ".*",
					Cardinality.SOME, "Input_widget", null);
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
			pattern.addStaticEdge(paw1.getId(), pw2.getId());
			pattern.addStaticEdge(paw3.getId(), pw1.getId());
			pattern.addStaticEdge(paw2.getId(), pw3.getId());
			pattern.addStaticEdge(paw5.getId(), pw2.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return pattern;
	}

	// single form, no confirmation
	public static GUI_Pattern instance2() {

		final GUI_Pattern pattern = new GUI_Pattern();
		try {
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
			pattern.addStaticEdge(paw1.getId(), pw2.getId());
			pattern.addStaticEdge(paw3.getId(), pw1.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return pattern;
	}

	// multiple form, confirmation
	public static GUI_Pattern instance3() {

		final GUI_Pattern pattern = new GUI_Pattern();
		try {
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
			pattern.addStaticEdge(paw1.getId(), pw2.getId());
			pattern.addStaticEdge(paw2.getId(), pw4.getId());
			pattern.addStaticEdge(paw2.getId(), pw3.getId());
			pattern.addStaticEdge(paw3.getId(), pw1.getId());
			pattern.addStaticEdge(paw6.getId(), pw4.getId());
			pattern.addStaticEdge(paw6.getId(), pw3.getId());
			pattern.addStaticEdge(paw7.getId(), pw4.getId());
			pattern.addStaticEdge(paw7.getId(), pw2.getId());
			pattern.addStaticEdge(paw5.getId(), pw4.getId());
			pattern.addStaticEdge(paw5.getId(), pw2.getId());

		} catch (final Exception e) {
			e.printStackTrace();
		}
		return pattern;
	}
}