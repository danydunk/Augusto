package usi.gui.functionality.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.pattern.Pattern_input_widget;
import usi.gui.pattern.Pattern_selectable_widget;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;
import usi.guisemantic.SpecificSemantics;

/**
 * This class represents one instance of a pattern inside a GUI. It reference to
 * the GUI, the pattern, and it maps all relations between the pattern's
 * components and the elements from the GUI (windows, inputs, buttons, etc )
 *
 * @author Matias Martinez - modified by Daniele Zuddas
 *
 */
public class Instance_GUI_pattern {

	// gui contains only the windows that match the pattern
	private final GUI gui;
	private final GUI_Pattern guipattern;
	private final List<Instance_window> windows;
	// protected List<Instance_container> containers;
	private final List<Action_widget> action_widgets;
	private final List<Input_widget> input_widgets;
	private final List<Selectable_widget> selectable_widgets;
	private final Map<Window, Pattern_window> windows_mapping;
	// protected Map<Container, Pattern_container> containers_mapping;
	private final Map<Action_widget, Pattern_action_widget> action_widgets_mapping;
	private final Map<Input_widget, Pattern_input_widget> input_widgets_mapping;
	private final Map<Selectable_widget, Pattern_selectable_widget> selectable_widgets_mapping;
	protected SpecificSemantics semantics;

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern,
			final List<Instance_window> windows) {

		this.gui = gui;
		this.guipattern = guipattern;
		this.windows = windows;

		// containers = new ArrayList<Instance_container>();
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();

		// containers_mapping = new HashMap<Container, Pattern_container>();
		this.action_widgets_mapping = new HashMap<>();
		this.input_widgets_mapping = new HashMap<>();
		this.selectable_widgets_mapping = new HashMap<>();
		this.windows_mapping = new HashMap<>();
		for (final Instance_window iw : this.windows) {
			this.windows_mapping.put(iw.getInstance(), iw.getPattern());
			// for(List<Instance_container> lic : iw.getC_map().values()) {
			// containers.addAll(lic);
			// for(Instance_container ic : lic) {
			// containers_mapping.put(ic.instance, ic.pattern);
			for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
				this.action_widgets.addAll(iw.getAw_map().get(paw));
				for (final Action_widget aw : iw.getAw_map().get(paw)) {
					this.action_widgets_mapping.put(aw, paw);
				}
			}
			for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
				this.input_widgets.addAll(iw.getIw_map().get(piw));
				for (final Input_widget iiw : iw.getIw_map().get(piw)) {
					this.input_widgets_mapping.put(iiw, piw);
				}

			}
			for (final Pattern_selectable_widget psw : iw.getSw_map().keySet()) {
				this.selectable_widgets.addAll(iw.getSw_map().get(psw));
				for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
					this.selectable_widgets_mapping.put(sw, psw);
				}
			}
		}
	}

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern) {

		this.gui = gui;
		this.guipattern = guipattern;

		this.windows = new ArrayList<>();
		// containers = new ArrayList<Instance_container>();
		this.action_widgets = new ArrayList<>();
		this.input_widgets = new ArrayList<>();
		this.selectable_widgets = new ArrayList<>();

		// containers_mapping = new HashMap<Container, Pattern_container>();
		this.action_widgets_mapping = new HashMap<>();
		this.input_widgets_mapping = new HashMap<>();
		this.selectable_widgets_mapping = new HashMap<>();
		this.windows_mapping = new HashMap<>();
	}

	public void addWindow(final Instance_window iw) {

		if (this.windows.contains(iw)) {
			return;
		}
		this.windows_mapping.put(iw.getInstance(), iw.getPattern());
		this.windows.add(iw);
		for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
			this.action_widgets.addAll(iw.getAw_map().get(paw));
			for (final Action_widget aw : iw.getAw_map().get(paw)) {
				this.action_widgets_mapping.put(aw, paw);
			}
		}
		for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
			this.input_widgets.addAll(iw.getIw_map().get(piw));
			for (final Input_widget iiw : iw.getIw_map().get(piw)) {
				this.input_widgets_mapping.put(iiw, piw);
			}

		}
		for (final Pattern_selectable_widget psw : iw.getSw_map().keySet()) {
			this.selectable_widgets.addAll(iw.getSw_map().get(psw));
			for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
				this.selectable_widgets_mapping.put(sw, psw);
			}
		}
	}

	public void removeWindow(final Instance_window iw) {

		if (!this.windows.contains(iw)) {
			return;
		}
		this.windows.remove(iw);
		this.windows_mapping.remove(iw.getInstance());
		for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
			this.action_widgets.removeAll(iw.getAw_map().get(paw));
			for (final Action_widget aw : iw.getAw_map().get(paw)) {
				this.action_widgets_mapping.remove(aw, paw);
			}
		}
		for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
			this.input_widgets.removeAll(iw.getIw_map().get(piw));
			for (final Input_widget iiw : iw.getIw_map().get(piw)) {
				this.input_widgets_mapping.remove(iiw, piw);
			}

		}
		for (final Pattern_selectable_widget psw : iw.getSw_map().keySet()) {
			this.selectable_widgets.removeAll(iw.getSw_map().get(psw));
			for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
				this.selectable_widgets_mapping.remove(sw, psw);
			}
		}
	}

	public GUI getGui() {

		return this.gui;
	}

	public GUI_Pattern getGuipattern() {

		return this.guipattern;
	}

	public List<Instance_window> getWindows() {

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

	public Map<Window, Pattern_window> getWindows_mapping() {

		return new HashMap<>(this.windows_mapping);
	}

	// public Map<Container, Pattern_container> getContainers_mapping() {
	// return containers_mapping;
	// }

	public Map<Action_widget, Pattern_action_widget> getAction_widgets_mapping() {

		return new HashMap<>(this.action_widgets_mapping);
	}

	public Map<Input_widget, Pattern_input_widget> getInput_widgets_mapping() {

		return new HashMap<>(this.input_widgets_mapping);
	}

	public Map<Selectable_widget, Pattern_selectable_widget> getSelectable_widgets_mapping() {

		return new HashMap<>(this.selectable_widgets_mapping);
	}

	@Override
	public Instance_GUI_pattern clone() {

		final Instance_GUI_pattern out = new Instance_GUI_pattern(new GUI(), this.guipattern);
		for (final Instance_window iw : this.windows) {
			out.addWindow(iw);
			try {
				out.getGui().addWindow(iw.getInstance());
			} catch (final Exception e) {
				// if window already added
			}
		}

		try {
			for (final Window w : this.gui.getWindows()) {
				for (final Action_widget aw : this.gui.getStaticBackwardLinks(w.getId())) {
					out.getGui().addStaticEdge(aw.getId(), w.getId());
				}
			}
		} catch (final Exception e) {
			return null;
		}
		return out;
	}

	public SpecificSemantics getSemantics() {

		return this.semantics;
	}

	public void generateSpecificSemantics() throws Exception {

		this.semantics = SpecificSemantics.generate(this);
	}
}
