package usi.gui.semantic.testcase;

import usi.action.ActionManager;
import usi.application.ApplicationHelper;
import usi.gui.GuiStateManager;

public class TestCaseRunner {

	private final boolean oracle;
	private final long sleep;
	private final ActionManager amanager;

	public TestCaseRunner(final long sleep, final boolean oracle) {

		this.sleep = sleep;
		this.oracle = oracle;
		this.amanager = new ActionManager(this.sleep);

	}

	public TestCaseRunner(final long sleep) {

		this(sleep, false);
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

		for (final GUIAction act : tc.getActions()) {

			this.amanager.executeAction(act);
			gmanager.readGUI();

			if (this.oracle) {
				// TODO: implement oracle
			}
		}

		return true;
	}
}
