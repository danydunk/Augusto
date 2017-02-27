package src.usi.gui.structure;

import com.rational.test.ft.object.interfaces.TestObject;

public class Action_widget extends Widget {

	public Action_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y, final int width, final int height)
			throws Exception {

		super(to, id, label, classs, x, y, width, height);
	}

	public Action_widget(final String id, final String label, final String classs, final int x,
			final int y, final int width, final int height) throws Exception {

		super(id, label, classs, x, y, width, height);
	}

	@Override
	public boolean equals(final Object o) {

		if (!(o instanceof Action_widget)) {
			return false;
		}
		final Action_widget w = (Action_widget) o;
		return w.getId().equals(this.getId());
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!super.isSame(w)) {
			return false;
		}
		if (!(w instanceof Action_widget)) {
			return false;
		}
		return true;
	}

	// @Override
	// public boolean isSimilar(final Widget w) {
	//
	// if (!(w instanceof Action_widget)) {
	// return false;
	// }
	// return this.sameProperties_weak(w);
	// }
}
