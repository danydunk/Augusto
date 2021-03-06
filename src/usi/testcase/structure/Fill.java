package src.usi.testcase.structure;

import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Window;

public class Fill extends GUIAction {

	final private String input;

	public Fill(final Window w, final Window oracle, final Input_widget iw, final String input)
			throws Exception {

		super(w, iw, oracle);
		if (iw == null) {
			throw new Exception("Fill: null inputs.");
		}
		this.input = input;
	}

	public String getInput() {

		return this.input;
	}

	@Override
	public boolean isSame(final GUIAction act) {

		if (!(act instanceof Fill)) {
			return false;
		}
		final Fill f = (Fill) act;
		if ((f.getInput() != null && !f.getInput().equals(this.input))
				|| (f.getInput() == null && this.input != null)) {
			return false;
		}
		return this.same(act);
	}
}
