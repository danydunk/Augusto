package usi.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import usi.gui.structure.Widget;
import usi.gui.structure.Window;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.SubitemFactory;

public class GuiStateManager {

	private final TestObject root;
	private final Property[] properties = new Property[1];
	private List<Window> currentWindows;

	public GuiStateManager(final TestObject root) {

		this.root = root;

		// this.properties[0] = new Property("showing", "true");
		// this.properties[1] = new Property("enabled", "true");
		this.properties[0] = new Property("visible", "true");
	}

	public List<Window> getCurrentWindows() throws Exception {

		TestObject[] appoggio = null;
		TestObject[] windows = null;

		try {
			appoggio = this.root.find(SubitemFactory.atChild(this.properties));
		} catch (final Exception e) {
			throw new Exception("GUIStateManager - getCurrentGUI: error in find, " + e.getMessage());
		}

		for (int cont = 0; cont < 20; cont++) {
			try {
				Thread.sleep(200);
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

		if (windows.length == 0) {
			throw new Exception("GUIStateManager - getCurrentGUI: no windows found");
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

			final ContextAnalyzer context = new ContextAnalyzer(new ArrayList<TestObject>(
					Arrays.asList(tos)));

			// windows with no widgets or with the override redirect flag are
			// filtered
			if ((wind.getProperty("name") != null && wind.getProperty("name").toString()
					.contains("overrideRedirect"))
					|| tos.length == 0) {
				continue;
			}

			final Widget wid = Widget.getWidget(wind);
			if (!(wid instanceof Window)) {
				throw new Exception(
						"GuiStateManager - getCurrentWindows: error, window not recognized.");
			}
			final Window w = (Window) wid;

			for (final TestObject to : tos) {
				// we keep only the widget of interest
				final Widget widget = Widget.getWidget(to);
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

	public List<Window> getLastComputedWindows() {

		return new ArrayList<>(this.currentWindows);
	}
}
