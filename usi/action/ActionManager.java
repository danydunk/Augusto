package usi.action;

import java.lang.reflect.Method;

import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.Go;
import usi.gui.semantic.testcase.Select;
import usi.gui.structure.Option_input_widget;

public class ActionManager {

	private final GuiStateManager guimanager;
	private final long sleeptime;

	public ActionManager(final GuiStateManager guimanager, final long sleeptime) {

		this.guimanager = guimanager;
		this.sleeptime = sleeptime;
	}

	public void executeAction(final GUIAction act) throws Exception {

		if (act.getWidget().getTo() == null) {
			throw new Exception("ActionManager - executeAction: missing TO reference.");
		}

		if (!this.guimanager.getCurrentTOs().contains(act.getWidget().getTo())) {
			throw new Exception("ActionManager - executeAction: TO not found.");
		}

		final String className = act.getWidget().getClasss();
		Class<?> c = null;
		try {
			c = Thread.currentThread().getContextClassLoader()
					.loadClass("usi.action.ui." + className);

		} catch (final ClassNotFoundException e) {
			throw new Exception("ActionManager - ActionManager: " + className + " not found.");
		}
		final Method[] ms = c.getDeclaredMethods();
		Method method = null;
		for (final Method m : ms) {
			if (m.getName().equals("click")) {
				method = m;
				break;
			}
		}

		if (act instanceof Click) {
			final Click click = (Click) act;

			try {
				method.invoke(c.newInstance(), click.getWidget());
			} catch (final Exception e) {
				try {
					Thread.sleep(100);
					method.invoke(c.newInstance(), click.getWidget());
				} catch (final Exception ee) {
					e.printStackTrace();
					throw new Exception("ActionManager - executeAction: error executing click, "
							+ ee.getMessage());
				}
			}
		}

		if (act instanceof Fill) {
			final Fill fill = (Fill) act;

			if (fill.getWidget() instanceof Option_input_widget) {
				final int index = Integer.valueOf(fill.getInput());
				try {
					method.invoke(c.newInstance(), fill.getWidget(), index);
				} catch (final Exception e) {
					throw new Exception("ActionManager - executeAction: error executing fill, "
							+ e.getMessage());
				}
			} else {
				try {
					method.invoke(c.newInstance(), fill.getWidget(), fill.getInput());
				} catch (final Exception e) {
					throw new Exception("ActionManager - executeAction: error executing fill, "
							+ e.getMessage());
				}
			}
		}

		if (act instanceof Select) {
			final Select select = (Select) act;

			try {
				method.invoke(c.newInstance(), select.getWidget(), select.getIndex());
			} catch (final Exception e) {
				throw new Exception("ActionManager - executeAction: error executing select, "
						+ e.getMessage());
			}
		}

		if (act instanceof Go) {
			throw new Exception(
					"ActionManager - executeAction: go actions must be elaborated before execution.");
		}

		Thread.sleep(this.sleeptime);
	}
}
