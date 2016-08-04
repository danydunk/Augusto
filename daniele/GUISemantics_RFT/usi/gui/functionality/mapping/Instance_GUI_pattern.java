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
import usi.gui.semantic.SpecificSemantics;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

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
	// from window to pattern window
	private final Map<String, String> windows_mapping;
	// private final Map<Action_widget, Pattern_action_widget>
	// action_widgets_mapping;
	// private final Map<Input_widget, Pattern_input_widget>
	// input_widgets_mapping;
	// private final Map<Selectable_widget, Pattern_selectable_widget>
	// selectable_widgets_mapping;
	// private final Map<Pattern_window, List<Window>> pattern_windows_mapping;
	// private final Map<Pattern_action_widget, List<Action_widget>>
	// pattern_action_widgets_mapping;
	// private final Map<Pattern_input_widget, List<Input_widget>>
	// pattern_input_widgets_mapping;
	// private final Map<Pattern_selectable_widget, List<Selectable_widget>>
	// pattern_selectable_widgets_mapping;

	protected SpecificSemantics semantics;

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern,
			final List<Instance_window> windows) {

		this.gui = gui;
		this.guipattern = guipattern;
		this.windows = windows;

		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		//
		// this.action_widgets_mapping = new HashMap<>();
		// this.input_widgets_mapping = new HashMap<>();
		// this.selectable_widgets_mapping = new HashMap<>();
		this.windows_mapping = new HashMap<>();
		for (final Instance_window iw : this.windows) {
			this.windows_mapping.put(iw.getInstance().getId(), iw.getPattern().getId());

			// for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
			// this.action_widgets.addAll(iw.getAw_map().get(paw));
			// for (final Action_widget aw : iw.getAw_map().get(paw)) {
			// this.action_widgets_mapping.put(aw, paw);
			// }
			// }
			// for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
			// this.input_widgets.addAll(iw.getIw_map().get(piw));
			// for (final Input_widget iiw : iw.getIw_map().get(piw)) {
			// this.input_widgets_mapping.put(iiw, piw);
			// }
			//
			// }
			// for (final Pattern_selectable_widget psw :
			// iw.getSw_map().keySet()) {
			// this.selectable_widgets.addAll(iw.getSw_map().get(psw));
			// for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
			// this.selectable_widgets_mapping.put(sw, psw);
			// }
			// }
		}
	}

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern) {

		this.gui = gui;
		this.guipattern = guipattern;

		this.windows = new ArrayList<>();
		// this.action_widgets = new ArrayList<>();
		// this.input_widgets = new ArrayList<>();
		// this.selectable_widgets = new ArrayList<>();
		//
		// this.action_widgets_mapping = new HashMap<>();
		// this.input_widgets_mapping = new HashMap<>();
		// this.selectable_widgets_mapping = new HashMap<>();
		this.windows_mapping = new HashMap<>();
	}

	public void addWindow(final Instance_window iw) {

		if (this.windows.contains(iw)) {
			return;
		}
		this.windows_mapping.put(iw.getInstance().getId(), iw.getPattern().getId());
		this.windows.add(iw);
		// for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
		// this.action_widgets.addAll(iw.getAw_map().get(paw));
		// for (final Action_widget aw : iw.getAw_map().get(paw)) {
		// this.action_widgets_mapping.put(aw, paw);
		// }
		// }
		// for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
		// this.input_widgets.addAll(iw.getIw_map().get(piw));
		// for (final Input_widget iiw : iw.getIw_map().get(piw)) {
		// this.input_widgets_mapping.put(iiw, piw);
		// }
		//
		// }
		// for (final Pattern_selectable_widget psw : iw.getSw_map().keySet()) {
		// this.selectable_widgets.addAll(iw.getSw_map().get(psw));
		// for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
		// this.selectable_widgets_mapping.put(sw, psw);
		// }
		// }
	}

	public void removeWindow(final Instance_window iw) {

		if (!this.windows.contains(iw)) {
			return;
		}
		this.windows.remove(iw);
		this.windows_mapping.remove(iw.getInstance());
		// for (final Pattern_action_widget paw : iw.getAw_map().keySet()) {
		// this.action_widgets.removeAll(iw.getAw_map().get(paw));
		// for (final Action_widget aw : iw.getAw_map().get(paw)) {
		// this.action_widgets_mapping.remove(aw, paw);
		// }
		// }
		// for (final Pattern_input_widget piw : iw.getIw_map().keySet()) {
		// this.input_widgets.removeAll(iw.getIw_map().get(piw));
		// for (final Input_widget iiw : iw.getIw_map().get(piw)) {
		// this.input_widgets_mapping.remove(iiw, piw);
		// }
		//
		// }
		// for (final Pattern_selectable_widget psw : iw.getSw_map().keySet()) {
		// this.selectable_widgets.removeAll(iw.getSw_map().get(psw));
		// for (final Selectable_widget sw : iw.getSw_map().get(psw)) {
		// this.selectable_widgets_mapping.remove(sw, psw);
		// }
		// }
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

	public Pattern_window getPW_for_W(final String w) {

		return this.guipattern.getWindow(this.windows_mapping.get(w));
	}

	public List<Window> getWS_for_PW(final String pw) {

		final List<Window> out = new ArrayList<>();

		for (final String w : this.windows_mapping.keySet()) {
			if (this.windows_mapping.get(w).equals(pw)) {
				out.add(this.gui.getWindow(w));
			}
		}
		return out;
	}

	public Pattern_action_widget getPAW_for_AW(final String aw) {

		for (final Instance_window iw : this.windows) {
			final Pattern_action_widget out = iw.getPAW_for_AW(aw);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public Pattern_input_widget getPIW_for_IW(final String iw) {

		for (final Instance_window iww : this.windows) {
			final Pattern_input_widget out = iww.getPIW_for_IW(iw);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public Pattern_selectable_widget getPSW_for_SW(final String sw) {

		for (final Instance_window iww : this.windows) {
			final Pattern_selectable_widget out = iww.getPSW_for_SW(sw);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public List<Action_widget> getAWS_for_PAW(final String paw) throws Exception {

		for (final Instance_window iw : this.windows) {
			final List<Action_widget> out = iw.getAWS_for_PAW(paw);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public List<Input_widget> getIWS_for_PIW(final String piw) throws Exception {

		for (final Instance_window iw : this.windows) {
			final List<Input_widget> out = iw.getIWS_for_PIW(piw);
			if (out != null) {
				return out;
			}
		}
		return null;
	}

	public List<Selectable_widget> getSWS_for_PSW(final String psw) throws Exception {

		for (final Instance_window iw : this.windows) {
			final List<Selectable_widget> out = iw.getSWS_for_PSW(psw);
			if (out != null) {
				return out;
			}
		}
		return null;
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
				for (final Action_widget aw : this.gui.getDynamicBackwardLinks(w.getId())) {
					out.getGui().addDynamicEdge(aw.getId(), w.getId());
				}
			}
		} catch (final Exception e) {
			return null;
		}
		out.setSpecificSemantics(this.semantics);
		return out;
	}

	public SpecificSemantics getSemantics() {

		return this.semantics;
	}

	public void generateSpecificSemantics() throws Exception {

		this.semantics = SpecificSemantics.generate(this);
	}

	public void setSpecificSemantics(final SpecificSemantics in) {

		this.semantics = in;
	}

	public List<Window> getPatternWindowMatches(final String pw) {

		final List<Window> out = new ArrayList<>();
		for (final Instance_window iw : this.windows) {
			if (iw.getPattern().getId().equals(pw)) {
				out.add(iw.getInstance());
			}
		}
		return out;
	}
}
