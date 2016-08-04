package usi.gui.semantic.testcase;

import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class Select extends GUIAction {

	// TODO: fix this
	// semantics of selectable widgets is initialized with a null list
	// the index of the select action points to a object added during the test
	// wrt the insertion order
	// it must be adapted at run time before execution to point to the correct
	// object
	private final int index;

	public Select(final Window w, final Window oracle, final Selectable_widget sw, final int index)
			throws Exception {

		super(w, sw, oracle);
		if (index < 0 || sw == null) {
			throw new Exception("Select: wrong inputs.");
		}
		this.index = index;
	}

	public int getIndex() {

		return this.index;
	}
}
