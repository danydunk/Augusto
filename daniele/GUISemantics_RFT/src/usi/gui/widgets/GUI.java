package usi.gui.widgets;

import java.util.List;

public class GUI {

	private final List<Window> windows;
	private final Window topWindow;

	public GUI(List<Window> windows, Window topWindow) {

		this.windows = windows;
		this.topWindow = topWindow;
	}

	public List<Window> getWindows() {

		return this.windows;
	}

	public Window getTopWindow() {

		return this.topWindow;
	}
}
