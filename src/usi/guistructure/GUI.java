package usi.guistructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class GUI {

	private List<Window> windows;
	// private Map<String, Container> containers;
	private List<Action_widget> action_widgets;
	private List<Input_widget> input_widgets;
	private List<Selectable_widget> selectable_widgets;
	private Map<Action_widget, Window> aw_window_mapping;
	private final Multimap<Action_widget, Window> edgesFrom;
	private final Multimap<Window, Action_widget> edgesTo;

	private Window initial;

	public GUI() {
		this.windows = new ArrayList<>();
		// containers = new HashMap<>();
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.aw_window_mapping = new HashMap<>();

		this.edgesFrom = HashMultimap.create();
		this.edgesTo = HashMultimap.create();
	}

	public boolean isEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w) || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI: wrong input in isEdge");
		}
		if (this.edgesFrom.containsEntry(aw, w) && this.edgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public Collection<Window> getForwardLinks(final Action_widget aw) throws Exception {

		if (aw == null || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI: wrong input in getFrwardLinks");
		}
		final Collection<Window> out = this.edgesFrom.get(aw);
		if (out != null) {
			return out;
		} else {
			return new ArrayList<Window>();
		}
	}

	public Collection<Action_widget> getBackwardLinks(final Window w) throws Exception {

		if (w == null || !this.windows.contains(w)) {
			throw new Exception("GUI: wrong input in getBackwardLinks");
		}

		final Collection<Action_widget> out = this.edgesTo.get(w);
		if (out != null) {
			return out;
		} else {
			return new ArrayList<Action_widget>();
		}
	}

	public void addEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w) || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI: wrong input in addEdge");
		}
		this.edgesFrom.put(aw, w);
		this.edgesTo.put(w, aw);
	}

	public void removeEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI: wrong input in removeEdge");
		}
		this.edgesFrom.remove(aw, w);
		this.edgesTo.remove(w, aw);
	}

	public void setInitial(final Window n) throws Exception {

		if (n == null || !this.windows.contains(n)) {
			throw new Exception("GUI: wrong input in setInitial");
		}
		this.initial = n;
	}

	public Window getInitial() {

		return this.initial;
	}

	public void setWindows(final Collection<Window> ws) throws Exception {

		if (ws == null) {
			throw new Exception("GUI: wrong input in setWindows");
		}
		// containers = new HashMap<>();
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.windows = new ArrayList<>();
		this.aw_window_mapping = new HashMap<>();

		for (final Window w : ws) {
			this.addWindow(w);
		}
	}

	public void addWindow(final Window n) throws Exception {

		if (n == null || this.windows.contains(n)) {
			throw new Exception("GUI: wrong input in addWindow");
		}
		this.windows.add(n);

		// for(Container c : n.getContainers()) {
		// containers.put(c.getId(), c);
		//
		// for(Input_widget iw : c.getInput_widgets())
		// input_widgets.put(iw.getId(), iw);
		// for(Selectable_widget sw : c.getSelectable_widgets())
		// selectable_widgets.put(sw.getId(), sw);
		// for(Action_widget aw : c.getAction_widgets()) {
		// action_widgets.put(aw.getId(), aw);
		// aw_window_mapping.put(aw, n);
		// }
		// this.action_widgets.addAll(n.getActionWidgets());
		this.input_widgets.addAll(n.getInputWidgets());
		this.selectable_widgets.addAll(n.getSelectableWidgets());
		for (final Action_widget aw : n.getActionWidgets()) {
			this.aw_window_mapping.put(aw, n);
			this.action_widgets.add(aw);
		}
	}

	public void removeWindow(final Window n) throws Exception {

		if (n == null || !this.windows.contains(n)) {
			throw new Exception("GUI: wrong input in removeWindow");
		}

		// the edges associated with this window are removed
		final List<Action_widget> to_remove = new ArrayList<>(this.getBackwardLinks(n));
		for (final Action_widget aw : to_remove) {
			this.removeEdge(aw, n);
		}

		final List<Action_widget> from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final Action_widget aw : from_links) {
			for (final Window w : this.getForwardLinks(aw)) {
				this.removeEdge(aw, w);
			}
		}

		this.windows.remove(n);
		this.setWindows(this.windows);
	}

	// public Map<String, Container> getContainers() {
	// return containers;
	// }

	public List<Window> getWindows() {

		return new ArrayList<>(this.windows);
	}

	public List<Action_widget> getAction_widgets() {

		return new ArrayList<>(this.action_widgets);
	}

	public List<Input_widget> getInput_widgets() {

		return new ArrayList<>(this.input_widgets);
	}

	public List<Selectable_widget> getSelectable_widgets() {

		return new ArrayList<>(this.selectable_widgets);
	}

	public Window getActionWidget_Window(final Action_widget aw) {

		return this.aw_window_mapping.get(aw);
	}
}
