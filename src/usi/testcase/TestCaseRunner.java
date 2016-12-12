package src.usi.testcase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import src.usi.application.ActionManager;
import src.usi.application.ApplicationHelper;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Option_input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.dialogs.Pattern_dialogs;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.GUIAction;
import src.usi.testcase.structure.GUITestCase;
import src.usi.testcase.structure.Select;
import src.usi.util.DijkstraAlgorithm;
import src.usi.util.Graph;
import src.usi.util.Vertex;

import com.rational.test.ft.object.interfaces.TestObject;

public class TestCaseRunner {

	// used for go actions
	private final GUI gui;
	private Map<Pair, List<String>> select_support_initial;
	private Map<Pair, List<String>> select_support_added;
	private Map<Pair, List<Integer>> select_support_added_indexes;
	private final boolean skip_dialogs = true;

	public TestCaseRunner(final GUI gui) {

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
			System.out.println("Testcase Runner: no windows found.");
			if (app.isRunning()) {
				app.restartApplication();
			} else {
				app.startApplication();
			}
			Thread.sleep(10000);
			gmanager.readGUI();
			curr = gmanager.getCurrentActiveWindows();
		}

		this.select_support_initial = new HashMap<>();
		this.select_support_added = new HashMap<>();
		this.select_support_added_indexes = new HashMap<>();

		final List<GUIAction> actions = tc.getActions();
		// structures needed to construct the GUITestCaseResult
		final List<GUIAction> actions_executed = new ArrayList<>();
		final List<GUIAction> actions_actually_executed = new ArrayList<>();
		final List<Window> results = new ArrayList<>();

		this.updatedStructuresForSelect(curr);

		final Window initial = tc.getActions().get(0).getWindow();
		if (!curr.isSame(initial)) {
			// if we are not in the right window
			if (this.gui == null) {
				throw new Exception(
						"TestCaseRunner - runTestCase: gui is required to reach initial window.");
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

			final List<GUIAction> go_actions = this.getActionSequenceToGO(curr_mapped_w, initial);
			for (final GUIAction go : go_actions) {
				try {
					ActionManager.executeAction(go);
				} catch (final Exception e) {
					System.out.println("ERROR EXECUTING ACTION");
					e.printStackTrace();
					throw new Exception(
							"TestCaseRunner - runTestCase: impossible to reach initial window.");
				}
				gmanager.readGUI();
				this.dealWithDialogsWindow(gmanager);

				actions_actually_executed.add(go);
				this.updatedStructuresForSelect(gmanager.getCurrentActiveWindows());
			}
		}
		if (!gmanager.getCurrentActiveWindows().isSame(initial)) {
			throw new Exception("TestCaseRunner - runTestCase: impossible to reach initial window.");
		}

		mainloop: for (int cont = 0; cont < actions.size(); cont++) {

			final GUIAction act = actions.get(cont);
			curr = gmanager.getCurrentActiveWindows();
			GUIAction act_to_execute = act;

			// the index of the action must be adjusted to the real one in the
			// app
			if ((act instanceof Select)) {
				final Select sel = (Select) act;
				final boolean abs = sel.isAbstract();

				if (abs) {
					final Selectable_widget sw = (Selectable_widget) act.getWidget();

					int ind = sel.getIndex();
					final Pair new_p = new Pair(curr, sw);
					boolean found = false;
					for (final Pair p : this.select_support_initial.keySet()) {
						if (p.isSame(new_p)) {
							if (this.select_support_added_indexes.get(p).size() <= ind) {
								// the selectable widget is not as expected so
								// we select the last index
								ind = this.select_support_added_indexes.get(p).size() - 1;
								if (ind == -1) {
									continue;
								}

							}
							final int size = this.select_support_initial.get(p).size()
									+ this.select_support_added.get(p).size();
							final int index = this.select_support_added_indexes.get(p).get(ind);
							// TODO: we need to find a way to put the right
							// selected
							// index
							final Selectable_widget new_sw = new Selectable_widget(sw.getId(),
									sw.getLabel(), sw.getClasss(), sw.getX(), sw.getY(), size, 0);
							final GUIAction select = new Select(act.getWindow(), act.getOracle(),
									new_sw, index, false);
							act_to_execute = select;
							found = true;
							break;
						}
					}
					if (!found) {
						continue mainloop;
					}
				}
			}

			if (ActionManager.executeAction(act_to_execute)) {

				gmanager.readGUI();

				this.dealWithDialogsWindow(gmanager);

				// if (cont == actions.size() - 1) {
				// System.out.println();
				// }

				actions_actually_executed.add(act_to_execute);

				this.updatedStructuresForSelect(gmanager.getCurrentActiveWindows());

				actions_executed.add(act);

				if (gmanager.getCurrentActiveWindows() != null) {
					results.add(this.getKnownWindowIfAny(gmanager.getCurrentActiveWindows()));
				} else {
					results.add(null);
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
				// we need to filter out the selectable widgets cause their
				// position might change when they are scrolled

				final List<Widget> widgets = in.getWidgets().stream().filter(e -> {

					// we deal with selectable widgets separately cause
					// selecting an element can modify the position of
					// the
					// widget
						if (e instanceof Selectable_widget) {
							return false;
						}
						return true;
					}).collect(Collectors.toList());

				final List<Widget> widgets2 = w.getWidgets().stream().filter(e -> {

					// we deal with selectable widgets separately cause
					// selecting an element can modify the position of
					// the
					// widget
						if (e instanceof Selectable_widget) {
							return false;
						}
						return true;
					}).collect(Collectors.toList());

				for (int x = 0; x < widgets.size(); x++) {
					if (widgets2.get(x) instanceof Action_widget) {
						final Action_widget aw = (Action_widget) widgets.get(x);
						final Action_widget aw2 = (Action_widget) widgets2.get(x);
						final Action_widget new_aw = new Action_widget(aw2.getId(), aw.getLabel(),
								aw.getClasss(), aw.getX(), aw.getY());
						new_aw.setDescriptor(aw.getDescriptor());
						out.addWidget(new_aw);

					} else if (widgets2.get(x) instanceof Input_widget) {
						final Input_widget iw = (Input_widget) widgets.get(x);
						if (widgets2.get(x) instanceof Option_input_widget) {
							final Option_input_widget iw2 = (Option_input_widget) widgets2.get(x);
							final Option_input_widget oiw = (Option_input_widget) iw;
							final Option_input_widget new_oiw = new Option_input_widget(
									iw2.getId(), iw.getLabel(), iw.getClasss(), iw.getX(),
									iw.getY(), oiw.getSize(), oiw.getSelected());
							new_oiw.setDescriptor(iw.getDescriptor());
							out.addWidget(new_oiw);
						} else {
							final Input_widget iw2 = (Input_widget) widgets2.get(x);
							final Input_widget new_iw = new Input_widget(iw2.getId(),
									iw.getLabel(), iw.getClasss(), iw.getX(), iw.getY(),
									iw.getValue());
							new_iw.setDescriptor(iw.getDescriptor());
							out.addWidget(new_iw);

						}
					}
				}

				for (int x = 0; x < in.getSelectableWidgets().size(); x++) {
					if (w.getSelectableWidgets().get(x) instanceof Selectable_widget) {
						final Selectable_widget sw = in.getSelectableWidgets().get(x);
						final Selectable_widget sw2 = w.getSelectableWidgets().get(x);

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

	private void dealWithDialogsWindow(final GuiStateManager gmanager) throws Exception {

		if (!this.skip_dialogs) {
			return;
		}

		if (gmanager.getCurrentActiveWindows() != null) {
			final Window current = gmanager.getCurrentActiveWindows();
			for (final Pattern_dialogs dialog : Pattern_dialogs.values()) {

				if (dialog.isMatch(current)) {
					final List<GUIAction> acts = dialog.getActionsToGoPast(current);
					for (final GUIAction act : acts) {
						ActionManager.executeAction(act);
						gmanager.readGUI();
					}
				}
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

	private List<GUIAction> getActionSequenceToGO(final Window current, final Window targetw)
			throws Exception {

		final List<GUIAction> out = new ArrayList<>();
		final Graph g = Graph.convertGUI(this.gui);

		Vertex source = g.getVertex(current.getId());
		Vertex target = g.getVertex(targetw.getId());

		final DijkstraAlgorithm alg = new DijkstraAlgorithm(g);
		alg.execute(source);
		final LinkedList<Vertex> path = alg.getPath(target);

		if (path == null) {
			throw new Exception(
					"GUIAction - getActionSequence: action sequence could not be found.");
		}

		source = path.pop();
		while (!path.isEmpty()) {
			target = path.pop();
			Click click = null;
			final Window s = this.gui.getWindow(source.getId());
			final Window t = this.gui.getWindow(target.getId());
			for (final Action_widget aw : this.gui.getStaticBackwardLinks(t.getId())) {
				if (s.getWidget(aw.getId()) != null) {
					click = new Click(s, null, aw);
					break;
				}
			}
			if (click == null) {
				throw new Exception(
						"GUIAction - getActionSequence: error generating action sequence.");
			}
			out.add(click);
			source = target;
		}

		return out;
	}
}
