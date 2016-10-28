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
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Selectable_widget;
import usi.gui.structure.Window;

import com.rational.test.ft.object.interfaces.TestObject;

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

	public GUITestCaseResult runTestCase(final GUITestCase tc) throws Exception {

		final ApplicationHelper app = ApplicationHelper.getInstance();
		if (app.isRunning()) {
			app.restartApplication();
		} else {
			app.startApplication();
		}

		final GuiStateManager gmanager = GuiStateManager.getInstance();
		gmanager.readGUI();

		Window curr = null;

		curr = gmanager.getCurrentActiveWindows();
		if (curr == null) {
			if (app.isRunning()) {
				app.restartApplication();
			} else {
				app.startApplication();
			}
			Thread.sleep(1000);
			gmanager.readGUI();
			curr = gmanager.getCurrentActiveWindows();
			// System.out.println(curr);
		}

		System.out.println(curr);
		this.select_support_initial = new HashMap<>();
		this.select_support_added = new HashMap<>();
		this.select_support_added_indexes = new HashMap<>();

		final List<GUIAction> actions = tc.getActions();
		// structures needed to construct the GUITestCaseResult
		final List<GUIAction> actions_executed = new ArrayList<>();
		final List<GUIAction> actions_actually_executed = new ArrayList<>();
		final List<Window> results = new ArrayList<>();
		List<GUIAction> go_actions = null;

		this.updatedStructuresForSelect(curr);

		mainloop: for (int cont = 0; cont < actions.size(); cont++) {

			final GUIAction act = actions.get(cont);
			curr = gmanager.getCurrentActiveWindows();
			GUIAction act_to_execute = act;

			if (act instanceof Go) {
				final Go go = (Go) act;
				final Window target = (Window) go.getWidget();
				System.out.println(curr);
				System.out.println(target);
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
			if ((act instanceof Select) || (act instanceof Select_doubleclick)) {

				final Selectable_widget sw = (Selectable_widget) act.getWidget();

				final int ind = (act instanceof Select) ? ((Select) act).getIndex()
						: ((Select_doubleclick) act).getIndex();
				final Pair new_p = new Pair(curr, sw);
				boolean found = false;
				for (final Pair p : this.select_support_initial.keySet()) {
					if (p.isSame(new_p)) {
						if (this.select_support_added_indexes.get(p).size() <= ind) {
							// the selectable widget is not as expected
							break mainloop;
						}
						final int size = this.select_support_initial.get(p).size()
								+ this.select_support_added.get(p).size();
						final int index = this.select_support_added_indexes.get(p).get(ind);
						// TODO: we need to find a way to put the right selected
						// index
						final Selectable_widget new_sw = new Selectable_widget(sw.getId(),
								sw.getLabel(), sw.getClasss(), sw.getX(), sw.getY(), size, 0);
						if (act instanceof Select) {
							final GUIAction select = new Select(act.getWindow(), act.getOracle(),
									new_sw, index);
							act_to_execute = select;
							found = true;
							break;
						}
						if (act instanceof Select_doubleclick) {
							final GUIAction select_dc = new Select_doubleclick(act.getWindow(),
									act.getOracle(), new_sw, index);
							act_to_execute = select_dc;
							found = true;
							break;
						}
					}
				}
				assert found;
			}

			try {
				this.amanager.executeAction(act_to_execute);
			} catch (final Exception e) {
				System.out.println("ERROR EXECUTING ACTION");
				e.printStackTrace();
				break mainloop;
			}
			gmanager.readGUI();

			this.dealWithErrorWindow(gmanager);

			actions_actually_executed.add(act_to_execute);

			this.updatedStructuresForSelect(gmanager.getCurrentActiveWindows());

			if (tc.containsAction(act)) {
				actions_executed.add(act);
				if (gmanager.getCurrentActiveWindows() != null) {
					results.add(this.getKnownWindowIfAny(gmanager.getCurrentActiveWindows()));
				} else {
					results.add(null);
				}
			} else {
				if (go_actions != null && go_actions.get(go_actions.size() - 1) == act) {
					results.add(this.getKnownWindowIfAny(gmanager.getCurrentActiveWindows()));
				}
			}
		}
		app.closeApplication();
		final GUITestCaseResult res = new GUITestCaseResult(tc, actions_executed, results,
				actions_actually_executed);
		return res;
	}

	private void updatedStructuresForSelect(final Window curr) throws Exception {

		if (curr == null) {
			return;
		}
		loop: for (final Selectable_widget sw : curr.getSelectableWidgets()) {
			final TestObject to = sw.getTo();
			final List<String> curr_el = Selectable_widget.getElements(to);

			final Pair new_p = new Pair(curr, sw);
			for (final Pair p : this.select_support_initial.keySet()) {
				if (p.isSame(new_p)) {
					// the objects that are not available anymore are removed
					final List<Integer> removed_indexes = new ArrayList<>();

					for (int c = 0; c < this.select_support_added.get(p).size(); c++) {
						final String el = this.select_support_added.get(p).get(c);
						if (!curr_el.contains(el)) {
							this.select_support_added.get(p).remove(c);
							this.select_support_added_indexes.get(p).remove(c);
							removed_indexes.add(c);
							c--;
						}
					}

					for (final String el : curr_el) {
						if (!this.select_support_initial.get(p).contains(el)
								&& !this.select_support_added.get(p).contains(el)) {
							if (removed_indexes.size() == 1) {
								// if one object is disappeared and one it
								// appeared we consider it as an update of the
								// previous (if the size is >1 or 0 we consider
								// it as addition)
								// TODO: this may work only for CRUD
								this.select_support_added.get(p).add(removed_indexes.get(0), el);
								this.select_support_added_indexes.get(p).add(
										removed_indexes.get(0), curr_el.indexOf(el));
							} else {
								this.select_support_added.get(p).add(el);
								this.select_support_added_indexes.get(p).add(curr_el.indexOf(el));
							}
						} else if (this.select_support_added.get(p).contains(el)) {
							// we update the indexes
							this.select_support_added_indexes.get(p).remove(
									this.select_support_added.get(p).indexOf(el));
							this.select_support_added_indexes.get(p).add(
									this.select_support_added.get(p).indexOf(el),
									curr_el.indexOf(el));
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

				// we can loop only once since if they are the same they must
				// have the same widgets number
				for (int x = 0; x < in.getWidgets().size(); x++) {
					if (w.getWidgets().get(x) instanceof Action_widget) {
						final Action_widget aw = (Action_widget) in.getWidgets().get(x);
						final Action_widget aw2 = (Action_widget) w.getWidgets().get(x);
						final Action_widget new_aw = new Action_widget(aw2.getId(), aw.getLabel(),
								aw.getClasss(), aw.getX(), aw.getY());
						new_aw.setDescriptor(aw.getDescriptor());
						out.addWidget(new_aw);

					} else if (w.getWidgets().get(x) instanceof Input_widget) {
						final Input_widget iw = (Input_widget) in.getWidgets().get(x);
						if (w.getWidgets().get(x) instanceof Option_input_widget) {
							final Option_input_widget iw2 = (Option_input_widget) w.getWidgets()
									.get(x);
							final Option_input_widget oiw = (Option_input_widget) iw;
							final Option_input_widget new_oiw = new Option_input_widget(
									iw2.getId(), iw.getLabel(), iw.getClasss(), iw.getX(),
									iw.getY(), oiw.getSize(), oiw.getSelected());
							new_oiw.setDescriptor(iw.getDescriptor());
							out.addWidget(new_oiw);
						} else {
							final Input_widget iw2 = (Input_widget) w.getWidgets().get(x);
							final Input_widget new_iw = new Input_widget(iw2.getId(),
									iw.getLabel(), iw.getClasss(), iw.getX(), iw.getY(),
									iw.getValue());
							new_iw.setDescriptor(iw.getDescriptor());
							out.addWidget(new_iw);

						}
					} else if (w.getWidgets().get(x) instanceof Selectable_widget) {
						final Selectable_widget sw = (Selectable_widget) in.getWidgets().get(x);
						final Selectable_widget sw2 = (Selectable_widget) w.getWidgets().get(x);
						// final TestObject to = sw.getTo();
						// final List<String> curr_el =
						// Selectable_widget.getElements(to);
						//
						// int index = -1;
						// // int size = 0;
						// final Pair new_p = new Pair(w, sw);
						// for (final Pair p :
						// this.select_support_initial.keySet()) {
						// if (p.isSame(new_p)) {
						// // for (final String el : curr_el) {
						// // if
						// // (this.select_support_added.get(p).contains(el))
						// // {
						// // //size++;
						// // }
						// // }
						//
						// if (sw.getSelected() != -1) {
						// final String selected =
						// curr_el.get(sw.getSelected());
						// index = this.select_support_added_indexes.get(p).get(
						// this.select_support_added.get(p).indexOf(selected));
						// }
						// break;
						// }
						// }

						final Selectable_widget new_sw = new Selectable_widget(sw2.getId(),
								sw.getLabel(), sw.getClasss(), sw.getX(), sw.getY(), sw.getSize(),
								sw.getSelected());
						new_sw.setDescriptor(sw.getDescriptor());
						out.addWidget(new_sw);

					}
				}

				return out;
			}
		}
		return in;
	}

	private void dealWithErrorWindow(final GuiStateManager gmanager) throws Exception {

		if (gmanager.getCurrentActiveWindows() != null) {
			final Window current = gmanager.getCurrentActiveWindows();
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
