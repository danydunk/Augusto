package usi.gui.structure;

import com.rational.test.ft.object.interfaces.TestObject;

public class Action_widget extends Widget {

	public Action_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y) throws Exception {

		super(to, id, label, classs, x, y);
	}

	public Action_widget(final String id, final String label, final String classs, final int x,
			final int y) throws Exception {

		super(id, label, classs, x, y);
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Action_widget)) {
			return false;
		}
		return this.sameProperties_strong(w);
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
