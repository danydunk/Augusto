package usi.guistructure;

public abstract class Widget {

	// TO DO: we should add a field to keep track of the class (JButton,
	// JTextField, ...)
	protected final String id;
	protected final String label;
	protected final String classs;

	public Widget(final String id, final String label) throws Exception {
		this(id, label, null);
	}

	public Widget(final String id, final String label, final String classs) throws Exception {
		if (id == null || id.length() == 0) {
			throw new Exception("Widget: missing id");
		}
		if (classs != null) {
			this.classs = classs;
		} else {
			this.classs = null;
		}

		this.id = id;

		if (label != null) {
			this.label = label;
		} else {
			this.label = "";
		}
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

	abstract public boolean isSame(Widget w);
}
