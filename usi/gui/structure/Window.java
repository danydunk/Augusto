package usi.gui.structure;

import java.util.ArrayList;
import java.util.List;

import com.rational.test.ft.object.interfaces.TestObject;

public class Window extends Widget {

	private boolean root;
	private final boolean modal;
	private List<Action_widget> action_widgets;
	private List<Input_widget> input_widgets;
	private List<Selectable_widget> selectable_widgets;

	// private List<Container> containers;

	public Window(final TestObject to, final String id, final String label, final String classs,
			final int x, final int y, final boolean modal) throws Exception {

		super(to, id, label, classs, x, y);
		if (id == null || id.length() == 0) {
			throw new Exception("Window: missing id");
		}

		this.root = false;
		this.modal = modal;
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
	}

	public Window(final String id, final String label, final String classs, final int x,
			final int y, final boolean modal) throws Exception {

		super(id, label, classs, x, y);
		if (id == null || id.length() == 0) {
			throw new Exception("Window: missing id");
		}

		this.root = false;
		this.modal = modal;
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
	}

	public void addWidget(final Widget w) {

		if (w instanceof Action_widget) {
			this.addActionWidget((Action_widget) w);
		}
		if (w instanceof Input_widget) {
			this.addInputWidget((Input_widget) w);
		}
		if (w instanceof Selectable_widget) {
			this.addSelectableWidget((Selectable_widget) w);
		}
	}

	private void addActionWidget(final Action_widget in) {

		if (in != null) {
			this.action_widgets.add(in);
		}
	}

	private void addInputWidget(final Input_widget in) {

		if (in != null) {
			this.input_widgets.add(in);
		}
	}

	private void addSelectableWidget(final Selectable_widget in) {

		if (in != null) {
			this.selectable_widgets.add(in);
		}
	}

	public void setAction_widgets(final List<Action_widget> in) {

		if (in == null) {
			this.action_widgets = new ArrayList<>();
		}
		this.action_widgets = in;
	}

	public void setInput_widgets(final List<Input_widget> in) {

		if (in == null) {
			this.input_widgets = new ArrayList<>();
		}
		this.input_widgets = in;
	}

	public void setSelectable_widgets(final List<Selectable_widget> in) {

		if (in == null) {
			this.selectable_widgets = new ArrayList<>();
		}
		this.selectable_widgets = in;
	}

	public boolean isModal() {

		return this.modal;
	}

	public boolean isRoot() {

		return this.root;
	}

	public void setRoot(final boolean root) {

		this.root = root;
	}

	public List<Action_widget> getActionWidgets() {

		return new ArrayList<>(this.action_widgets);
	}

	public List<Input_widget> getInputWidgets() {

		return new ArrayList<>(this.input_widgets);
	}

	public List<Selectable_widget> getSelectableWidgets() {

		return new ArrayList<>(this.selectable_widgets);
	}

	// TODO: implement this
	@Override
	public boolean isSame(final Widget w) {

		if (!(w instanceof Window)) {
			return false;
		}
		return true;
	}

}
