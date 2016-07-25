package usi.gui.ripping;

import java.util.ArrayList;
import java.util.List;

import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;
import usi.util.IDManager;

public class Ripper {

	private final List<String> action_widget_to_ignore;
	private final ApplicationHelper application;
	private GuiStateManager guimanager;
	private ActionManager actionManager;
	private GUI gui;
	private final long sleeptime;

	public Ripper(final long sleeptime) {

		this.application = ApplicationHelper.getInstance();
		this.sleeptime = sleeptime;
		this.action_widget_to_ignore = new ArrayList<>();
	}

	public Ripper(final long sleeptime, final GUI gui) {

		this.application = ApplicationHelper.getInstance();
		this.sleeptime = sleeptime;
		this.action_widget_to_ignore = new ArrayList<>();
		this.gui = gui;
		this.actionManager = new ActionManager(this.sleeptime);
	}

	public GUI ripApplication() throws Exception {

		this.gui = new GUI();
		IDManager.create(this.gui);
		this.application.startApplication();
		this.guimanager = GuiStateManager.getInstance();
		// we take only the first window of the list because that's the front
		// window
		final Window rootWind = this.guimanager.readGUI().get(0);
		rootWind.setRoot(true);
		this.gui.addWindow(rootWind);

		// list of action performed to reach a certain window
		final List<GUIAction> actions = new ArrayList<>();
		this.ripWindow(actions, rootWind);
		this.application.closeApplication();
		return this.gui;
	}

	public void ripWindow(final List<GUIAction> actions, final Window w) throws Exception {

		if (this.guimanager == null) {
			this.guimanager = GuiStateManager.getInstance();
		}
		List<Action_widget> aws = this.filterAWS(w.getActionWidgets());

		for (int cont = 0; cont < aws.size(); cont++) {

			final Action_widget aw = aws.get(cont);

			if (this.action_widget_to_ignore.contains(aw.getClasss())) {
				continue;
			}
			final Click act = new Click(w, null, aw);

			this.actionManager.executeAction(act);

			final List<Window> curr_windows = this.guimanager.readGUI();

			if (curr_windows.size() > 0 && w.isSame(curr_windows.get(0))) {
				// same window
				final List<Action_widget> aws2 = curr_windows.get(0).getActionWidgets();
				// we check that the action widgets are the same
				// we consider only the widgets that were in the window the
				// first time we reached it
				List<Action_widget> new_aws = new ArrayList<>();
				loop: for (final Action_widget aww : aws) {
					for (final Action_widget aww2 : aws2) {
						if (aww2.isSame(aww)) {
							// aww.setTO(aww2.getTo());
							new_aws.add(aww);
							continue loop;
						}
					}
					break loop;
				}
				new_aws = this.filterAWS(new_aws);
				if (new_aws.size() != aws.size() && cont < aws.size() - 1) {
					// some widgets were missing and there are still actions to
					// do, we restart the app
					this.restart_and_go_to_window(actions, w);
					continue;
				}
				aws = new_aws;
				continue;
			}

			// if the windows are 0 the application was closed
			if (curr_windows.size() > 0) {

				final Window current = curr_windows.get(0);

				final Window match = this.isWindowNew(current);
				if (match == null) {
					// new window
					this.gui.addWindow(current);
					this.gui.addEdge(aw.getId(), current.getId());
					final List<GUIAction> actions_to_current = new ArrayList<>(actions);
					actions_to_current.add(act);
					this.ripWindow(actions_to_current, current);

				} else {
					this.gui.addEdge(aw.getId(), match.getId());
				}
			}

			if (cont < aws.size() - 1) {
				// application is restarted and brought back to the window
				this.restart_and_go_to_window(actions, w);
			}
		}
	}

	// function that brings back the app to the target window by executing the
	// input sequence of actions
	// it returns the list of action widgets in the window with the current TOs
	private void restart_and_go_to_window(final List<GUIAction> actions, final Window w)
			throws Exception {

		this.application.restartApplication();
		this.guimanager = GuiStateManager.getInstance();
		for (final GUIAction act : actions) {
			this.guimanager.readGUI();

			this.actionManager.executeAction(act);
		}

		if (!w.isSame(this.guimanager.readGUI().get(0))) {
			throw new Exception(
					"Ripper - restart_and_go_to_window: it was not possible to reach the selected window.");
		}

		// // we check that the action widgets are the same
		// // we consider only the widgets that were in the window the
		// // first time we reached it
		// final List<Action_widget> new_aws = new ArrayList<>();
		// loop: for (final Action_widget aww : w.getActionWidgets()) {
		// for (final Action_widget aww2 :
		// this.guimanager.getCurrentWindows().get(0)
		// .getActionWidgets()) {
		// if (aww2.isSame(aww)) {
		// aww.setTO(aww2.getTo());
		// new_aws.add(aww);
		// continue loop;
		// }
		// }
		// break loop;
		// }
		// return new_aws;
	}

	private Window isWindowNew(final Window w) {

		final List<Window> windows = this.gui.getWindows();
		for (final Window wind : windows) {
			if (w.isSame(wind)) {
				return wind;
			}
		}
		return null;
	}

	private List<Action_widget> filterAWS(final List<Action_widget> aws) {

		final List<Action_widget> out = new ArrayList<>();
		for (final Action_widget aw : aws) {
			if (this.action_widget_to_ignore.contains(aw.getClasss())) {
				continue;
			}
			boolean en = true;
			try {
				en = Boolean.valueOf(aw.getTo().getProperty("enabled").toString());
			} catch (final Exception e) {
				// missing property
			}
			if (en) {
				out.add(aw);
			}
		}
		return out;
	}
}
