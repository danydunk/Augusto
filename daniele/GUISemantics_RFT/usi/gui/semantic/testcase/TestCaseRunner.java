package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.gui.GuiStateManager;
import usi.gui.structure.GUI;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.ITestDataElement;
import com.rational.test.ft.vp.ITestDataElementList;
import com.rational.test.ft.vp.impl.TestDataList;

public class TestCaseRunner {

	private final boolean oracle;
	private final long sleep;
	private final ActionManager amanager;
	// used for go actions
	private final GUI gui;

	public TestCaseRunner(final long sleep, final GUI gui, final boolean oracle) {

		this.sleep = sleep;
		this.oracle = oracle;
		this.amanager = new ActionManager(this.sleep);
		this.gui = gui;
	}

	public TestCaseRunner(final long sleep) {

		this(sleep, null, false);
	}

	public boolean runTestCase(final GUITestCase tc) throws Exception {

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

				final List<GUIAction> go_actions = go.getActionSequence(curr_mapped_w, this.gui);
				actions.addAll(cont + 1, go_actions);
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

			this.amanager.executeAction(act);
			gmanager.readGUI();

			if (this.oracle) {
				// TODO: implement oracle
			}
		}
		app.closeApplication();
		return true;
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
