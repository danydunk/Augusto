package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUITestCase {

	final private List<GUIAction> actions;
	final private A4Solution solution;

	public GUITestCase(final A4Solution solution, final List<GUIAction> actions) throws Exception {

		if (actions == null || actions.size() == 0) {
			throw new Exception("GUITestCase: null or empty list of actions.");
		}
		this.solution = solution;
		this.actions = actions;
	}

	public List<GUIAction> getActions() {

		return new ArrayList<GUIAction>(this.actions);
	}

	public A4Solution getAlloySolution() {

		return this.solution;
	}

	public boolean containsAction(final GUIAction act) {

		for (final GUIAction a : this.actions) {
			if (a.isSame(act)) {
				return true;
			}
		}
		return false;
	}
}