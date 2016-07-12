package usi.gui.semantic.testcase;

import java.util.List;

public class GUITestCase {

	final private List<GUIAction> actions;

	public GUITestCase(final List<GUIAction> actions) throws Exception {
		if (actions == null || actions.size() == 0) {
			throw new Exception("GUITestCase: null or empty list of actions.");
		}
		this.actions = actions;
	}

	public List<GUIAction> getActions() {

		return this.actions;
	}
}