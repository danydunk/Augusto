package usi.guipattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import usi.guisemantic.FunctionalitySemantics;
import usi.guisemantic.alloy.AlloyUtil;

public class GUI_Pattern {

	private final String GUI_SEMANTICS_PATH = "./resources/alloy/GUI_general.als";

	private List<Pattern_window> windows;
	// private Map<String, Pattern_container> containers;
	private List<Pattern_action_widget> action_widgets;
	private List<Pattern_input_widget> input_widgets;
	private List<Pattern_selectable_widget> selectable_widgets;
	private Map<Pattern_action_widget, Pattern_window> aw_window_mapping;
	private final Multimap<Pattern_action_widget, Pattern_window> edgesFrom;
	private final Multimap<Pattern_window, Pattern_action_widget> edgesTo;
	private FunctionalitySemantics semantics;

	public GUI_Pattern() {
		this.windows = new ArrayList<>();
		this.edgesFrom = HashMultimap.create();
		this.edgesTo = HashMultimap.create();
		this.aw_window_mapping = new HashMap<>();
		// containers = new HashMap<>();
		this.action_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
	}

	public boolean isEdge(final Pattern_action_widget aw, final Pattern_window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w) || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI_Pattern: wrong input in isEdge");
		}
		if (this.edgesFrom.containsEntry(aw, w) && this.edgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Pattern_window> getForwardLinks(final Pattern_action_widget aw) throws Exception {

		if (aw == null || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI_Pattern: wrong input in getFrwardLinks");
		}

		final Collection<Pattern_window> out = this.edgesFrom.get(aw);
		if (out != null) {
			return new ArrayList<>(out);
		} else {
			return new ArrayList<Pattern_window>();
		}
	}

	public List<Pattern_action_widget> getBackwardLinks(final Pattern_window w) throws Exception {

		if (w == null || !this.windows.contains(w)) {
			throw new Exception("GUI_Pattern: wrong input in getBackwardLinks");
		}

		final Collection<Pattern_action_widget> out = this.edgesTo.get(w);
		if (out != null) {
			return new ArrayList<>(out);
		} else {
			return new ArrayList<Pattern_action_widget>();
		}
	}

	public void addEdge(final Pattern_action_widget aw, final Pattern_window w) throws Exception {

		if (aw == null || w == null || !this.windows.contains(w) || !this.action_widgets.contains(aw)) {
			throw new Exception("GUI_Pattern: wrong input in addEdge");
		}
		this.edgesFrom.put(aw, w);
		this.edgesTo.put(w, aw);
	}

	public void removeEdge(final Pattern_action_widget aw, final Pattern_window w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI_Pattern: wrong input in removeEdge");
		}
		this.edgesFrom.remove(aw, w);
		this.edgesTo.remove(w, aw);
	}

	public void setWindows(final List<Pattern_window> ws) throws Exception {

		if (ws == null) {
			throw new Exception("GUI_Pattern: wrong input in setWindows");
		}
		// containers = new HashMap<>();
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();
		this.aw_window_mapping = new HashMap<>();
		this.windows = new ArrayList<>();

		for (final Pattern_window w : ws) {
			this.addWindow(w);
		}
	}

	public void addWindow(final Pattern_window n) throws Exception {

		if (n == null || this.windows.contains(n)) {
			throw new Exception("GUI_Pattern: wrong input in addWindow");
		}
		this.windows.add(n);

		// for(Pattern_container c : n.getContainers()) {
		// containers.put(c.getId(), c);
		//
		// for(Pattern_input_widget iw : c.getInput_widgets())
		// input_widgets.put(iw.getId(), iw);
		// for(Pattern_selectable_widget sw : c.getSelectable_widgets())
		// selectable_widgets.put(sw.getId(), sw);
		// for(Pattern_action_widget aw : c.getAction_widgets())
		// action_widgets.put(aw.getId(), aw);
		// }
		this.action_widgets.addAll(n.getActionWidgets());
		this.input_widgets.addAll(n.getInputWidgets());
		this.selectable_widgets.addAll(n.getSelectableWidgets());
		for (final Pattern_action_widget paw : n.getActionWidgets()) {
			this.aw_window_mapping.put(paw, n);
		}
	}

	public void removeWindow(final Pattern_window n) throws Exception {

		if (n == null || !this.windows.contains(n)) {
			throw new Exception("GUI_Pattern: wrong input in removeWindow");
		}
		this.windows.remove(n);
		this.setWindows(this.windows);
	}

	public List<Pattern_window> getWindows() {

		return new ArrayList<>(this.windows);
	}

	// public Map<String, Pattern_container> getContainers() {
	// return containers;
	// }
	//
	// public void addContainer(Pattern_container c) throws Exception {
	// if(c == null || containers.containsKey(c.getId()))
	// throw new Exception("GUI_Pattern: wrong input in addContainer");
	// containers.put(c.getId(), c);
	// }
	//
	// public void removeContainer(Pattern_container c) throws Exception {
	// if(c == null || !containers.containsKey(c.getId()))
	// throw new Exception("GUI_Pattern: wrong input in removeContainer");
	// containers.remove(c);
	// }

	public List<Pattern_action_widget> getAction_widgets() {

		return new ArrayList<>(this.action_widgets);
	}

	public List<Pattern_input_widget> getInput_widgets() {

		return new ArrayList<>(this.input_widgets);
	}

	public Map<Pattern_action_widget, Pattern_window> getAw_window_mapping() {

		return new HashMap<>(this.aw_window_mapping);
	}

	public FunctionalitySemantics getSemantics() {

		return this.semantics;
	}

	public void setSemantics(final FunctionalitySemantics in) {

		this.semantics = in;
	}

	public void loadSemantics(final String filename) throws Exception {

		final File gui_model = new File(this.GUI_SEMANTICS_PATH);
		final File alloy_metamodel = new File("./resources/alloy/" + filename);
		if (alloy_metamodel.exists() && !alloy_metamodel.isDirectory() && gui_model.exists()
				&& !gui_model.isDirectory()) {
			// the content of the 2 files is read
			BufferedReader br = new BufferedReader(new FileReader(gui_model));
			String gui_s = "";
			String s;
			while ((s = br.readLine()) != null) {
				gui_s += s + System.getProperty("line.separator");
			}
			br.close();
			br = new BufferedReader(new FileReader(alloy_metamodel));
			String func_s = "";
			while ((s = br.readLine()) != null) {
				func_s += s + System.getProperty("line.separator");
			}
			br.close();
			// the two files are merged
			final String model = gui_s + func_s;
			final FunctionalitySemantics semantics = FunctionalitySemantics
					.instantiate(AlloyUtil.loadAlloyModelFromString(model));
			this.semantics = semantics;
		}
	}
}
