package usi.gui.semantic.testcase;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;

public abstract class GUIAction {

	final private Window w;
	final private Widget wid;
	// how the window should look like after the action is performed
	final private Window oracle;

	private Window result;

	public GUIAction(final Window w, final Widget wid, final Window oracle) throws Exception {

		if (w == null) {
			throw new Exception("GUIAction: null source window.");
		}
		this.oracle = oracle;
		this.w = w;
		this.wid = wid;
	}

	public Window getWindow() {

		return this.w;
	}

	public Widget getWidget() {

		return this.wid;
	}

	public Window getOracle() {

		return this.oracle;
	}

	public void setResult(final Window result) {

		this.result = result;
	}

	public Window getResult() {

		return this.result;
	}
}
