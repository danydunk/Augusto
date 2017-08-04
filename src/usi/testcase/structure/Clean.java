package src.usi.testcase.structure;

import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Window;

public class Clean extends GUIAction {

	public Clean(final Window w, final Input_widget iw) throws Exception {

		super(w, iw, null);
		if (iw == null) {
			throw new Exception("Clean: null inputs.");
		}
	}

	@Override
	public boolean isSame(final GUIAction act) {

		if (!(act instanceof Clean)) {
			return false;
		}
		return this.same(act);
	}

}
