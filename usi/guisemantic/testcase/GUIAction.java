package usi.guisemantic.testcase;


import usi.gui.structure.Widget;
import usi.gui.structure.Window;

public abstract class GUIAction {

	final private Widget w;
	// how the window should look like after the action is performed

	final private Window oracle;

	public GUIAction(final Widget w, final Window oracle) throws Exception {
		if (w == null) {
			throw new Exception("GUIAction: null widget.");
		}
		this.oracle = oracle;
		this.w = w;
	}

	public Widget getWidget() {

		return this.w;
	}

	public Window getOracle() {

		return this.oracle;
	}
}
