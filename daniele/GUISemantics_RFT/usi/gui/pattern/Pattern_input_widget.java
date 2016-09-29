package usi.gui.pattern;

import java.util.regex.Matcher;

import usi.gui.structure.Input_widget;
import usi.gui.structure.Option_input_widget;

public class Pattern_input_widget extends Pattern_widget<Input_widget> {

	private final String value;

	public Pattern_input_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String value, final String classs) {

		super(id, label, card, alloy_correspondence, classs);
		if (value != null) {
			this.value = value;
		} else {
			this.value = ".*";
		}
	}

	public String getValue() {

		return this.value;
	}

	@Override
	public boolean isMatch(final Input_widget iw) throws Exception {

		if (!super.isMatch(iw)) {
			return false;
		}

		final java.util.regex.Pattern r = java.util.regex.Pattern.compile(this.getValue());

		if (iw instanceof Option_input_widget) {
			final Option_input_widget oiw = (Option_input_widget) iw;
			final String v = String.valueOf(oiw.getSelected()) + ":"
					+ String.valueOf(oiw.getSize());
			final Matcher m = r.matcher(String.valueOf(v));
			return (m.find());
		}

		final Matcher m = r.matcher(String.valueOf(iw.getValue()));
		return (m.find());
	}

}
