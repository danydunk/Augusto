package usi.guistructure;

public class Action_widget extends Widget {

	public Action_widget(final String id, final String label, final String classs) throws Exception {
		super(id, label, classs);
	}

	public Action_widget(final String id, final String label) throws Exception {
		this(id, label, null);
	}

	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Action_widget)) {
			return false;
		}
		final Action_widget aw = (Action_widget) w;
		if (!this.label.equals(aw.label)) {
			return false;
		}
		if (!this.classs.equals(aw.classs)) {
			return false;
		}
		return true;
	}
}
