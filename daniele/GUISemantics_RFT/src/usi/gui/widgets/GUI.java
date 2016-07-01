package usi.gui.widgets;

import java.util.List;

public class GUI {

	private final List<Window> windows;
	private final Window root;

	public GUI(List<Window> windows, Window root) {

		this.windows = windows;
		this.root = root;
	}

	public List<Window> getWindows() {

		return this.windows;
	}

	public Window getRoot() {

		return this.root;
	}
}
