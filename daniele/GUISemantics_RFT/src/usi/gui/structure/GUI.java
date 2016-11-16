package src.usi.gui.structure;

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
	private Multimap<String, String> staticEdgesFrom;
	private Multimap<String, String> staticEdgesTo;
	private Multimap<String, String> dynamicEdgesFrom;
	private Multimap<String, String> dynamicEdgesTo;
	private String root;

	public GUI() {

		this.windows = new HashMap<>();
		this.aw_window_mapping = new HashMap<>();

		this.staticEdgesFrom = HashMultimap.create();
		this.staticEdgesTo = HashMultimap.create();
		this.dynamicEdgesFrom = HashMultimap.create();
		this.dynamicEdgesTo = HashMultimap.create();
	}

	public boolean isStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI - isStaticEdge: wrong input.");
		}
		if (this.staticEdgesFrom.containsEntry(aw, w) && this.staticEdgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Window> getStaticForwardLinks(final String aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI - getStaticForwardLinks: wrong input.");
		}
		final Collection<String> out = this.staticEdgesFrom.get(aw);
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

	public List<Action_widget> getStaticBackwardLinks(final String w) throws Exception {

		if (w == null || !this.windows.containsKey(w)) {
			throw new Exception("GUI - getStaticBackwardLinks: wrong input.");
		}

		final Collection<String> out = this.staticEdgesTo.get(w);
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
				throw new Exception("GUI - getStaticBackwardLinks: edge not found.");
			}
			return to_return;
		} else {
			return new ArrayList<Action_widget>();
		}
	}

	public void addStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI - addStaticEdge: wrong input.");
		}
		this.staticEdgesFrom.put(aw, w);
		this.staticEdgesTo.put(w, aw);
	}

	public void removeStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI - removeStaticEdge: wrong input.");
		}
		this.staticEdgesFrom.remove(aw, w);
		this.staticEdgesTo.remove(w, aw);
	}

	public boolean isDynamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI - isDynamicEdge: wrong input.");
		}
		if (this.dynamicEdgesFrom.containsEntry(aw, w) && this.dynamicEdgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Window> getDynamicForwardLinks(final String aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI - getDynamicForwardLinks: wrong input.");
		}
		final Collection<String> out = this.dynamicEdgesFrom.get(aw);
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

	public List<Action_widget> getDynamicBackwardLinks(final String w) throws Exception {

		if (w == null || !this.windows.containsKey(w)) {
			throw new Exception("GUI: wrong input in getBackwardLinks");
		}

		final Collection<String> out = this.dynamicEdgesTo.get(w);
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
				throw new Exception("GUI - getDynamicBackwardLinks: edge not found.");
			}
			return to_return;
		} else {
			return new ArrayList<Action_widget>();
		}
	}

	public void addDynamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI: wrong input in addDynamicEdge");
		}
		this.dynamicEdgesFrom.put(aw, w);
		this.dynamicEdgesTo.put(w, aw);
	}

	public void removeDynamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI - removeDynamicEdge: wrong input.");
		}
		this.dynamicEdgesFrom.remove(aw, w);
		this.dynamicEdgesTo.remove(w, aw);
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

		this.staticEdgesFrom = HashMultimap.create();
		this.staticEdgesTo = HashMultimap.create();
		this.dynamicEdgesFrom = HashMultimap.create();
		this.dynamicEdgesTo = HashMultimap.create();

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

		// the static edges associated with this window are removed
		List<Action_widget> to_remove = new ArrayList<>(this.getStaticBackwardLinks(n.getId()));
		for (final Action_widget aw : to_remove) {
			this.removeStaticEdge(aw.getId(), n.getId());
		}

		List<String> from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final String aw : from_links) {
			for (final Window w : this.getStaticForwardLinks(aw)) {
				this.removeStaticEdge(aw, w.getId());
			}
		}

		// the dynamic edges associated with this window are removed
		to_remove = new ArrayList<>(this.getDynamicBackwardLinks(n.getId()));
		for (final Action_widget aw : to_remove) {
			this.removeDynamicEdge(aw.getId(), n.getId());
		}

		from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final String aw : from_links) {
			for (final Window w : this.getDynamicForwardLinks(aw)) {
				this.removeDynamicEdge(aw, w.getId());
			}
		}

		this.windows.remove(n.getId());
		this.setWindows(new ArrayList<Window>(this.windows.values()));
	}

	public List<Window> getWindows() {

		return new ArrayList<>(this.windows.values());
	}

	public List<Action_widget> getAction_widgets() {

		final List<Action_widget> out = new ArrayList<>();

		for (final Window pw : this.windows.values()) {
			out.addAll(pw.getActionWidgets());
		}

		return out;
	}

	public List<Input_widget> getInput_widgets() {

		final List<Input_widget> out = new ArrayList<>();

		for (final Window pw : this.windows.values()) {
			out.addAll(pw.getInputWidgets());
		}

		return out;
	}

	public List<Selectable_widget> getSelectable_widgets() {

		final List<Selectable_widget> out = new ArrayList<>();

		for (final Window pw : this.windows.values()) {
			out.addAll(pw.getSelectableWidgets());
		}

		return out;
	}

	public Window getActionWidget_Window(final String aw) {

		return this.aw_window_mapping.get(aw);
	}

	public boolean containsWindow(final String id) {

		return this.windows.containsKey(id);
	}

	public Window getWindow(final String id) {

		return this.windows.get(id);
	}

	public int getNumberOfStaticEdges() {

		return this.staticEdgesFrom.size();
	}

	public int getNumberOfDynamicEdges() {

		return this.dynamicEdgesFrom.size();
	}
}
