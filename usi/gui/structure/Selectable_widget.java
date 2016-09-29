package usi.gui.structure;

import com.rational.test.ft.object.interfaces.TestObject;

public class Selectable_widget extends Widget {

	private final int size;
	private final int selected;

	public Selectable_widget(final TestObject to, final String id, final String label,
			final String classs, final int x, final int y, final int size, final int selected)
					throws Exception {

		super(to, id, label, classs, x, y);
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Selectable_widget: wrong size or selected.");
		}
		this.size = size;
		this.selected = selected;
	}

	public Selectable_widget(final String id, final String label, final String classs, final int x,
			final int y, final int size, final int selected) throws Exception {

		super(id, label, classs, x, y);
		if (size < 0 || selected < -1 || selected > size - 1) {
			throw new Exception("Selectable_widget: wrong size or selected.");
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

		if (!(w instanceof Selectable_widget)) {
			return false;
		}

		// size and selected is not checked cause it can change
		return this.sameProperties_strong(w);
	}

	@Override
	public boolean isSimilar(final Widget w) {

		if (!(w instanceof Selectable_widget)) {
			return false;
		}

		// size and selected is not checked cause it can change
		return this.sameProperties_weak(w);
	}
}
