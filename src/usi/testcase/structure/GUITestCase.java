package src.usi.testcase.structure;

import java.util.ArrayList;
import java.util.List;

public class GUITestCase {

	final private List<GUIAction> actions;
	final private String semantic_properties;

	public GUITestCase(final List<GUIAction> actions, final String semantic_properties)
			throws Exception {

		if (actions == null || actions.size() == 0) {
			throw new Exception("GUITestCase: empty actions or null initial window.");
		}

		this.actions = actions;
		this.semantic_properties = semantic_properties;
	}

	public String getSemanticProperty() {

		return this.semantic_properties;
	}

	public List<GUIAction> getActions() {

		return new ArrayList<GUIAction>(this.actions);
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

				if ((fill1.getInput() != null && !fill1.getInput().equals(fill2.getInput()))
						|| (fill1.getInput() == null && fill2.getInput() != null)) {
					return false;
				}

			}
			if (act1 instanceof Clean) {

				if (!(act2 instanceof Clean)) {
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

	@Override
	public String toString() {

		String out = "TESTCASE SIZE = " + this.getActions().size();
		int cont = 1;
		for (final GUIAction act : this.getActions()) {
			if (act instanceof Clean) {
				continue;
			}
			out += System.lineSeparator();
			out += "ACTION " + cont;
			out += System.lineSeparator();

			if (act instanceof Click) {
				out += "CLICK " + act.getWidget().getId() + " - " + act.getWidget().getLabel();
			} else if (act instanceof Fill) {
				final Fill f = (Fill) act;
				out += "FILL " + f.getWidget().getId() + " - " + act.getWidget().getDescriptor()
						+ " WITH " + f.getInput();
			} else if (act instanceof Select) {
				final Select s = (Select) act;
				out += "SELECT " + s.getWidget().getId() + " WITH " + s.getIndex();
			}
			cont++;
		}
		out += System.lineSeparator();
		return out;
	}
}