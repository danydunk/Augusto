package usi.guisemantic.testcase;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;

public class Select extends GUIAction {

	final private int index;

	public Select(final Widget w, final Window oracle, final int index) throws Exception {
		super(w, oracle);
		if (index < 0) {
			throw new Exception("Select: null index.");
		}
		this.index = index;
	}

	public int getIndex() {

		return this.index;
	}
}
