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

	private Map<String, Window> windows;
	private Map<String, Window> aw_window_mapping;
	private Multimap<String, String> edgesFrom;
	private Multimap<String, String> edgesTo;
	private String root;

	// TODO: dynamic edges
	public GUI() {

		this.windows = new HashMap<>();
		this.aw_window_mapping = new HashMap<>();

		this.edgesFrom = HashMultimap.create();
		this.edgesTo = HashMultimap.create();
	}

	public boolean isEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI: wrong input in isEdge");
		}
		if (this.edgesFrom.containsEntry(aw, w) && this.edgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Window> getForwardLinks(final String aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI: wrong input in staticEdgesTo");
		}
		final Collection<String> out = this.edgesFrom.get(aw);
		if (out != null) {
			final List<Window> to_return = new ArrayList<>();
			for (final String s : out) {
				to_return.add(this.windows.get(s));
			}
			return to_return;
		} else {
			return new ArrayList<Window>();
		}
	}

	public List<Action_widget> getBackwardLinks(final String w) throws Exception {

		if (w == null || !this.windows.containsKey(w)) {
			throw new Exception("GUI: wrong input in getBackwardLinks");
		}

		final Collection<String> out = this.edgesTo.get(w);
		if (out != null) {
			final List<Action_widget> to_return = new ArrayList<>();
			loop: for (final String id : out) {
				final List<Action_widget> aws = this.aw_window_mapping.get(id).getActionWidgets();
				for (final Action_widget aw : aws) {
					if (aw.getId().equals(id)) {
						to_return.add(aw);
						continue loop;
					}
				}
				throw new Exception("GUI - getBackwardLinks: edge not found.");
			}
			return to_return;
		} else {
			return new ArrayList<Action_widget>();
		}
	}

	public void addEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI: wrong input in addEdge");
		}
		this.edgesFrom.put(aw, w);
		this.edgesTo.put(w, aw);
	}

	public void removeEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI: wrong input in removeEdge");
		}
		this.edgesFrom.remove(aw, w);
		this.edgesTo.remove(w, aw);
	}

	public Window getRoot() {

		return this.windows.get(this.root);
	}

	public void setWindows(final List<Window> ws) throws Exception {

		if (ws == null) {
			throw new Exception("GUI: wrong input in setWindows");
		}
		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		this.windows = new HashMap<>();
		this.aw_window_mapping = new HashMap<>();

		this.edgesFrom = HashMultimap.create();
		this.edgesTo = HashMultimap.create();

		for (final Window w : ws) {
			this.addWindow(w);
		}
	}

	public void addWindow(final Window n) throws Exception {

		if (n == null || this.windows.containsKey(n.getId())) {
			throw new Exception("GUI: wrong input in addWindow");
		}
		this.windows.put(n.getId(), n);

		if (n.isRoot()) {
			if (this.root != null) {
				throw new Exception("GUI - addWindow: there already is a root window.");
			}
			this.root = n.getId();
		}

		for (final Action_widget aw : n.getActionWidgets()) {
			this.aw_window_mapping.put(aw.getId(), n);
		}
	}

	public void removeWindow(final Window n) throws Exception {

		if (n == null || !this.windows.containsKey(n.getId())) {
			throw new Exception("GUI: wrong input in removeWindow");
		}

		// the edges associated with this window are removed
		final List<Action_widget> to_remove = new ArrayList<>(this.getBackwardLinks(n.getId()));
		for (final Action_widget aw : to_remove) {
			this.removeEdge(aw.getId(), n.getId());
		}

		final List<String> from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final String aw : from_links) {
			for (final Window w : this.getForwardLinks(aw)) {
				this.removeEdge(aw, w.getId());
			}
		}

		this.windows.remove(n.getId());
		this.setWindows(new ArrayList<Window>(this.windows.values()));
	}

	public List<Window> getWindows() {

		return new ArrayList<>(this.windows.values());
	}

	public Window getActionWidget_Window(final String aw) {

		return this.aw_window_mapping.get(aw);
	}
}
