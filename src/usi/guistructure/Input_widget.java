package usi.guistructure;

public class Input_widget extends Widget {

	private final String text;

	public Input_widget(final String id, final String label, final String text, final String classs) throws Exception {
		super(id, label, classs);
		if (text == null) {
			this.text = "";
		} else {
			this.text = text;
		}
	}

	public Input_widget(final String id, final String label, final String text) throws Exception {
		this(id, label, text, null);
	}

	public String getText() {

		return this.text;
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Input_widget)) {
			return false;
		}
		final Input_widget iw = (Input_widget) w;
		if (!this.label.equals(iw.label)) {
			return false;
		}
		if (!this.text.equals(iw.text)) {
			return false;
		}
		if (!this.classs.equals(iw.classs)) {
			return false;
		}
		return true;
	}
}
