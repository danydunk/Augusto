package usi.gui.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rational.test.ft.object.interfaces.TestObject;

public class Window extends Widget {

	private boolean root;
	private final boolean modal;
	// these lists are ordered by the widgets position
	private final Map<String, Widget> widgets_map;
	private List<Action_widget> action_widgets;
	private List<Input_widget> input_widgets;
	private List<Selectable_widget> selectable_widgets;

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
		this.widgets_map = new HashMap<>();
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
		this.widgets_map = new HashMap<>();
	}

	public Widget getWidget(final String id) {

		return this.widgets_map.get(id);
	}

	public boolean containsWidget(final String id) {

		return this.widgets_map.containsKey(id);
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
		this.widgets_map.put(w.getId(), w);
	}

	private void addActionWidget(final Action_widget in) {

		if (in != null && !this.widgets_map.containsKey(in.getId())) {
			this.action_widgets.add(in);
			this.action_widgets.sort(null);
		}
	}

	private void addInputWidget(final Input_widget in) {

		if (in != null && !this.widgets_map.containsKey(in.getId())) {
			this.input_widgets.add(in);
			this.input_widgets.sort(null);
		}
	}

	private void addSelectableWidget(final Selectable_widget in) {

		if (in != null && !this.widgets_map.containsKey(in.getId())) {
			this.selectable_widgets.add(in);
			this.input_widgets.sort(null);
		}
	}

	public void setAction_widgets(final List<Action_widget> in) {

		if (in != null) {

			for (final Action_widget aw : this.action_widgets) {
				this.widgets_map.remove(aw.getId());
			}

			this.action_widgets = in;

			for (final Action_widget aw : this.action_widgets) {
				this.widgets_map.put(aw.getId(), aw);
			}

			this.action_widgets.sort(null);
		}
	}

	public void setInput_widgets(final List<Input_widget> in) {

		if (in != null) {

			for (final Input_widget iw : this.input_widgets) {
				this.widgets_map.remove(iw.getId());
			}

			this.input_widgets = in;

			for (final Input_widget iw : this.input_widgets) {
				this.widgets_map.put(iw.getId(), iw);
			}
			this.input_widgets.sort(null);
		}
	}

	public void setSelectable_widgets(final List<Selectable_widget> in) {

		if (in != null) {
			for (final Selectable_widget sw : this.selectable_widgets) {
				this.widgets_map.remove(sw.getId());
			}

			this.selectable_widgets = in;
			for (final Selectable_widget sw : this.selectable_widgets) {
				this.widgets_map.put(sw.getId(), sw);
			}
			this.selectable_widgets.sort(null);
		}
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

	@Override
	public boolean isSame(final Widget w) {

		// we do not consider the label or position cause they can change over
		// time

		if (!(w instanceof Window)) {
			return false;
		}
		// if (!super.sameProperties(w)) {
		// return false;
		// }

		// same class
		if (!w.classs.equals(this.classs)) {
			return false;
		}

		// position is not reliable
		// // we use the position +- delta to match
		// final int delta = 1;
		// if (w.x > this.x + delta || w.x < this.x - delta) {
		// System.out.println("2");
		//
		// return false;
		// }
		// if (w.y > this.y + delta || w.y < this.y - delta) {
		// System.out.println("3");
		//
		// return false;
		// }

		// we consider a window to be the same if it has the same widgets
		final Window win = (Window) w;
		if (win.getActionWidgets().size() != this.getActionWidgets().size()) {
			return false;
		}
		// widgets are ordered by position
		for (int cont = 0; cont < this.getActionWidgets().size(); cont++) {
			final Action_widget aw = this.getActionWidgets().get(cont);
			if (!aw.isSame(win.getActionWidgets().get(cont))) {

				return false;
			}
		}

		if (win.getInputWidgets().size() != this.getInputWidgets().size()) {

			return false;
		}
		// widgets are ordered by position
		for (int cont = 0; cont < this.getInputWidgets().size(); cont++) {
			final Input_widget iw = this.getInputWidgets().get(cont);
			if (iw instanceof Option_input_widget) {
				final Option_input_widget oiw = (Option_input_widget) iw;
				if (!oiw.isSame(win.getInputWidgets().get(cont))) {

					return false;
				}
			} else {
				if (!iw.isSame(win.getInputWidgets().get(cont))) {

					return false;
				}
			}
		}

		if (win.getSelectableWidgets().size() != this.getSelectableWidgets().size()) {

			return false;
		}
		// widgets are ordered by position
		for (int cont = 0; cont < this.getSelectableWidgets().size(); cont++) {
			final Selectable_widget iw = this.getSelectableWidgets().get(cont);
			if (!iw.isSame(win.getSelectableWidgets().get(cont))) {

				return false;
			}
		}

		return true;
	}

	public List<Widget> getWidgets() {

		final List<Widget> widgs = new ArrayList<>();
		widgs.addAll(this.action_widgets);
		widgs.addAll(this.input_widgets);
		widgs.addAll(this.selectable_widgets);
		return widgs;
	}
}
