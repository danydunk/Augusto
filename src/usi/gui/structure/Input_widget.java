package src.usi.gui.structure;

import com.rational.test.ft.object.interfaces.TestObject;

public class Input_widget extends Widget {

	private final String value;

	public Input_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y, final int width, final int height,
			final String value) throws Exception {

		super(to, id, label, classs, x, y, width, height);
		if (value == null) {
			this.value = "";
		} else {
			this.value = value;
		}
	}

	public Input_widget(final String id, final String label, final String classs, final int x,
			final int y, final int width, final int height, final String value) throws Exception {

		super(id, label, classs, x, y, width, height);
		if (value == null) {
			this.value = "";
		} else {
			this.value = value;
		}
	}

	@Override
	public boolean equals(final Object o) {

		if (!(o instanceof Input_widget)) {
			return false;
		}
		final Input_widget w = (Input_widget) o;
		return w.getId().equals(this.getId());
	}

	public String getValue() {

		return this.value;
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!super.isSame(w)) {
			return false;
		}
		if (!(w instanceof Input_widget)) {
			return false;
		}

		return true;
	}

	// @Override
	// public boolean isSimilar(final Widget w) {
	//
	// if (!(w instanceof Input_widget)) {
	// return false;
	// }
	//
	// return this.sameProperties_weak(w);
	// }
}
