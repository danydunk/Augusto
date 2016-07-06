package usi.gui.structure;

import com.rational.test.ft.object.interfaces.TestObject;

public class Option_input_widget extends Input_widget {

	private final int size;
	private final int selected;

	public Option_input_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y, final int size, final int selected)
					throws Exception {

		super(to, id, label, classs, x, y, String.valueOf(selected));
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Option_input_widget: wrong size or selected.");
		}
		this.size = size;
		this.selected = selected;
	}

	public Option_input_widget(final String id, final String label, final String classs,
			final int x, final int y, final int size, final int selected) throws Exception {

		super(id, label, classs, x, y, String.valueOf(selected));
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Option_input_widget: wrong size or selected.");
		}
		this.size = size;
		this.selected = selected;
	}

	public int getSize() {

		return this.size;
	}

	public int getSelected() {

		return this.selected;
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Option_input_widget)) {
			return false;
		}
		final Option_input_widget oiw = (Option_input_widget) w;
		if (this.size != oiw.size) {
			return false;
		}
		// size and selected is not checked cause it can change
		return super.isSame(w);
	}
}
