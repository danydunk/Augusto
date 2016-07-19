package usi.gui.semantic.testcase;

import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;

public class Fill extends GUIAction {

	final private String input;

	public Fill(final Window w, final Window oracle, final Input_widget iw, final String input)
			throws Exception {

		super(w, iw, oracle);
		if (input == null || iw == null) {
			throw new Exception("Fill: null inputs.");
		}
		this.input = input;
	}

	public String getInput() {

		return this.input;
	}
}
