package usi.gui.semantic.testcase;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;

public class Fill extends GUIAction {

	final private String input;

	public Fill(final Widget w, final Window oracle, final String input) throws Exception {
		super(w, oracle);
		if (input == null) {
			throw new Exception("Fill: null input.");
		}
		this.input = input;
	}

	public String getInput() {

		return this.input;
	}
}
