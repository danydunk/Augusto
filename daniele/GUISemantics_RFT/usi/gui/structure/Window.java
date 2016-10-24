package usi.gui.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		// System.out.println("CHECKING " + this.id + " " + this.label +
		// " WITH " + w.id + " "
		// + w.label);
		if (!(w instanceof Window)) {
			return false;
		}

		// for window we cannot relay on sameProperties_weak beacuse labels can
		// change
		// position and label can vary

		if (w.label == null && this.label != null) {
			return false;
		}
		if (w.label != null && this.label == null) {
			return false;
		}

		if (w.descriptor == null && this.descriptor != null) {
			return false;
		}
		if (w.descriptor != null && this.descriptor == null) {
			return false;
		}
		if (w.descriptor != null && w.descriptor.length() > 0 && this.descriptor.length() == 0) {
			return false;
		}
		if (w.descriptor != null && w.descriptor.length() == 0 && this.descriptor.length() > 0) {
			return false;
		}

		// same class
		if (!w.classs.equals(this.classs)) {
			return false;
		}

		// we consider a window to be the same if it has the same widgets
		final Window win = (Window) w;

		List<Widget> widgets = this.getWidgets();
		List<Widget> widgets_bis = win.getWidgets();
		// TODO: is there a better way to do it?
		// when checking whether 2 windows are the same we skip the elements
		// under the menu window (it changes according to the windows open)
		widgets = widgets
				.stream()
				.filter(e -> {
					if (e instanceof Action_widget
							&& e.getClasss().toLowerCase().equals("menuitemui")
							&& e.getLabel().toLowerCase().startsWith("window -")) {
						return false;
					}
					return true;
				}).collect(Collectors.toList());

		widgets_bis = widgets_bis
				.stream()
				.filter(e -> {
					if (e instanceof Action_widget
							&& e.getClasss().toLowerCase().equals("menuitemui")
							&& e.getLabel().toLowerCase().startsWith("window -")) {
						return false;
					}
					return true;
				}).collect(Collectors.toList());

		if (widgets.size() != widgets_bis.size()) {
			return false;
		}
		// we iterate trough the widgets which are ordered by position
		for (int x = 0; x < widgets.size(); x++) {
			if (widgets.get(x) instanceof Action_widget) {
				final Action_widget aw = (Action_widget) widgets.get(x);
				if (!aw.isSimilar(widgets_bis.get(x))) {
					return false;
				}
			} else if (widgets.get(x) instanceof Input_widget) {
				if (widgets.get(x) instanceof Option_input_widget) {
					final Option_input_widget oiw = (Option_input_widget) widgets.get(x);
					if (!oiw.isSimilar(widgets_bis.get(x))) {
						return false;
					}
				} else {
					final Input_widget iw = (Input_widget) widgets.get(x);
					if (!iw.isSimilar(widgets_bis.get(x))) {
						return false;
					}
				}
			} else if (widgets.get(x) instanceof Selectable_widget) {
				final Selectable_widget sw = (Selectable_widget) widgets.get(x);
				if (!sw.isSimilar(widgets_bis.get(x))) {
					return false;
				}
			}
		}

		return true;
	}

	public List<Widget> getWidgets() {

		final List<Widget> widgs = new ArrayList<>();
		widgs.addAll(this.action_widgets);
		widgs.addAll(this.input_widgets);
		widgs.addAll(this.selectable_widgets);
		Collections.sort(widgs);
		return widgs;
	}

	@Override
	public boolean isSimilar(final Widget w) {

		return super.sameProperties_weak(w);
	}
}
