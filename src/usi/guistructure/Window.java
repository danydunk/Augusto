package usi.guistructure;

import java.util.ArrayList;
import java.util.List;

public class Window extends Widget {

	private final boolean root;
	private final boolean modal;
	private List<Action_widget> action_widgets;
	private List<Input_widget> input_widgets;
	private List<Selectable_widget> selectable_widgets;
	// private List<Container> containers;

	public Window(final String id, final boolean modal, final String title, final boolean root) throws Exception {
		super(id, title);
		if (id == null || id.length() == 0) {
			throw new Exception("Window: missing id");
		}

		this.root = root;
		this.modal = modal;
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		// containers = new ArrayList<>();
	}

	public Window(final String id, final String title) throws Exception {
		this(id, false, title, false);
	}

	// public List<Container> getContainers() {
	// return containers;
	// }

	// public void addContainer(Container c) throws Exception {
	// if(c == null || containers.contains(c))
	// throw new Exception("Window: wrong input in addContainer");
	// containers.add(c);
	// }

	// public void removeContainer(Container c) throws Exception {
	// if(c == null || !containers.contains(c))
	// throw new Exception("Window: wrong input in addContainer");
	// containers.remove(c);
	// }

	public void addActionWidget(final Action_widget in) {

		if (in != null) {
			this.action_widgets.add(in);
		}
	}

	public void addInputWidget(final Input_widget in) {

		if (in != null) {
			this.input_widgets.add(in);
		}
	}

	public void addSelectableWidget(final Selectable_widget in) {

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
