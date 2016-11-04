package usi.action;

import java.lang.reflect.Method;

import usi.configuration.ConfigurationManager;
import usi.gui.GuiStateManager;
import usi.gui.semantic.testcase.Click;
import usi.gui.semantic.testcase.Fill;
import usi.gui.semantic.testcase.GUIAction;
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
	 * GUI must be read before calling this method This function returns true if
	 * the action was executed, false if it could not be executed because the
	 * widget was disabled It throws an exception if there is no
	 */
	public boolean executeAction(final GUIAction act) throws Exception {

		final GuiStateManager guimanager = GuiStateManager.getInstance();

		Window currWind = guimanager.getCurrentActiveWindows();
		if (currWind == null) {
			guimanager.readGUI();
			currWind = guimanager.getCurrentActiveWindows();
			if (currWind == null) {
				return false;
			}

		}

		if (act.getWindow() == null || !currWind.isSame(act.getWindow())) {
			// we read again the gui in case it was a problem of sleeptime
			Thread.sleep(ConfigurationManager.getSleepTime());
			guimanager.readGUI();
			currWind = guimanager.getCurrentActiveWindows();
			if (act.getWindow() == null || !currWind.isSame(act.getWindow())) {
				return false;
			}
		}

		final String className = act.getWidget().getClasss();
		Class<?> c = null;
		try {
			c = Thread.currentThread().getContextClassLoader()
					.loadClass("usi.action.ui." + className);

		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		final Method[] ms = c.getDeclaredMethods();

		final Widget wid = this.findWidgetInCurrWindow(act.getWidget(), act.getWindow(), currWind);
		final TestObject to = wid.getTo();
		assert (to != null);

		if (!Boolean.valueOf(to.getProperty("enabled").toString())) {

			return false;
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
						return false;
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
						return false;
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
					try {
						method.invoke(c.newInstance(), to_fill, index);
					} catch (final Exception e) {
						e.printStackTrace();
						return false;
					}
				} else {
					to_fill = oiw.getTOS().get(index);
					try {
						method.invoke(c.newInstance(), to_fill);
					} catch (final Exception e) {
						e.printStackTrace();
						return false;
					}
				}

			} else {
				try {
					method.invoke(c.newInstance(), to, fill.getInput());
				} catch (final Exception e) {
					e.printStackTrace();
					return false;
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
				e.printStackTrace();
				return false;
			}
		}

		Thread.sleep(this.sleeptime);
		return true;
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
