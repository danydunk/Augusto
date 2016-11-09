package usi.testcase.structure;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;


public abstract class GUIAction {

	final private Window w;
	final private Widget wid;
	// how the window should look like after the action is performed
	final private Window oracle;

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

	protected boolean same(final GUIAction act) {

		if (!act.getWidget().isSame(this.getWidget())) {
			return false;
		}
		if (!act.getWindow().isSame(this.getWindow())) {
			return false;
		}
		if (act.getOracle() != null && this.getOracle() == null) {
			return false;
		}
		if (act.getOracle() == null && this.getOracle() != null) {
			return false;
		}
		if (act.getOracle() != null && this.getOracle() != null
				&& !act.getOracle().isSame(this.getOracle())) {
			return false;
		}
		return true;
	}

	abstract public boolean isSame(GUIAction act);
}
