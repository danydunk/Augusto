package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.gui.GuiStateManager;
import usi.gui.pattern.Pattern_error_window;
import usi.gui.structure.GUI;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.ITestDataElement;
import com.rational.test.ft.vp.ITestDataElementList;
import com.rational.test.ft.vp.impl.TestDataList;

public class TestCaseRunner {

	private final long sleep;
	private final ActionManager amanager;
	// used for go actions
	private final GUI gui;

	public TestCaseRunner(final long sleep, final GUI gui) {

		this.sleep = sleep;
		this.amanager = new ActionManager(this.sleep);
		this.gui = gui;
	}

	public TestCaseRunner(final long sleep) {

		this(sleep, null);
	}

	public GUITestCaseResult runTestCase(final GUITestCase tc) throws Exception {

		final ApplicationHelper app = ApplicationHelper.getInstance();
		if (app.isRunning()) {
			app.restartApplication();
		} else {
			app.startApplication();
		}

		final GuiStateManager gmanager = GuiStateManager.getInstance();
		gmanager.readGUI();
		final Map<Pair, List<String>> select_support_initial = new HashMap<>();
		final Map<Pair, List<String>> select_support_added = new HashMap<>();
		final Map<Pair, List<Integer>> select_support_added_indexes = new HashMap<>();

		final List<GUIAction> actions = tc.getActions();
		// structures needed to construct the GUITestCaseResult
		final List<GUIAction> actions_executed = new ArrayList<>();
		final List<GUIAction> actions_actually_executed = new ArrayList<>();
		final List<Window> results = new ArrayList<>();
		List<GUIAction> go_actions = null;
		for (int cont = 0; cont < actions.size(); cont++) {

			GUIAction act = actions.get(cont);
			final Window curr = gmanager.getCurrentWindows().get(0);
			loop: for (final Selectable_widget sw : curr.getSelectableWidgets()) {
				final TestObject to = sw.getTo();
				final TestDataList list = (TestDataList) to.getTestData("list");
				final ITestDataElementList el_list = list.getElements();
				final List<String> curr_el = new ArrayList<>();
				for (int c = 0; c < el_list.getLength(); c++) {
					final ITestDataElement element = el_list.getElement(c);
					curr_el.add(element.getElement().toString());
				}

				final Pair new_p = new Pair(curr, sw);
				for (final Pair p : select_support_initial.keySet()) {
					if (p.isSame(new_p)) {
						for (final String el : curr_el) {
							if (!select_support_initial.get(p).contains(el)
									&& !select_support_added.get(p).contains(el)) {
								select_support_added.get(p).add(el);
								select_support_added_indexes.get(p).add(curr_el.indexOf(el));
							}
						}
						for (int c = 0; c < select_support_added.get(p).size(); c++) {
							final String el = select_support_added.get(p).get(c);
							if (!curr_el.contains(el)) {
								select_support_added.get(p).remove(c);
								select_support_added_indexes.get(p).remove(c);
								c--;
							}
						}
						continue loop;
					}
				}
				// if the sw is seen for the first time
				select_support_added.put(new_p, new ArrayList<String>());
				select_support_added_indexes.put(new_p, new ArrayList<Integer>());
				select_support_initial.put(new_p, curr_el);
			}

			if (act instanceof Go) {
				final Go go = (Go) act;
				final Window target = (Window) go.getWidget();
				if (curr.isSame(target)) {
					// we are already in the right window
					actions_executed.add(act);
					results.add(null);
					continue;
				}

				if (this.gui == null) {
					throw new Exception(
							"TestCaseRunner - runTestCase: gui is required to perform Go actions.");
				}

				Window curr_mapped_w = null;
				for (final Window ww : this.gui.getWindows()) {
					if (ww.isSame(curr)) {
						curr_mapped_w = ww;
						break;
					}
				}
				if (curr_mapped_w == null) {
					throw new Exception(
							"TestCaseRunner - runTestCase: current window could not be found in gui.");
				}

				go_actions = go.getActionSequence(curr_mapped_w, this.gui);
				actions.addAll(cont + 1, go_actions);
				actions_executed.add(act);
				if (go_actions.size() == 0) {
					results.add(null);
				}
				continue;
			}

			// the index of the action must be adjusted to the real one in the
			// app
			if (act instanceof Select) {
				final Select sel = (Select) act;
				final Selectable_widget sw = (Selectable_widget) sel.getWidget();
				final Pair new_p = new Pair(curr, sw);
				boolean found = false;
				for (final Pair p : select_support_initial.keySet()) {
					if (p.isSame(new_p)) {
						final int index = select_support_added_indexes.get(p).get(sel.getIndex());
						final GUIAction select = new Select(sel.getWindow(), sel.getOracle(),
								(Selectable_widget) sel.getWidget(), index);
						act = select;
						found = true;
						break;
					}
				}
				if (!found) {
					throw new Exception("TestCaseRunner - runTestCase: error in select.");
				}
			}
			// System.out.println("ACTION " + act);
			// System.out.println(act.getWidget().getId());
			// System.out.println(act.getWidget().getLabel());
			// System.out.println(act.getWindow().getId());
			// System.out.println(act.getWindow().getLabel());

			this.amanager.executeAction(act);
			gmanager.readGUI();
			this.dealWithErrorWindow(gmanager);
			actions_actually_executed.add(act);

			if (tc.containsAction(act)) {
				actions_executed.add(act);
				if (gmanager.getCurrentWindows().size() > 0) {
					results.add(this.getKnownWindowIfAny(gmanager.getCurrentWindows().get(0)));
				} else {
					results.add(null);
				}
			} else {
				if (go_actions != null && go_actions.get(go_actions.size() - 1) == act) {
					results.add(this.getKnownWindowIfAny(gmanager.getCurrentWindows().get(0)));
				}
			}
		}
		app.closeApplication();
		final GUITestCaseResult res = new GUITestCaseResult(tc, actions_executed, results,
				actions_actually_executed);
		return res;
	}

	/**
	 * function that checks if the window is already in the GUI, and if it is
	 * the case returns the input window with the id it has in the GUI
	 *
	 * @return
	 * @throws Exception
	 */
	private Window getKnownWindowIfAny(final Window in) throws Exception {

		for (final Window w : this.gui.getWindows()) {
			if (w.isSame(in)) {
				final Window out = new Window(in.getTo(), w.getId(), in.getLabel(), in.getClasss(),
						in.getX(), in.getY(), in.isModal());
				out.setRoot(in.isRoot());
				out.setAction_widgets(in.getActionWidgets());
				out.setInput_widgets(in.getInputWidgets());
				out.setSelectable_widgets(in.getSelectableWidgets());
				return out;
			}
		}
		return in;
	}

	private void dealWithErrorWindow(final GuiStateManager gmanager) throws Exception {

		if (gmanager.getCurrentWindows().size() > 0) {
			final Window current = gmanager.getCurrentWindows().get(0);
			final Pattern_error_window err = Pattern_error_window.getInstance();
			if (err.isMatch(current)) {
				// we create a click action (the window must have only one
				// action widget to match the err window)
				final Click click = new Click(current, null, current.getActionWidgets().get(0));
				this.amanager.executeAction(click);
				gmanager.readGUI();
			}
		}
	}

	private class Pair {

		Window w;
		Selectable_widget sw;

		public Pair(final Window w, final Selectable_widget sw) {

			this.w = w;
			this.sw = sw;
		}

		public boolean isSame(final Pair p) {

			return this.w.isSame(p.w) && this.sw.isSame(p.sw);
		}
	}
}
