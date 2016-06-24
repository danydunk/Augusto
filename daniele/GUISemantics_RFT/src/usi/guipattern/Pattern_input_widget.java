package usi.guipattern;

import java.util.regex.Matcher;

import usi.guistructure.Input_widget;

public class Pattern_input_widget extends Pattern_widget<Input_widget> {

	private final String text;

	public Pattern_input_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String text) {
		super(id, label, card, alloy_correspondence);
		if (text != null) {
			this.text = text;
		} else {
			this.text = ".*";
		}
	}

	public String getText() {

		return this.text;
	}

	@Override
	public boolean isMatch(final Input_widget iw) throws Exception {

		if (!super.isMatch(iw)) {
			return false;
		}

		final java.util.regex.Pattern r = java.util.regex.Pattern.compile(this.getText());
		final Matcher m = r.matcher(String.valueOf(iw.getText()));
		return (m.find());
	}

}
