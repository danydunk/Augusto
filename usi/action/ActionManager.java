package usi.action;

import java.lang.reflect.Method;
import java.util.List;

import usi.gui.GuiStateManager;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Window;
import usi.guisemantic.testcase.Click;
import usi.guisemantic.testcase.Fill;
import usi.guisemantic.testcase.GUIAction;
import usi.guisemantic.testcase.Go;
import usi.guisemantic.testcase.Select;

public class ActionManager {

	private final GuiStateManager guimanager;
	private final long sleeptime;

	public ActionManager(final GuiStateManager guimanager, final long sleeptime) {

		this.guimanager = guimanager;
		this.sleeptime = sleeptime;
	}

	public void executeAction(final GUIAction act) throws Exception {

		final List<Window> winds = this.guimanager.getLastComputedWindows();
		boolean to_found = false;
		for (final Window wind : winds) {
			if (wind.getWidgets().contains(act.getWidget())) {
				to_found = true;
			}
		}

		if (!to_found) {
			throw new Exception("ActionManager - executeAction: widget not found.");
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
				e.printStackTrace();
				throw new Exception("ActionManager - executeAction: error executing click, "
						+ e.getMessage());
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
