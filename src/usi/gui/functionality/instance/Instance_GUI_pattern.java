package src.usi.gui.functionality.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.usi.configuration.ConfigurationManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.pattern.structure.Pattern_window;
import src.usi.semantic.SpecificSemantics;
import src.usi.semantic.alloy.AlloyUtil;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;

/**
 * This class represents one instance of a pattern inside a GUI. It reference to
 * the GUI, the pattern, and it maps all relations between the pattern's
 * components and the elements from the GUI (windows, inputs, buttons, etc )
 *
 * @author Daniele Zuddas
 *
 */
public class Instance_GUI_pattern {

	// gui contains only the windows that match the pattern
	private final GUI gui;
	private final GUI_Pattern guipattern;

	private final List<Instance_window> windows;
	// from window to pattern window
	private final Map<String, List<String>> windows_mapping;
	// from pattern window to window
	private final Map<String, List<String>> pw_windows_mapping;
	protected SpecificSemantics semantics;

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern,
			final List<Instance_window> windows) {

		this.gui = gui;
		this.guipattern = guipattern;
		this.windows = windows;

		this.windows_mapping = new HashMap<>();
		for (final Instance_window iw : this.windows) {
			if (!this.windows_mapping.containsKey(iw.getInstance().getId())) {
				this.windows_mapping.put(iw.getInstance().getId(), new ArrayList<>());

			}
			this.windows_mapping.get(iw.getInstance().getId()).add(iw.getPattern().getId());
		}
		this.pw_windows_mapping = new HashMap<>();
		for (final Instance_window iw : this.windows) {
			if (!this.pw_windows_mapping.containsKey(iw.getPattern().getId())) {
				this.pw_windows_mapping.put(iw.getPattern().getId(), new ArrayList<>());

			}
			this.pw_windows_mapping.get(iw.getPattern().getId()).add(iw.getInstance().getId());
		}
	}

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern) {

		this.gui = gui;
		this.guipattern = guipattern;

		this.windows = new ArrayList<>();
		this.windows_mapping = new HashMap<>();
		this.pw_windows_mapping = new HashMap<>();
	}

	public void addWindow(final Instance_window iw) {

		if (this.windows.contains(iw)) {
			return;
		}

		if (!this.windows_mapping.containsKey(iw.getInstance().getId())) {
			this.windows_mapping.put(iw.getInstance().getId(), new ArrayList<>());
		}
		this.windows_mapping.get(iw.getInstance().getId()).add(iw.getPattern().getId());

		if (!this.pw_windows_mapping.containsKey(iw.getPattern().getId())) {
			this.pw_windows_mapping.put(iw.getPattern().getId(), new ArrayList<>());

		}
		this.pw_windows_mapping.get(iw.getPattern().getId()).add(iw.getInstance().getId());

		this.windows.add(iw);
	}

	public void removeWindow(final Instance_window iw) {

		if (!this.windows.contains(iw)) {
			return;
		}

		this.windows_mapping.get(iw.getInstance().getId()).remove(iw.getPattern().getId());
		this.pw_windows_mapping.get(iw.getPattern().getId()).remove(iw.getInstance().getId());

		this.windows.remove(iw);
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

	public List<Pattern_window> getPW_for_W(final String w) {

		final List<Pattern_window> out = new ArrayList<>();
		if (!this.windows_mapping.containsKey(w)) {
			return out;
		}
		for (final String x : this.windows_mapping.get(w)) {
			out.add(this.guipattern.getWindow(x));
		}
		return out;
	}

	public List<Window> getWS_for_PW(final String pw) {

		final List<Window> out = new ArrayList<>();
		for (final String x : this.pw_windows_mapping.get(pw)) {
			out.add(this.gui.getWindow(x));
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
				if (!out.getGui().containsWindow(iw.getInstance().getId())) {
					out.getGui().addWindow(iw.getInstance());
				}
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

	public boolean isSemanticsValid() throws Exception {

		if (this.semantics == null) {
			this.generateSpecificSemantics();
		}
		final SpecificSemantics sem = SpecificSemantics.instantiate(this.semantics);
		String run = "run System for " + ConfigurationManager.getAlloyRunScope();
		String scopes = " but ";
		final int ws = AlloyUtil.getWinScope(this.semantics);
		scopes += ws + " Window";
		final int aws = AlloyUtil.getAWScope(this.semantics);
		final int vs = AlloyUtil.getValueScope(this.semantics);
		final int iws = AlloyUtil.getIWScope(this.semantics);
		final int sws = AlloyUtil.getSWScope(this.semantics);
		if (aws > -1) {
			scopes += "," + aws + " Action_widget";
		}
		if (iws > -1) {
			scopes += "," + iws + " Input_widget";
		}
		if (sws > -1) {
			scopes += "," + sws + " Selectable_widget";
		}
		if (vs > -1) {
			scopes += "," + (vs + (ConfigurationManager.getAlloyRunScope() * 2 / 3)) + " Value";
		}
		run = run + scopes;
		sem.addRun_command(run);
		// System.out.println(sem);
		final Module mod = AlloyUtil.compileAlloyModel(sem.toString());

		return AlloyUtil.runCommand(mod, mod.getAllCommands().get(0)).satisfiable();
	}
}
