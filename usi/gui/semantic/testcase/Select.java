package usi.gui.semantic.testcase;

import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

public class Select extends GUIAction {

	final private int index;

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
