package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.gui.GuiStateManager;
import usi.gui.pattern.Pattern_error_window;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
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
	private Map<Pair, List<String>> select_support_initial;
	private Map<Pair, List<String>> select_support_added;
	private Map<Pair, List<Integer>> select_support_added_indexes;

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
		this.select_support_initial = new HashMap<>();
		this.select_support_added = new HashMap<>();
		this.select_support_added_indexes = new HashMap<>();

		final List<GUIAction> actions = tc.getActions();
		// structures needed to construct the GUITestCaseResult
		final List<GUIAction> actions_executed = new ArrayList<>();
		final List<GUIAction> actions_actually_executed = new ArrayList<>();
		final List<Window> results = new ArrayList<>();
		List<GUIAction> go_actions = null;

		Window curr = gmanager.getCurrentWindows().get(0);
		this.updatedStructuresForSelect(curr);

		for (int cont = 0; cont < actions.size(); cont++) {

			GUIAction act = actions.get(cont);
			curr = gmanager.getCurrentWindows().get(0);

			if (act instanceof Go) {
				final Go go = (Go) act;
				final Window target = (Window) go.getWidget();
				if (curr.isSame(target)) {
					// we are already in the right window
					actions_executed.add(act);
					results.add(this.getKnownWindowIfAny(curr));
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
				for (final Pair p : this.select_support_initial.keySet()) {
					if (p.isSame(new_p)) {
						final int index = this.select_support_added_indexes.get(p).get(
								sel.getIndex());
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

			this.updatedStructuresForSelect(gmanager.getCurrentWindows().get(0));

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

	private void updatedStructuresForSelect(final Window curr) {

		if (curr == null) {
			return;
		}
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
			for (final Pair p : this.select_support_initial.keySet()) {
				if (p.isSame(new_p)) {
					for (final String el : curr_el) {
						if (!this.select_support_initial.get(p).contains(el)
								&& !this.select_support_added.get(p).contains(el)) {
							this.select_support_added.get(p).add(el);
							this.select_support_added_indexes.get(p).add(curr_el.indexOf(el));
						}
					}
					for (int c = 0; c < this.select_support_added.get(p).size(); c++) {
						final String el = this.select_support_added.get(p).get(c);
						if (!curr_el.contains(el)) {
							this.select_support_added.get(p).remove(c);
							this.select_support_added_indexes.get(p).remove(c);
							c--;
						}
					}
					continue loop;
				}
			}
			// if the sw is seen for the first time
			this.select_support_added.put(new_p, new ArrayList<String>());
			this.select_support_added_indexes.put(new_p, new ArrayList<Integer>());
			this.select_support_initial.put(new_p, curr_el);
		}
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
				out.setRoot(w.isRoot());
				loop: for (final Action_widget aw : in.getActionWidgets()) {
					for (final Action_widget aw2 : w.getActionWidgets()) {
						if (aw2.isSame(aw)) {
							final Action_widget new_aw = new Action_widget(aw2.getId(),
									aw.getLabel(), aw.getClasss(), aw.getX(), aw.getY());
							new_aw.setDescriptor(aw.getDescriptor());
							out.addWidget(new_aw);
							continue loop;
						}
					}
					throw new Exception(
							"TestCaseRunner - getKnownWindowIfAny: action widget not found.");
				}

				loop: for (final Input_widget iw : in.getInputWidgets()) {
					for (final Input_widget iw2 : w.getInputWidgets()) {
						if (iw2.isSame(iw)) {
							final Input_widget new_iw = new Input_widget(iw2.getId(),
									iw.getLabel(), iw.getClasss(), iw.getX(), iw.getY(),
									iw.getValue());
							new_iw.setDescriptor(iw.getDescriptor());
							out.addWidget(new_iw);
							continue loop;
						}
					}
					throw new Exception(
							"TestCaseRunner - getKnownWindowIfAny: input widget not found.");
				}

				loop: for (final Selectable_widget sw : in.getSelectableWidgets()) {
					for (final Selectable_widget sw2 : w.getSelectableWidgets()) {
						if (sw2.isSame(sw)) {

							final TestObject to = sw.getTo();
							final TestDataList list = (TestDataList) to.getTestData("list");
							final ITestDataElementList el_list = list.getElements();
							final List<String> curr_el = new ArrayList<>();
							for (int c = 0; c < el_list.getLength(); c++) {
								final ITestDataElement element = el_list.getElement(c);
								curr_el.add(element.getElement().toString());
							}

							int index = -1;
							int size = 0;
							final Pair new_p = new Pair(w, sw);
							for (final Pair p : this.select_support_initial.keySet()) {
								if (p.isSame(new_p)) {
									for (final String el : curr_el) {
										if (this.select_support_added.get(p).contains(el)) {
											size++;
										}
									}

									if (sw.getSelected() != -1) {
										final String selected = curr_el.get(sw.getSelected());
										index = this.select_support_added_indexes.get(p).get(
												this.select_support_added.get(p).indexOf(selected));
									}
									break;
								}
							}

							final Selectable_widget new_sw = new Selectable_widget(sw2.getId(),
									sw.getLabel(), sw.getClasss(), sw.getX(), sw.getY(), size,
									index);
							new_sw.setDescriptor(sw.getDescriptor());
							out.addWidget(new_sw);
							continue loop;
						}
					}
					throw new Exception(
							"TestCaseRunner - getKnownWindowIfAny: selectable widget not found.");
				}

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
