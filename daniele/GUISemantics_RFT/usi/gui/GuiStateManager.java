package usi.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;
import usi.util.IDManager;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.SubitemFactory;

public class GuiStateManager {

	private IDManager idm;

	private final TestObject root;
	private final Property[] properties = new Property[1];
	private List<Window> currentWindows;
	private List<TestObject> currentTOs;
	private static GuiStateManager instance;

	public static GuiStateManager getInstance() {

		return instance;
	}

	public static void create(final TestObject root) {

		instance = new GuiStateManager(root);
	}

	public static void destroy() {

		instance = null;
	}

	private GuiStateManager(final TestObject root) {

		this.root = root;
		// this.properties[0] = new Property("showing", "true");
		// this.properties[1] = new Property("enabled", "true");
		this.properties[0] = new Property("visible", "true");
		this.currentWindows = new ArrayList<>();
		this.currentTOs = new ArrayList<>();
		this.idm = IDManager.getInstance();
	}

	public void setIDManager(final IDManager idm) {

		this.idm = idm;
	}

	public List<Window> readGUI() throws Exception {

		this.currentWindows = new ArrayList<>();
		this.currentTOs = new ArrayList<>();

		TestObject[] appoggio = null;
		TestObject[] windows = null;

		try {
			appoggio = this.root.find(SubitemFactory.atChild(this.properties));
		} catch (final Exception e) {
			throw new Exception("GUIStateManager - getCurrentGUI: error in find, " + e.getMessage());
		}

		for (int cont = 0; cont < 5; cont++) {
			try {
				Thread.sleep(100);
				windows = this.root.find(SubitemFactory.atChild(this.properties));
			} catch (final Exception e) {
				throw new Exception("GUIStateManager - getCurrentGUI: error in find, "
						+ e.getMessage());
			}

			if (windows != null && windows.length != 0 && appoggio != null
					&& appoggio.length == windows.length) {
				break;
			}
			appoggio = windows;
		}

		final List<Window> winds = new ArrayList<Window>();

		for (final TestObject wind : windows) {

			TestObject[] tos = null;

			try {
				tos = wind.find(SubitemFactory.atDescendant(this.properties));
			} catch (final Exception e) {
				throw new Exception("GUIStateManager - getCurrentGUI: error in sub-widget find, "
						+ e.getMessage());
			}

			this.currentTOs.addAll(Arrays.asList(tos));

			final ContextAnalyzer context = new ContextAnalyzer(new ArrayList<TestObject>(
					Arrays.asList(tos)));

			// windows with no widgets or with the override redirect flag are
			// filtered
			if ((wind.getProperty("name") != null && wind.getProperty("name").toString()
					.contains("overrideRedirect"))
					|| tos.length == 0) {
				continue;
			}

			final Widget wid = Widget.getWidget(wind, this.idm);
			if (!(wid instanceof Window)) {
				throw new Exception(
						"GuiStateManager - getCurrentWindows: error, window not recognized.");
			}
			final Window w = (Window) wid;

			for (final TestObject to : tos) {
				// we keep only the widget of interest
				final Widget widget = Widget.getWidget(to, this.idm);
				if (widget != null) {
					// if the widget does not have a label we look for
					// descriptors
					if (widget.getLabel() == null || widget.getLabel().length() == 0) {
						widget.setDescriptor(context.getDescriptor(widget.getTo()));
					}
					w.addWidget(widget);
				}
			}

			winds.add(w);

		}
		this.currentWindows = winds;
		return winds;
	}

	public List<TestObject> getCurrentTOs() {

		return new ArrayList<>(this.currentTOs);
	}

	public List<Window> getCurrentWindows() {

		return new ArrayList<>(this.currentWindows);
	}
}
