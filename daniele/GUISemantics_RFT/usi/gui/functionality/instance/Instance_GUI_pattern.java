package usi.gui.functionality.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.configuration.ConfigurationManager;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.AlloyUtil;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;
import usi.pattern.structure.GUI_Pattern;
import usi.pattern.structure.Pattern_action_widget;
import usi.pattern.structure.Pattern_input_widget;
import usi.pattern.structure.Pattern_selectable_widget;
import usi.pattern.structure.Pattern_window;
import usi.testcase.AlloyTestCaseGenerator;
import usi.testcase.GUITestCaseResult;
import usi.testcase.structure.Click;
import usi.testcase.structure.Fill;
import usi.testcase.structure.GUIAction;
import usi.testcase.structure.GUITestCase;
import usi.testcase.structure.Select;
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
	private final Map<String, String> windows_mapping;
	protected SpecificSemantics semantics;

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern,
			final List<Instance_window> windows) {

		this.gui = gui;
		this.guipattern = guipattern;
		this.windows = windows;

		this.windows_mapping = new HashMap<>();
		for (final Instance_window iw : this.windows) {
			this.windows_mapping.put(iw.getInstance().getId(), iw.getPattern().getId());
		}
	}

	public Instance_GUI_pattern(final GUI gui, final GUI_Pattern guipattern) {

		this.gui = gui;
		this.guipattern = guipattern;

		this.windows = new ArrayList<>();
		this.windows_mapping = new HashMap<>();
	}

	public void addWindow(final Instance_window iw) {

		if (this.windows.contains(iw)) {
			return;
		}
		this.windows_mapping.put(iw.getInstance().getId(), iw.getPattern().getId());
		this.windows.add(iw);
	}

	public void removeWindow(final Instance_window iw) {

		if (!this.windows.contains(iw)) {
			return;
		}
		this.windows.remove(iw);
		this.windows_mapping.remove(iw.getInstance());
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
			scopes += "," + (vs + ConfigurationManager.getAlloyRunScope()) + " Value";
		}
		run = run + scopes;
		sem.addRun_command(run);

		final Module mod = AlloyUtil.compileAlloyModel(sem.toString());

		return AlloyUtil.runCommand(mod, mod.getAllCommands().get(0)).satisfiable();
	}

	/**
	 * Function that updates a testcase results by removing all the actions not
	 * executed in the TC for which the semantics was unknonwn
	 *
	 * @param res
	 * @return
	 * @throws Exception
	 */
	public GUITestCaseResult updateTCResult(final GUITestCaseResult res) throws Exception {

		if (res.getTc().getActions().size() == res.getActions_executed().size()) {
			return res;
		}
		final List<GUIAction> actions = new ArrayList<>();
		final List<GUIAction> actions_executed = new ArrayList<>();
		final List<GUIAction> actions_actually_executed = res.getActions_actually_executed();
		final List<Window> results = new ArrayList<>();
		Window last = null;
		boolean tc_changed = false;
		int y = 0;
		for (int x = 0; x < res.getTc().getActions().size(); x++) {
			final GUIAction act_to_execute = res.getTc().getActions().get(x);
			if (y >= res.getActions_executed().size()
					|| !act_to_execute.isSame(res.getActions_executed().get(y))) {

				if (last != null && !act_to_execute.getWindow().getId().equals(last.getId())) {
					continue;
				}

				if (act_to_execute instanceof Click) {
					if (this.getSemantics().getClickSemantics().getContent().trim().length() == 0) {
						// semantics is empty, therefore this action can be
						// skipped
						tc_changed = true;
						continue;
					}
				}
				if (act_to_execute instanceof Fill) {
					if (this.getSemantics().getFillSemantics().getContent().trim().length() == 0) {
						// semantics is empty, therefore this action can be
						// skipped
						tc_changed = true;
						continue;
					}

				}
				if (act_to_execute instanceof Select) {
					if (this.getSemantics().getSelectSemantics().getContent().trim().length() == 0) {
						// semantics is empty, therefore this action can be
						// skipped
						tc_changed = true;
						continue;
					}
				}

				actions.add(act_to_execute);
				actions_executed.add(act_to_execute);
				if (y > 0) {
					last = res.getResults().get(y - 1);
				}
				results.add(last);

			} else {
				actions.add(act_to_execute);
				actions_executed.add(act_to_execute);
				results.add(res.getResults().get(y));
				y++;
			}
		}

		GUITestCaseResult new_res = new GUITestCaseResult(new GUITestCase(res.getTc()
				.getAlloySolution(), actions, res.getTc().getRunCommand()), actions_executed,
				results, actions_actually_executed);

		if (!tc_changed) {
			return res;
		}

		final Alloy_Model sem = AlloyUtil.getTCaseModel(this.getSemantics(), new_res.getTc()
				.getActions(), null);
		final Instance_GUI_pattern clone = this.clone();
		clone.setSpecificSemantics(SpecificSemantics.instantiate(sem));
		final AlloyTestCaseGenerator test_gen = new AlloyTestCaseGenerator(clone);
		final List<GUITestCase> tests = test_gen.generateTestCases();
		assert (tests.size() < 2);
		if (tests.size() == 1) {
			new_res = new GUITestCaseResult(tests.get(0), actions_executed, results,
					actions_actually_executed);
			return new_res;
		} else {
			return null;
		}
	}
}
