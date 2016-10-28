package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;

public class GUITestCase {

	final private List<GUIAction> actions;
	final private A4Solution solution;
	final private String run_command;

	public GUITestCase(final A4Solution solution, final List<GUIAction> actions,
			final String run_command) throws Exception {

		if (actions == null || actions.size() == 0) {
			throw new Exception("GUITestCase: empty actions or null initial window.");
		}

		this.run_command = run_command;
		this.solution = solution;
		this.actions = actions;
	}

	public List<GUIAction> getActions() {

		return new ArrayList<GUIAction>(this.actions);
	}

	public A4Solution getAlloySolution() {

		return this.solution;
	}

	public String getRunCommand() {

		return this.run_command;
	}

	public boolean containsAction(final GUIAction act) {

		for (final GUIAction a : this.actions) {
			if (a.isSame(act)) {
				return true;
			}
		}
		return false;
	}

	public boolean isSame(final GUITestCase tc) {

		if (this.getActions().size() != tc.getActions().size()) {
			return false;
		}
		for (int cont = 0; cont < tc.getActions().size(); cont++) {
			final GUIAction act1 = tc.getActions().get(cont);
			final GUIAction act2 = this.getActions().get(cont);

			if (!act1.getWidget().getId().equals(act2.getWidget().getId())
					|| !act1.getWindow().getId().equals(act2.getWindow().getId())) {
				return false;
			}

			if (act1 instanceof Click) {

				if (!(act2 instanceof Click)) {
					return false;
				}
			}
			if (act1 instanceof Fill) {

				if (!(act2 instanceof Fill)) {
					return false;
				}
				final Fill fill1 = (Fill) act1;
				final Fill fill2 = (Fill) act2;

				if (!fill1.getInput().equals(fill2.getInput())) {
					return false;
				}

			}
			if (act1 instanceof Select) {

				if (!(act2 instanceof Select)) {
					return false;
				}
				final Select select1 = (Select) act1;
				final Select select2 = (Select) act2;

				if (select1.getIndex() != select2.getIndex()) {
					return false;
				}
			}

		}
		return true;
	}
}