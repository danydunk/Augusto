package usi.gui.semantic.testcase;

import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class Select_doubleclick extends GUIAction {

	// TODO: fix this
	// semantics of selectable widgets is initialized with a null list
	// the index of the select action points to a object added during the test
	// wrt the insertion order
	// it must be adapted at run time before execution to point to the correct
	// object
	private final int index;
	private final boolean abs;

	@Override
	public boolean isSame(final GUIAction act) {

		if (!(act instanceof Select)) {
			return false;
		}
		final Select s = (Select) act;
		if (s.getIndex() != this.index) {
			return false;
		}
		return this.same(act);
	}

	public Select_doubleclick(final Window w, final Window oracle, final Selectable_widget sw,
			final int index, final boolean abs) throws Exception {

		super(w, sw, oracle);
		if (index < 0 || sw == null) {
			throw new Exception("Select: wrong inputs.");
		}
		this.abs = abs;
		this.index = index;
	}

	public int getIndex() {

		return this.index;
	}

	public boolean isAbstract() {

		return this.abs;
	}
}
