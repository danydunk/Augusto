package usi.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import usi.gui.structure.Option_input_widget;
import usi.gui.structure.Widget;
import usi.gui.structure.Window;
import usi.util.IDManager;

import com.rational.test.ft.TargetGoneException;
import com.rational.test.ft.object.interfaces.RootTestObject;
import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.SubitemFactory;

public class GuiStateManager {

	private IDManager idm;

	private final RootTestObject root;
	private final Property[] properties = new Property[1];
	// private List<Window> currentWindows;
	private Window currentActiveWindow;
	private List<TestObject> currentTOs;
	private static GuiStateManager instance;

	public static GuiStateManager getInstance() {

		return instance;
	}

	public static void create(final RootTestObject root) throws Exception {

		instance = new GuiStateManager(root);
	}

	public static void destroy() {

		instance = null;
	}

	private GuiStateManager(final RootTestObject root) throws Exception {

		this.root = root;
		// this.properties[0] = new Property("showing", "true");
		// this.properties[0] = new Property("active", "true");
		this.properties[0] = new Property("visible", "true");
		// this.currentWindows = new ArrayList<>();
		this.currentActiveWindow = null;
		this.currentTOs = new ArrayList<>();
		this.idm = IDManager.getInstance();
		if (this.idm == null) {
			IDManager.create();
			this.idm = IDManager.getInstance();
		}
	}

	public List<Window> readGUI() throws Exception {

		this.idm.calculateLastIDs();
		this.currentActiveWindow = null;
		this.currentTOs = new ArrayList<>();

		TestObject[] appoggio = null;
		TestObject[] windows = null;

		try {
			appoggio = this.root.find(SubitemFactory.atChild(this.properties));
			// this.dom = (DomainTestObject) this.dom.find();
			// appoggio = this.dom.getTopObjects();
		} catch (final TargetGoneException tg) {
			windows = new TestObject[0];
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("GUIStateManager - getCurrentGUI: error in find, " + e.getMessage());
		}

		for (int cont = 0; cont < 5; cont++) {
			try {
				// RationalTestScript.unregisterAll();
				Thread.sleep(200);
				windows = this.root.find(SubitemFactory.atChild(this.properties));

				// this.dom = (DomainTestObject) this.dom.find();
				// windows = this.dom.getTopObjects();
			} catch (final TargetGoneException tg) {
				windows = new TestObject[0];
			} catch (final Exception e) {
				throw new Exception("GUIStateManager - getCurrentGUI: error in find, "
						+ e.getMessage());
			}

			if (windows.length != 0 && appoggio.length != 0 && appoggio.length == windows.length) {
				break;
			}
			appoggio = windows;
		}

		final List<Window> winds = new ArrayList<Window>();

		if (windows.length == 0) {
			return winds;
		}

		// we search for the top
		TestObject window = null;
		int cont = 0;
		while (window == null && cont < 10) {
			for (final TestObject to : windows) {

				final boolean focused = Boolean.valueOf(to.getProperty("focused").toString());

				if (focused) {
					window = to;
				}
			}
			if (window == null) {
				// RationalTestScript.unregisterAll();
				Thread.sleep(200);
				windows = this.root.find(SubitemFactory.atChild(this.properties));
			}
			cont++;
		}

		if (window == null) {
			throw new Exception("GUIStateManager - getCurrentGUI: error finding top window.");
		}

		// for (final TestObject wind : windows) {

		TestObject[] tos = null;

		try {
			tos = window.find(SubitemFactory.atDescendant(this.properties));
		} catch (final Exception e) {
			throw new Exception("GUIStateManager - getCurrentGUI: error in sub-widget find, "
					+ e.getMessage());
		}
		tos = this.filterTOSforTabbedPane(tos);
		this.currentTOs.addAll(Arrays.asList(tos));

		final ContextAnalyzer context = new ContextAnalyzer(new ArrayList<TestObject>(
				Arrays.asList(tos)));

		// windows with no widgets or with the override redirect flag are
		// filtered
		// if (tos.length == 0) {
		// continue;
		// }

		final List<Widget> wids = Widget.getWidgets(window, this.idm);
		if (wids.size() != 1 || !(wids.get(0) instanceof Window)) {
			throw new Exception(
					"GuiStateManager - getCurrentWindows: error, window not recognized.");
		}
		final Window w = (Window) wids.get(0);
		this.currentActiveWindow = w;
		List<Widget> widgets_to_add = new ArrayList<>();

		for (final TestObject to : tos) {
			// we keep only the widget of interest
			final List<Widget> widgets = Widget.getWidgets(to, this.idm);
			for (final Widget widget : widgets) {
				if (widget != null) {
					// if the widget does not have a label we look for
					// descriptors
					if (widget.getLabel() == null) {
						widget.setDescriptor(context.getDescriptor(widget.getTo()));
					}
					widgets_to_add.add(widget);
				}
			}
		}
		widgets_to_add = this.postProcessWidgetList(widgets_to_add, context);
		for (final Widget wid : widgets_to_add) {
			w.addWidget(wid);
		}

		winds.add(w);

		return winds;
	}

	/**
	 * the input list of widgets is processed to find widgets that can be
	 * grouped together (for instance radio buttons)
	 *
	 * @param widgets
	 * @return
	 * @throws Exception
	 */
	private List<Widget> postProcessWidgetList(final List<Widget> widgets,
			final ContextAnalyzer context) throws Exception {

		final List<Widget> out = new ArrayList<>(widgets);
		final List<Widget> radio_b = new ArrayList<>();
		for (final Widget wid : widgets) {
			if (wid.getClasss().equals("RadioButtonUI")) {
				radio_b.add(wid);
			}
		}
		final List<String> radio_b_descriptors = new ArrayList<>();
		for (final Widget wid : radio_b) {
			radio_b_descriptors.add(context.getDescriptor(wid.getTo()));
		}

		final Map<String, List<Widget>> map = new HashMap<>();
		for (int cont = 0; cont < radio_b.size(); cont++) {
			final Widget wid = radio_b.get(cont);
			final String desc = radio_b_descriptors.get(cont);
			if (map.containsKey(desc)) {
				map.get(desc).add(wid);
			} else {
				final List<Widget> list = new ArrayList<>();
				list.add(wid);
				map.put(desc, list);
			}
		}

		for (final String desc : map.keySet()) {
			final List<Widget> list = map.get(desc);
			int selected = -1;
			final List<TestObject> tos = new ArrayList<>();
			for (int x = 0; x < list.size(); x++) {
				final Option_input_widget oiw = (Option_input_widget) list.get(x);
				tos.add(oiw.getTo());
				if (oiw.getSelected() == 1) {
					if (selected != -1) {
						throw new Exception(
								"GUIStateManager - postProcessWidgetList: multiple selected in radio button list.");
					}
					selected = x;
				}
				out.remove(oiw);
			}
			if (selected == -1) {
				throw new Exception(
						"GUIStateManager - postProcessWidgetList: error finding selected radio button.");
			}
			final Option_input_widget new_oiw = new Option_input_widget(tos, list.get(0).getId(),
					"", "RadioButtonUI", list.get(0).getX(), list.get(0).getY(), list.size(),
					selected);
			new_oiw.setDescriptor(desc);
			out.add(new_oiw);
		}
		Collections.sort(out);
		return out;
	}

	/**
	 * method that filters out the widgets that are in the unselected tabs of
	 * the tabbedPanes (if any)
	 *
	 * @param tos
	 * @return
	 */
	private TestObject[] filterTOSforTabbedPane(final TestObject[] tos) {

		final List<TestObject> out_array = new ArrayList<>();
		mainloop: for (final TestObject to : tos) {
			if (!Boolean.valueOf(to.getProperty("showing").toString())) {
				// if the to is not showing
				TestObject parent = to.getMappableParent();
				while (parent != null && parent != this.root) {
					Object classui = null;
					try {
						classui = parent.getProperty("uIClassID");
					} catch (final Exception e) {}
					if (classui == null) {
						break;
					}
					if (classui.toString().equals("TabbedPaneUI")) {
						// it is a descendant of a tabbed pane, it means it is
						// in one of the unselected tabs
						continue mainloop;
					}
					parent = parent.getMappableParent();
				}
				out_array.add(to);
			} else {
				out_array.add(to);
			}
		}
		return out_array.toArray(new TestObject[out_array.size()]);
	}

	public List<TestObject> getCurrentTOs() {

		return new ArrayList<>(this.currentTOs);
	}

	public Window getCurrentActiveWindows() {

		return this.currentActiveWindow;
	}
}
