package usi.action;

import java.lang.reflect.Method;

import usi.configuration.ConfigurationManager;
import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
import usi.gui.semantic.testcase.Go;
import usi.gui.semantic.testcase.Select;
import usi.gui.semantic.testcase.Select_doubleclick;
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

		Window currWind = guimanager.getCurrentActiveWindows();
		if (currWind == null) {
			guimanager.readGUI();
			currWind = guimanager.getCurrentActiveWindows();
			if (currWind == null) {
				throw new Exception("ActionManager - executeAction: no current window.");
			}

		}
		// System.out.println(act.getWindow());
		// System.out.println(act.getWindow().getId());
		// System.out.println(currWind.getId());

		if (act.getWindow() == null || !currWind.isSame(act.getWindow())) {
			// we read again the gui in case it was a problem of sleeptime
			Thread.sleep(ConfigurationManager.getSleepTime());
			guimanager.readGUI();
			currWind = guimanager.getCurrentActiveWindows();
			if (act.getWindow() == null || !currWind.isSame(act.getWindow())) {
				throw new Exception("ActionManager - executeAction: wrong source reference.");
			}
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

		final Widget wid = this.findWidgetInCurrWindow(act.getWidget(), act.getWindow(), currWind);
		final TestObject to = wid.getTo();
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

			// TODO: find a better way to handle TabbedPaneUI
			// label is used to distinguish the different tabs
			if (act.getWidget().getClasss().equals("TabbedPaneUI")) {
				try {
					method.invoke(c.newInstance(), to, act.getWidget().getLabel());
				} catch (final Exception e) {
					try {
						Thread.sleep(100);
						method.invoke(c.newInstance(), to);
					} catch (final Exception ee) {
						e.printStackTrace();
						throw new Exception(
								"ActionManager - executeAction: error executing click, "
										+ ee.getMessage());
					}
				}

			} else {
				try {
					method.invoke(c.newInstance(), to);
				} catch (final Exception e) {
					try {
						Thread.sleep(100);
						method.invoke(c.newInstance(), to);
					} catch (final Exception ee) {
						e.printStackTrace();
						throw new Exception(
								"ActionManager - executeAction: error executing click, "
										+ ee.getMessage());
					}
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
				final Option_input_widget oiw = (Option_input_widget) wid;
				final int index = Integer.valueOf(fill.getInput());
				TestObject to_fill = null;
				if (oiw.getTOS().size() == 1) {
					to_fill = oiw.getTOS().get(0);
				} else {
					to_fill = oiw.getTOS().get(index);
				}
				try {
					method.invoke(c.newInstance(), to_fill, index);
				} catch (final Exception e) {
					e.printStackTrace();
					throw new Exception("ActionManager - executeAction: error executing fill, "
							+ e.getMessage());
				}
			} else {
				try {
					method.invoke(c.newInstance(), to, fill.getInput());
				} catch (final Exception e) {
					System.out.println("exception2");
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

		if (act instanceof Select_doubleclick) {
			final Select_doubleclick select = (Select_doubleclick) act;

			Method method = null;
			for (final Method m : ms) {
				if (m.getName().equals("doubleClick")) {
					method = m;
					break;
				}
			}

			try {
				method.invoke(c.newInstance(), to, select.getIndex());
			} catch (final Exception e) {
				throw new Exception(
						"ActionManager - executeAction: error executing select_doubleclick, "
								+ e.getMessage());
			}
		}

		Thread.sleep(this.sleeptime);
	}

	private Widget findWidgetInCurrWindow(final Widget w, final Window wind, final Window currWind) {

		final Widget ww = wind.getWidget(w.getId());
		final int index = wind.getWidgets().indexOf(ww);

		if (index == -1) {
			return null;
		}

		final Widget wid = currWind.getWidgets().get(index);
		if (!w.isSame(wid)) {
			return null;
		}

		return wid;
	}
}
