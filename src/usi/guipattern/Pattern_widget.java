package usi.guipattern;

import java.util.regex.Matcher;

import usi.guistructure.Widget;

public abstract class Pattern_widget<T extends Widget> {

	private final String id;
	private final String label;
	private final String alloy_correspondence;
	private final Cardinality cardinality;

	public Pattern_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence) {
		if (id != null) {
			this.id = id;
		} else {
			this.id = "";
		}
		if (label != null) {
			this.label = label;
		} else {
			this.label = ".*";
		}
		if (card != null) {
			this.cardinality = card;
		} else {
			this.cardinality = Cardinality.SET;
		}
		if (alloy_correspondence != null) {
			this.alloy_correspondence = alloy_correspondence;
		} else {
			this.alloy_correspondence = "";
		}
	}

	public Cardinality getCardinality() {

		return this.cardinality;
	}

	public String getId() {

		return this.id;
	}

	public String getLabel() {

		return this.label;
	}

	public boolean isMatch(final T w) throws Exception {

		if (w == null) {
			throw new Exception("Pattern_widget: wrong input in isMatch");
		}

		final java.util.regex.Pattern r = java.util.regex.Pattern.compile(this.getLabel());

		final Matcher m = r.matcher(w.getLabel());
		return m.find();
	}

	// public abstract boolean isMatch(T e) throws Exception;
	//
	// public List<T> isMatch(Collection<T> l) throws Exception {
	// List<T> matches = new ArrayList<>();
	//
	// for(T el : l) {
	// if(this.isMatch(el))
	// matches.add(el);
	// }
	// return matches;
	// }

	public String getAlloyCorrespondence() {

		return this.alloy_correspondence;
	}

	// public void setAlloyCorrespondence(final String alloy_correspondence) {
	//
	// this.alloy_correspondence = alloy_correspondence;
	// }
}
