package usi.action;

import java.lang.reflect.Method;

import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.Go;
import usi.gui.semantic.testcase.Select;
import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Widget;
import usi.gui.structure.Window;

import com.rational.test.ft.object.interfaces.TestObject;

public class ActionManager {

	private final long sleeptime;

	public ActionManager(final long sleeptime) {

		this.sleeptime = sleeptime;
	}

	/*
	 *
	 * GUI must be read before calling this method
	 */
	public void executeAction(final GUIAction act) throws Exception {

		if (act instanceof Go) {
			throw new Exception(
					"ActionManager - executeAction: go actions must be elaborated before execution.");
		}

		final GuiStateManager guimanager = GuiStateManager.getInstance();
		final Window currWind = guimanager.getCurrentWindows().get(0);
		// System.out.println(act.getWindow());
		// System.out.println(act.getWindow().getId());
		// System.out.println(currWind.getId());

		if (act.getWindow() == null || !currWind.isSame(act.getWindow())) {
			throw new Exception("ActionManager - executeAction: wrong source reference.");
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

		final TestObject to = this.findWidgetInCurrWindow(act.getWidget(), currWind);
		if (to == null) {
			throw new Exception("ActionManager - executeAction: TO not found.");
		}

		if (act instanceof Click) {
			final Click click = (Click) act;

			Method method = null;
			for (final Method m : ms) {
				if (m.getName().equals("click")) {
					method = m;
					break;
				}
			}

			try {
				method.invoke(c.newInstance(), to);
			} catch (final Exception e) {
				try {
					Thread.sleep(100);
					method.invoke(c.newInstance(), to);
				} catch (final Exception ee) {
					e.printStackTrace();
					throw new Exception("ActionManager - executeAction: error executing click, "
							+ ee.getMessage());
				}
			}
		}

		if (act instanceof Fill) {
			final Fill fill = (Fill) act;

			Method method = null;
			for (final Method m : ms) {
				if (m.getName().equals("fill")) {
					method = m;
					break;
				}
			}

			if (fill.getWidget() instanceof Option_input_widget) {
				final int index = Integer.valueOf(fill.getInput());
				try {
					method.invoke(c.newInstance(), to, index);
				} catch (final Exception e) {
					e.printStackTrace();
					throw new Exception("ActionManager - executeAction: error executing fill, "
							+ e.getMessage());
				}
			} else {
				try {
					method.invoke(c.newInstance(), to, fill.getInput());
				} catch (final Exception e) {
					e.printStackTrace();
					throw new Exception("ActionManager - executeAction: error executing fill, "
							+ e.getMessage());
				}
			}
		}

		if (act instanceof Select) {
			final Select select = (Select) act;

			Method method = null;
			for (final Method m : ms) {
				if (m.getName().equals("click")) {
					method = m;
					break;
				}
			}

			try {
				method.invoke(c.newInstance(), to, select.getIndex());
			} catch (final Exception e) {
				throw new Exception("ActionManager - executeAction: error executing select, "
						+ e.getMessage());
			}
		}

		Thread.sleep(this.sleeptime);
	}

	private TestObject findWidgetInCurrWindow(final Widget w, final Window wind) {

		for (final Widget wid : wind.getWidgets()) {
			if (wid.isSame(w)) {
				return wid.getTo();
			}
		}
		return null;
	}
}
