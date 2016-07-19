package usi.gui.semantic.testcase;

import usi.gui.structure.Action_widget;
import usi.gui.structure.Window;

public class Click extends GUIAction {

	public Click(final Window w, final Window oracle, final Action_widget aw) throws Exception {

		super(w, aw, oracle);
		if (aw == null) {
			throw new Exception("Click: null button.");
		}
	}

}
