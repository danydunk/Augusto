package usi.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import usi.gui.widgets.Widget;
import usi.gui.widgets.Window;
import usi.util.IDManager;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.SubitemFactory;

public class GuiStateManager {

	private final List<String> widgetsOfInterest;
	private final ArrayList<Widget> currentWidgets;
	private final TestObject root;
	private final Property[] properties = new Property[1];
	private final IDManager ids;

	public GuiStateManager(final TestObject root) {

		this.root = root;
		this.ids = new IDManager();

		this.currentWidgets = new ArrayList<Widget>();
		// this.properties[0] = new Property("showing", "true");
		// this.properties[1] = new Property("enabled", "true");
		this.properties[0] = new Property("visible", "true");

		this.widgetsOfInterest = new ArrayList<String>();
		this.widgetsOfInterest.add("ButtonUI");
		this.widgetsOfInterest.add("MenuItemUI");
		this.widgetsOfInterest.add("TabbedPaneUI");
		this.widgetsOfInterest.add("FormattedTextFieldUI");
		this.widgetsOfInterest.add("PasswordFieldUI");
		this.widgetsOfInterest.add("javax.swing.JTextArea");
		this.widgetsOfInterest.add("TextAreaUI");
		this.widgetsOfInterest.add("TextFieldUI");
		this.widgetsOfInterest.add("RadioButtonUI");
		this.widgetsOfInterest.add("CheckBoxUI");
		this.widgetsOfInterest.add("ComboBoxUI");
		this.widgetsOfInterest.add("ListUI");
		this.widgetsOfInterest.add("TableUI");
	}

	public ArrayList<Widget> getCurrentWidgets() {

		return this.currentWidgets;
	}

	public List<Window> getCurrentGUI() throws Exception {

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
				throw new Exception("GUIStateManager - getCurrentGUI: error in find, " + e.getMessage());
			}

			if (windows != null && windows.length != 0 && appoggio != null && appoggio.length == windows.length) {
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
				throw new Exception("GUIStateManager - getCurrentGUI: error in sub-widget find, " + e.getMessage());
			}

			// System.out.println("WINDOW");
			// for (final TestObject tt : tos) {
			// System.out.println(tt.getProperty("uIClassID").toString());
			// }
			tos = this.orderTOs(tos);

			final ContextAnalyzer context = new ContextAnalyzer(new ArrayList<TestObject>(Arrays.asList(tos)));

			final List<Widget> ws = new ArrayList<Widget>();
			for (final TestObject to : tos) {
				// we keep only the widget of interest
				if (this.widgetsOfInterest.contains(to.getProperty("uIClassID").toString())) {
					final Widget widget = new Widget(to, this.ids.nextWidgetId());
					// if the widget does not have a label we look for
					// descriptors
					if (widget.getProperty("label") == null || widget.getProperty("label").length() == 0) {
						widget.setDescriptor(context.getDescriptor(widget.getView()));
					}
					ws.add(widget);
				}
			}

			final Window w = new Window(wind, ws, this.ids.nextWindowId());

			// windows with no widgets or with the override redirect flag are
			// filtered
			if (!(wind.getProperty("name") != null && wind.getProperty("name").toString().contains("overrideRedirect"))
					&& tos.length > 0) {
				winds.add(w);
			}

		}
		return winds;
	}

	/**
	 * @author DZ Method that orders TOs w.r.t their properties. For each TO the
	 *         properties we consider for ordering are defined in JWidget.java
	 *         For ordering the class WidgetWrapper is used
	 * @param widget
	 *            list
	 * @return A ordered TO list
	 * @throws Exception
	 */
	private TestObject[] orderTOs(final TestObject[] tos) throws Exception {

		final TOWrapper[] wws = new TOWrapper[tos.length];
		int cont = 0;
		for (final TestObject w : tos) {
			wws[cont] = new TOWrapper(w);
			cont++;
		}
		Arrays.sort(wws);
		final TestObject[] outArray = new TestObject[wws.length];

		cont = 0;
		for (final TOWrapper ww : wws) {
			outArray[cont] = ww.getTO();
			cont++;
		}

		return outArray;
	}

	/**
	 * 
	 * @author DZ Class that wraps a widget. It implements the Comparable
	 *         interface and compares widgets w.r.t their state representation
	 *         calculated with the method getPropertyValue. For each widget type
	 *         the properties we consider for comparing are defined in
	 *         JWidget.java
	 */
	public class TOWrapper implements Comparable {

		private TestObject to = null;
		private final int x;
		private final int y;

		public TOWrapper(final TestObject to) throws Exception {

			Point p = null;
			try {
				p = (Point) to.getProperty("locationOnScreen");
			} catch (final Exception e) {
				// widget not visible
				p = (Point) to.getMappableParent().getProperty("locationOnScreen");
			}
			this.to = to;
			this.x = p.x;
			this.y = p.y;
		}

		@Override
		public int compareTo(final Object arg0) {

			final TOWrapper in = (TOWrapper) arg0;
			final double x = in.getX();
			final double y = in.getY();

			if (this.y < y) {
				return -1;
			}
			if (this.y > y) {
				return 1;
			}
			// y must be equal
			if (this.x < x) {
				return -1;
			}
			if (this.x > x) {
				return 1;
			}
			// also x must be equal
			return 0;
		}

		public TestObject getTO() {

			return this.to;
		}

		public int getX() {

			return this.x;
		}

		public int getY() {

			return this.y;
		}
	}
}
