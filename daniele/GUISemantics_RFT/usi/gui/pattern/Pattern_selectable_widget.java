package usi.gui.pattern;

import java.util.regex.Matcher;

import usi.gui.structure.Selectable_widget;

public class Pattern_selectable_widget extends Pattern_widget<Selectable_widget> {

	private final String size;

	public Pattern_selectable_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String size, final String classs) {

		super(id, label, card, alloy_correspondence, classs);
		if (size != null) {
			this.size = size;
		} else {
			this.size = ".*";
		}
	}

	public String getSize() {

		return this.size;
	}

	@Override
	public boolean isMatch(final Selectable_widget sw) throws Exception {

		if (!super.isMatch(sw)) {
			return false;
		}

		final java.util.regex.Pattern r = java.util.regex.Pattern.compile(this.getSize());
		final Matcher m = r.matcher(String.valueOf(sw.getSize()));
		return (m.find());
	}
}
