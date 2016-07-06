package usi.gui.structure;

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
	// private List<Action_widget> action_widgets;
	// private List<Input_widget> input_widgets;
	// private List<Selectable_widget> selectable_widgets;
	private Map<Action_widget, Window> aw_window_mapping;
	private final Multimap<Action_widget, Window> staticEdgesFrom;
	private final Multimap<Window, Action_widget> staticEdgesTo;
	private Window root;

	// TODO: dynamic edges
	public GUI() {

		this.windows = new ArrayList<>();
		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		this.aw_window_mapping = new HashMap<>();

		this.staticEdgesFrom = HashMultimap.create();
		this.staticEdgesTo = HashMultimap.create();
	}

	public boolean isStaticEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w)
				|| !this.aw_window_mapping.keySet().contains(aw)) {
			throw new Exception("GUI: wrong input in isEdge");
		}
		if (this.staticEdgesFrom.containsEntry(aw, w) && this.staticEdgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public Collection<Window> getStaticForwardLinks(final Action_widget aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.keySet().contains(aw)) {
			throw new Exception("GUI: wrong input in getFrwardLinks");
		}
		final Collection<Window> out = this.staticEdgesFrom.get(aw);
		if (out != null) {
			return out;
		} else {
			return new ArrayList<Window>();
		}
	}

	public Collection<Action_widget> getStaticBackwardLinks(final Window w) throws Exception {

		if (w == null || !this.windows.contains(w)) {
			throw new Exception("GUI: wrong input in getBackwardLinks");
		}

		final Collection<Action_widget> out = this.staticEdgesTo.get(w);
		if (out != null) {
			return out;
		} else {
			return new ArrayList<Action_widget>();
		}
	}

	public void addStaticEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w)
				|| !this.aw_window_mapping.keySet().contains(aw)) {
			throw new Exception("GUI: wrong input in addEdge");
		}
		this.staticEdgesFrom.put(aw, w);
		this.staticEdgesTo.put(w, aw);
	}

	public void removeStaticEdge(final Action_widget aw, final Window w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI: wrong input in removeEdge");
		}
		this.staticEdgesFrom.remove(aw, w);
		this.staticEdgesTo.remove(w, aw);
	}

	public void setRoot(final Window n) throws Exception {

		if (n == null || !this.windows.contains(n)) {
			throw new Exception("GUI: wrong input in setInitial");
		}
		this.root = n;
	}

	public Window getRoot() {

		return this.root;
	}

	public void setWindows(final Collection<Window> ws) throws Exception {

		if (ws == null) {
			throw new Exception("GUI: wrong input in setWindows");
		}
		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
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

		// this.input_widgets.addAll(n.getInputWidgets());
		// this.selectable_widgets.addAll(n.getSelectableWidgets());
		for (final Action_widget aw : n.getActionWidgets()) {
			this.aw_window_mapping.put(aw, n);
			// this.action_widgets.add(aw);
		}
	}

	public void removeWindow(final Window n) throws Exception {

		if (n == null || !this.windows.contains(n)) {
			throw new Exception("GUI: wrong input in removeWindow");
		}

		// the edges associated with this window are removed
		final List<Action_widget> to_remove = new ArrayList<>(this.getStaticBackwardLinks(n));
		for (final Action_widget aw : to_remove) {
			this.removeStaticEdge(aw, n);
		}

		final List<Action_widget> from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final Action_widget aw : from_links) {
			for (final Window w : this.getStaticForwardLinks(aw)) {
				this.removeStaticEdge(aw, w);
			}
		}

		this.windows.remove(n);
		this.setWindows(this.windows);
	}

	public List<Window> getWindows() {

		return new ArrayList<>(this.windows);
	}

	// public List<Action_widget> getAction_widgets() {
	//
	// return new ArrayList<>(this.action_widgets);
	// }
	//
	// public List<Input_widget> getInput_widgets() {
	//
	// return new ArrayList<>(this.input_widgets);
	// }
	//
	// public List<Selectable_widget> getSelectable_widgets() {
	//
	// return new ArrayList<>(this.selectable_widgets);
	// }

	public Window getActionWidget_Window(final Action_widget aw) {

		return this.aw_window_mapping.get(aw);
	}
}
