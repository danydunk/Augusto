package src.usi.pattern.structure;

import java.util.regex.Matcher;

import src.usi.gui.structure.Widget;

public abstract class Pattern_widget<T extends Widget> {

	private final String id;
	private final String label;
	private final String classs;
	private final String alloy_correspondence;
	private final Cardinality cardinality;
	private final String position;

	public Pattern_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String classs) {

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
		if (classs != null) {
			this.classs = classs;
		} else {
			this.classs = ".*";
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
		this.position = ".*";
	}

	public Pattern_widget(final String id, final String label, final Cardinality card,
			final String alloy_correspondence, final String classs, final String position) {

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
		if (classs != null) {
			this.classs = classs;
		} else {
			this.classs = ".*";
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

		if (position != null) {
			this.position = position;
		} else {
			this.position = ".*";
		}
	}

	@Override
	public boolean equals(final Object o) {

		if (!(o instanceof Pattern_widget)) {
			return false;
		}
		final Pattern_widget w = (Pattern_widget) o;
		return w.getId().equals(this.getId());
	}

	@Override
	public int hashCode() {

		return this.id.hashCode();
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

	public String getClasss() {

		return this.classs;
	}

	public boolean isMatch(final T w) throws Exception {

		if (w == null) {
			throw new Exception("Pattern_widget: wrong input in isMatch");
		}

		java.util.regex.Pattern r = java.util.regex.Pattern.compile(this.getPosition());
		final String pos = w.getX() + ":" + w.getY();

		Matcher m = r.matcher(pos);
		if (!m.find()) {
			return false;
		}

		r = java.util.regex.Pattern.compile(this.getClasss());

		if (w.getClasss() == null) {
			m = r.matcher("");
			if (!m.find()) {
				return false;
			}
		} else {
			m = r.matcher(w.getClasss().toLowerCase());
			if (!m.find()) {
				return false;
			}
		}

		if (this.getLabel().equals("?null?")) {
			if (w.getLabel() == null && w.getDescriptor() == null) {
				return true;
			} else {
				return false;
			}
		}
		r = java.util.regex.Pattern.compile(this.getLabel());

		if ((w.getLabel() == null || w.getLabel().length() == 0)
				&& (w.getDescriptor() != null && w.getDescriptor().length() > 0)) {
			m = r.matcher(w.getDescriptor().toLowerCase());
			return m.find();
		}

		if (w.getLabel() == null) {
			m = r.matcher("");
			return m.find();
		}

		m = r.matcher(w.getLabel().toLowerCase());

		return m.find();
	}

	public String getAlloyCorrespondence() {

		return this.alloy_correspondence;
	}

	public String getPosition() {

		return this.position;
	}
}
