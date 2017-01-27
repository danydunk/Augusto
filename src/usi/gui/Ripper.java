package src.usi.gui;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import src.usi.application.ActionManager;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Window;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.dialogs.Pattern_dialogs;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.testcase.structure.Click;
import src.usi.testcase.structure.GUIAction;
import src.usi.util.IDManager;
import src.usi.xml.XMLUtil;

public class Ripper {

	private final List<String> action_widget_to_ignore;
	private final ApplicationHelper application;
	private GuiStateManager guimanager;
	private GUI gui;
	private List<Pattern_action_widget> aw_to_filter;
	private final boolean skip_dialogs = true;

	public Ripper() throws Exception {

		this.application = ApplicationHelper.getInstance();
		this.action_widget_to_ignore = new ArrayList<>();

		if (ConfigurationManager.getRipperFilters().length() == 0) {
			this.aw_to_filter = new ArrayList<>();
		} else {
			this.aw_to_filter = this.loadAWtoFilter(ConfigurationManager.getRipperFilters());
			if (this.aw_to_filter == null) {
				this.aw_to_filter = new ArrayList<>();
			}
		}
	}

	public Ripper(final GUI gui) throws Exception {

		this.application = ApplicationHelper.getInstance();
		this.action_widget_to_ignore = new ArrayList<>();
		this.gui = gui;
		if (ConfigurationManager.getRipperFilters().length() == 0) {
			this.aw_to_filter = new ArrayList<>();
		} else {
			this.aw_to_filter = this.loadAWtoFilter(ConfigurationManager.getRipperFilters());
			if (this.aw_to_filter == null) {
				this.aw_to_filter = new ArrayList<>();
			}
		}
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
		if (this.guimanager == null) {
			this.restart_and_go_to_window(actions, w);
		}
		final List<Action_widget> aws = this.filterAWS(w.getActionWidgets());

		mainloop: for (int cont = 0; cont < aws.size(); cont++) {

			final Action_widget aw = aws.get(cont);

			// we filter the aws
			for (final Pattern_action_widget paw : this.aw_to_filter) {
				if (paw.isMatch(aw)) {
					continue mainloop;
				}
			}

			if (this.action_widget_to_ignore.contains(aw.getClasss())) {
				continue;
			}
			final Click act = new Click(w, null, aw);
			ActionManager.executeAction(act);

			this.guimanager.readGUI();
			this.dealWithDialogsWindow(this.guimanager);
			final Window curr_window = this.guimanager.getCurrentActiveWindows();

			if (curr_window != null && w.isSame(curr_window)) {
				// same window
				// aws = curr_window.getActionWidgets();
				continue;
			}

			// if the windows is null the application was closed
			if (curr_window != null) {

				final Window match = this.isWindowNew(curr_window);
				if (match == null) {
					// new window
					this.gui.addWindow(curr_window);
					this.gui.addStaticEdge(aw.getId(), curr_window.getId());
					final List<GUIAction> actions_to_current = new ArrayList<>(actions);
					actions_to_current.add(act);
					this.ripWindow(actions_to_current, curr_window);

				} else {
					this.gui.addStaticEdge(aw.getId(), match.getId());
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
			this.dealWithDialogsWindow(this.guimanager);

			ActionManager.executeAction(act);
		}
		this.guimanager.readGUI();
		this.dealWithDialogsWindow(this.guimanager);

		if (!w.isSame(this.guimanager.getCurrentActiveWindows())) {
			// we read again the gui in case it was a problem of sleeptime
			Thread.sleep(ConfigurationManager.getSleepTime());
			this.guimanager.readGUI();
			this.dealWithDialogsWindow(this.guimanager);

			if (!w.isSame(this.guimanager.getCurrentActiveWindows())) {
				throw new Exception(
						"Ripper - restart_and_go_to_window: it was not possible to reach the selected window.");
			}
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

	// TODO: this code is duplicated
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

	private List<Pattern_action_widget> loadAWtoFilter(final String path) {

		Document doc = null;
		try {
			doc = XMLUtil.read(path);
		} catch (final Exception e) {
			System.out.println("Filter file not found.");
			return null;
		}
		final Node root = doc.getDocumentElement();
		final List<Pattern_action_widget> out = GUIPatternParser.createActionsWidgets(root);
		if (out != null && out.size() > 0) {
			return out;
		}
		return null;
	}
}
