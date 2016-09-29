package usi.gui.pattern;

import usi.gui.structure.Window;

public class Pattern_error_window extends Pattern_window {

	private static Pattern_error_window instance;
	final private Pattern_action_widget paw = new Pattern_action_widget("paw", ".*(ok|next).*",
			Cardinality.ONE, "", null);
	final private Pattern_input_widget piw = new Pattern_input_widget("piw", ".*",
			Cardinality.NONE, "", ".*", null);
	final private Pattern_selectable_widget psw = new Pattern_selectable_widget("psw", ".*",
			Cardinality.NONE, "", ".*", null);

	static public Pattern_error_window getInstance() {

		if (instance == null) {
			instance = new Pattern_error_window();
		}
		return instance;
	}

	private Pattern_error_window() {

		super("error_w", ".*", Cardinality.ONE, "", Boolean_regexp.TRUE, false, null);
		this.addWidget(this.paw);
		this.addWidget(this.piw);
		this.addWidget(this.psw);
	}

	@Override
	public boolean isMatch(final Window w) throws Exception {

		final boolean out = super.isMatch(w);
		if (!out) {
			return false;
		}
		if (w.getActionWidgets().size() == 1 && w.getSelectableWidgets().size() == 0
				&& w.getInputWidgets().size() == 0) {
			return true;
		}
		return false;
	}
}
