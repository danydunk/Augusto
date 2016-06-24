package usi.guistructure;

public class Selectable_widget extends Widget {

	private final int size;

	public Selectable_widget(final String id, final String label, final int size, final String classs)
			throws Exception {
		super(id, label, classs);
		if (size < 0) {
			throw new Exception("Selectable_widget: wrong size.");
		}
		this.size = size;
	}

	public Selectable_widget(final String id, final String label, final int size) throws Exception {
		this(id, label, size, null);
	}

	public int getSize() {

		return this.size;
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Selectable_widget)) {
			return false;
		}
		final Selectable_widget sw = (Selectable_widget) w;
		if (!this.label.equals(sw.label)) {
			return false;
		}
		if (!this.classs.equals(sw.classs)) {
			return false;
		}
		if (this.size != sw.size) {
			return false;
		}
		return true;
	}
}
