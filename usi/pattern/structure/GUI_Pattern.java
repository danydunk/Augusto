package usi.pattern.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import usi.configuration.PathsManager;
import usi.gui.functionality.instance.Instance_GUI_pattern;
import usi.gui.functionality.instance.Instance_window;
import usi.gui.semantic.FunctionalitySemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class GUI_Pattern {

	private Map<String, Pattern_window> windows;
	private Map<String, Pattern_window> aw_window_mapping;
	private Multimap<String, String> staticEdgesFrom;
	private Multimap<String, String> staticEdgesTo;
	private Multimap<String, String> dynamicEdgesFrom;
	private Multimap<String, String> dynamicEdgesTo;
	private FunctionalitySemantics semantics;
	private FunctionalitySemantics semantics_without_unvalid;
	private final String name;

	public GUI_Pattern(final String name) {

		this.windows = new LinkedHashMap<>();
		this.staticEdgesFrom = LinkedHashMultimap.create();
		this.staticEdgesTo = LinkedHashMultimap.create();
		this.dynamicEdgesFrom = LinkedHashMultimap.create();
		this.dynamicEdgesTo = LinkedHashMultimap.create();
		this.aw_window_mapping = new HashMap<>();
		this.name = name;
		// this.action_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
	}

	public String getName() {

		return this.name;
	}

	public boolean isStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in isStaticEdge");
		}
		if (this.staticEdgesFrom.containsEntry(aw, w) && this.staticEdgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Pattern_window> getStaticForwardLinks(final String aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in getStaticForwardLinks");
		}

		final Collection<String> out = this.staticEdgesFrom.get(aw);
		if (out != null) {
			final List<Pattern_window> to_return = new ArrayList<>();
			for (final String s : out) {
				to_return.add(this.windows.get(s));
			}
			return to_return;

		} else {
			return new ArrayList<Pattern_window>();
		}
	}

	public List<Pattern_action_widget> getStaticBackwardLinks(final String w) throws Exception {

		if (w == null || !this.windows.containsKey(w)) {
			throw new Exception("GUI_Pattern: wrong input in getStaticBackwardLinks");
		}

		final Collection<String> out = this.staticEdgesTo.get(w);
		if (out != null) {
			final List<Pattern_action_widget> to_return = new ArrayList<>();
			loop: for (final String id : out) {
				final List<Pattern_action_widget> aws = this.aw_window_mapping.get(id)
						.getActionWidgets();
				for (final Pattern_action_widget aw : aws) {
					if (aw.getId().equals(id)) {
						to_return.add(aw);
						continue loop;
					}
				}
				throw new Exception("GUI - getStaticBackwardLinks: edge not found.");
			}
			return to_return;
		} else {
			return new ArrayList<Pattern_action_widget>();
		}
	}

	public void addStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in addStaticEdge");
		}
		this.staticEdgesFrom.put(aw, w);
		this.staticEdgesTo.put(w, aw);
	}

	public void removeStaticEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI_Pattern: wrong input in removeStaticEdge");
		}
		this.staticEdgesFrom.remove(aw, w);
		this.staticEdgesTo.remove(w, aw);
	}

	public boolean isDyanamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in isDyanamicEdge");
		}
		if (this.dynamicEdgesFrom.containsEntry(aw, w) && this.dynamicEdgesTo.containsEntry(w, aw)) {
			return true;
		}
		return false;
	}

	public List<Pattern_window> getDynamicForwardLinks(final String aw) throws Exception {

		if (aw == null || !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in getDynamicForwardLinks");
		}

		final Collection<String> out = this.dynamicEdgesFrom.get(aw);
		if (out != null) {
			final List<Pattern_window> to_return = new ArrayList<>();
			for (final String s : out) {
				to_return.add(this.windows.get(s));
			}
			return to_return;

		} else {
			return new ArrayList<Pattern_window>();
		}
	}

	public List<Pattern_action_widget> getDynamicBackwardLinks(final String w) throws Exception {

		if (w == null || !this.windows.containsKey(w)) {
			throw new Exception("GUI_Pattern: wrong input in getDynamicBackwardLinks");
		}

		final Collection<String> out = this.dynamicEdgesTo.get(w);
		if (out != null) {
			final List<Pattern_action_widget> to_return = new ArrayList<>();
			loop: for (final String id : out) {
				final List<Pattern_action_widget> aws = this.aw_window_mapping.get(id)
						.getActionWidgets();
				for (final Pattern_action_widget aw : aws) {
					if (aw.getId().equals(id)) {
						to_return.add(aw);
						continue loop;
					}
				}
				throw new Exception("GUI - getDynamicBackwardLinks: edge not found.");
			}
			return to_return;
		} else {
			return new ArrayList<Pattern_action_widget>();
		}
	}

	public void addDynamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null || !this.windows.containsKey(w)
				|| !this.aw_window_mapping.containsKey(aw)) {
			throw new Exception("GUI_Pattern: wrong input in addDynamicEdge");
		}
		this.dynamicEdgesFrom.put(aw, w);
		this.dynamicEdgesTo.put(w, aw);
	}

	public void removeDynamicEdge(final String aw, final String w) throws Exception {

		if (aw == null || w == null) {
			throw new Exception("GUI_Pattern: wrong input in removeDynamicEdge");
		}
		this.dynamicEdgesFrom.remove(aw, w);
		this.dynamicEdgesTo.remove(w, aw);
	}

	public void setWindows(final List<Pattern_window> ws) throws Exception {

		if (ws == null) {
			throw new Exception("GUI_Pattern: wrong input in setWindows");
		}
		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		this.aw_window_mapping = new HashMap<>();
		this.windows = new HashMap<>();

		this.staticEdgesFrom = HashMultimap.create();
		this.staticEdgesTo = HashMultimap.create();
		this.dynamicEdgesFrom = HashMultimap.create();
		this.dynamicEdgesTo = HashMultimap.create();
		this.aw_window_mapping = new HashMap<>();

		for (final Pattern_window w : ws) {
			this.addWindow(w);
		}
	}

	public void addWindow(final Pattern_window n) throws Exception {

		if (n == null || this.windows.containsKey(n.getId())) {
			throw new Exception("GUI_Pattern: wrong input in addWindow");
		}
		this.windows.put(n.getId(), n);

		// this.action_widgets.addAll(n.getActionWidgets());
		// this.input_widgets.addAll(n.getInputWidgets());
		// this.selectable_widgets.addAll(n.getSelectableWidgets());
		for (final Pattern_action_widget paw : n.getActionWidgets()) {
			this.aw_window_mapping.put(paw.getId(), n);
		}
	}

	public void removeWindow(final Pattern_window n) throws Exception {

		if (n == null || !this.windows.containsKey(n.getId())) {
			throw new Exception("GUI_Pattern: wrong input in removeWindow");
		}

		// the static edges associated with this window are removed
		List<Pattern_action_widget> to_remove = new ArrayList<>(this.getStaticBackwardLinks(n
				.getId()));
		for (final Pattern_action_widget aw : to_remove) {
			this.removeStaticEdge(aw.getId(), n.getId());
		}

		List<String> from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final String aw : from_links) {
			for (final Pattern_window w : this.getStaticForwardLinks(aw)) {
				this.removeStaticEdge(aw, w.getId());
			}
		}

		// the dynamic edges associated with this window are removed
		to_remove = new ArrayList<>(this.getDynamicBackwardLinks(n.getId()));
		for (final Pattern_action_widget aw : to_remove) {
			this.removeStaticEdge(aw.getId(), n.getId());
		}

		from_links = this.aw_window_mapping.entrySet().parallelStream()
				.filter(e -> e.getValue() == n).map(ee -> ee.getKey()).collect(Collectors.toList());

		for (final String aw : from_links) {
			for (final Pattern_window w : this.getDynamicForwardLinks(aw)) {
				this.removeStaticEdge(aw, w.getId());
			}
		}

		this.windows.remove(n.getId());
		this.setWindows(new ArrayList<>(this.windows.values()));
	}

	public List<Pattern_window> getWindows() {

		return new ArrayList<>(this.windows.values());
	}

	public List<Pattern_action_widget> getAction_widgets() {

		final List<Pattern_action_widget> out = new ArrayList<>();

		for (final Pattern_window pw : this.windows.values()) {
			out.addAll(pw.getActionWidgets());
		}

		return out;
	}

	public List<Pattern_input_widget> getInput_widgets() {

		final List<Pattern_input_widget> out = new ArrayList<>();

		for (final Pattern_window pw : this.windows.values()) {
			out.addAll(pw.getInputWidgets());
		}

		return out;
	}

	public List<Pattern_selectable_widget> getSelectable_widgets() {

		final List<Pattern_selectable_widget> out = new ArrayList<>();

		for (final Pattern_window pw : this.windows.values()) {
			out.addAll(pw.getSelectableWidgets());
		}

		return out;
	}

	public Pattern_window getActionWidget_Window(final String aw) {

		return this.aw_window_mapping.get(aw);
	}

	public FunctionalitySemantics getSemantics_without_unvalid() {

		return this.semantics_without_unvalid;
	}

	public void setSemantics_without_unvalid(final FunctionalitySemantics in) {

		this.semantics_without_unvalid = in;
	}

	public FunctionalitySemantics getSemantics() {

		return this.semantics;
	}

	public void setSemantics(final FunctionalitySemantics in) {

		this.semantics = in;
	}

	public void loadSemantics(final String filename) throws Exception {

		this.semantics = this.loadAlloyModel("GUI_general.als", filename);
	}

	private FunctionalitySemantics loadAlloyModel(final String general, final String filename)
			throws Exception {

		final File gui_model = new File(PathsManager.getAlloyModelsFolder() + general);
		final File alloy_metamodel = new File(PathsManager.getAlloyModelsFolder() + filename);
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
			final FunctionalitySemantics semantics = FunctionalitySemantics.instantiate(AlloyUtil
					.loadAlloyModelFromString(model));
			return semantics;
		}
		return null;
	}

	public void loadSemantics_without(final String filename) throws Exception {

		this.semantics_without_unvalid = this.loadAlloyModel("GUI_general_without_unvalid.als",
				filename);
	}

	public boolean containsWindow(final String id) {

		return this.windows.containsKey(id);
	}

	public Pattern_window getWindow(final String id) {

		return this.windows.get(id);
	}

	public boolean isInstance(final Instance_GUI_pattern in) throws Exception {

		if (in.getGuipattern() != this) {
			return false;
		}

		final GUI match_gui = in.getGui();

		for (final Pattern_window pw : this.getWindows()) {
			final List<Window> matches = in.getPatternWindowMatches(pw.getId());
			if (matches.size() < pw.getCardinality().getMin()
					|| matches.size() > pw.getCardinality().getMax()) {

				return false;
			}
		}

		for (final Instance_window iww : in.getWindows()) {
			for (final Pattern_action_widget paw : iww.getPattern().getActionWidgets()) {
				final List<Action_widget> aws = iww.getAWS_for_PAW(paw.getId());
				if (aws.size() < paw.getCardinality().getMin()
						|| aws.size() > paw.getCardinality().getMax()) {

					return false;
				}
			}
			for (final Pattern_selectable_widget psw : iww.getPattern().getSelectableWidgets()) {
				final List<Selectable_widget> sws = iww.getSWS_for_PSW(psw.getId());
				if (sws.size() < psw.getCardinality().getMin()
						|| sws.size() > psw.getCardinality().getMax()) {

					return false;
				}
			}
			for (final Pattern_input_widget piw : iww.getPattern().getInputWidgets()) {
				final List<Input_widget> iws = iww.getIWS_for_PIW(piw.getId());
				if (iws.size() < piw.getCardinality().getMin()
						|| iws.size() > piw.getCardinality().getMax()) {

					return false;
				}
			}
		}

		for (final Pattern_window pw : this.getWindows()) {
			final List<Window> matches = in.getPatternWindowMatches(pw.getId());
			for (final Window match : matches) {
				// static
				boolean found_aw_match = false;
				loop: for (final Pattern_action_widget paw : this
						.getStaticBackwardLinks(pw.getId())) {
					final Pattern_window source_pw = this.getActionWidget_Window(paw.getId());
					final List<Window> pw_matches = in.getPatternWindowMatches(source_pw.getId());

					if (pw_matches.size() == 0 && source_pw.getCardinality().getMin() == 0) {
						continue;
					}
					for (final Window w : pw_matches) {
						List<Action_widget> aw_matches = in.getAWS_for_PAW(paw.getId());
						if (aw_matches == null) {
							continue;
						}
						aw_matches = aw_matches.stream().filter(e -> w.containsWidget(e.getId()))
								.collect(Collectors.toList());
						if (aw_matches.size() == 0) {
							continue;
						}
						found_aw_match = true;
						for (final Action_widget aw : aw_matches) {
							if (match_gui.isStaticEdge(aw.getId(), match.getId())) {
								found_aw_match = false;
								break loop;
							}
						}
					}

				}
				if (found_aw_match) {
					System.out.println(pw.getId() + " " + match.getId());
					return false;
				}
				// dynamic
				found_aw_match = false;
				loop: for (final Pattern_action_widget paw : this.getDynamicBackwardLinks(pw
						.getId())) {

					final Pattern_window source_pw = this.getActionWidget_Window(paw.getId());
					final List<Window> pw_matches = in.getPatternWindowMatches(source_pw.getId());

					if (pw_matches.size() == 0 && source_pw.getCardinality().getMin() == 0) {
						continue;
					}
					for (final Window w : pw_matches) {

						List<Action_widget> aw_matches = in.getAWS_for_PAW(paw.getId());
						if (aw_matches == null) {
							continue;
						}
						aw_matches = aw_matches.stream().filter(e -> w.containsWidget(e.getId()))
								.collect(Collectors.toList());

						if (aw_matches.size() == 0) {
							continue;
						}
						found_aw_match = true;
						for (final Action_widget aw : aw_matches) {

							if (match_gui.isDynamicEdge(aw.getId(), match.getId())) {
								found_aw_match = false;
								break loop;
							}
						}
					}
				}
				if (found_aw_match) {

					return false;
				}

				for (final Pattern_action_widget paw : pw.getActionWidgets()) {
					// static
					List<Action_widget> aw_matches = in.getAWS_for_PAW(paw.getId()).stream()
							.filter(e -> match.containsWidget(e.getId()))
							.collect(Collectors.toList());

					loop: for (final Action_widget aw : aw_matches) {
						boolean target_found = false;
						for (final Pattern_window target_pw : this.getStaticForwardLinks(paw
								.getId())) {
							final List<Window> targets = in.getPatternWindowMatches(target_pw
									.getId());

							for (final Window ww : targets) {
								target_found = true;
								if (match_gui.isStaticEdge(aw.getId(), ww.getId())) {
									continue loop;
								}
							}
						}
						if (target_found) {

							return false;
						}
					}
					// dynamic
					aw_matches = in.getAWS_for_PAW(paw.getId()).stream()
							.filter(e -> match.containsWidget(e.getId()))
							.collect(Collectors.toList());

					loop: for (final Action_widget aw : aw_matches) {
						boolean target_found = false;
						for (final Pattern_window target_pw : this.getDynamicForwardLinks(paw
								.getId())) {
							final List<Window> targets = in.getPatternWindowMatches(target_pw
									.getId());

							for (final Window ww : targets) {
								target_found = true;
								if (match_gui.isDynamicEdge(aw.getId(), ww.getId())) {
									continue loop;
								}
							}
						}
						if (target_found) {

							return false;
						}
					}
				}

			}

		}

		return true;
	}
}
