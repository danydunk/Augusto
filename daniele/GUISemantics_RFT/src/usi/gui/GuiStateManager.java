package usi.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import usi.gui.widgets.GUI;
import usi.gui.widgets.Widget;
import usi.gui.widgets.Window;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.script.Property;
import com.rational.test.ft.script.SubitemFactory;

public class GuiStateManager {

	private final HashSet<String> testObjToFilter;
	private final ArrayList<Widget> currentWidgets;
	private final TestObject root;
	private final Property[] properties = new Property[3];

	public GuiStateManager(TestObject root) {

		this.root = root;

		this.currentWidgets = new ArrayList<Widget>();
		this.properties[0] = new Property("showing", "true");
		this.properties[1] = new Property("enabled", "true");
		this.properties[2] = new Property("visible", "true");

		this.testObjToFilter = new HashSet<String>();
		this.testObjToFilter.add("PopupMenuSeparatorUI");
		this.testObjToFilter.add("SeparatorUI");
	}

	public ArrayList<Widget> getCurrentWidgets() {

		return this.currentWidgets;
	}

	public GUI getCurrentGUI() throws Exception {

		TestObject[] appoggio = null;
		TestObject[] windows = null;

		try {
			appoggio = this.root.find(SubitemFactory.atChild(this.properties));
		} catch (Exception e) {
			throw new Exception("GUIStateManager - getCurrentGUI: error in find, " + e.getMessage());
		}

		for (int cont = 0; cont < 20; cont++) {
			try {
				Thread.sleep(200);
				windows = this.root.find(SubitemFactory.atChild(this.properties));
			} catch (Exception e) {
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

		Window rootWind = null;
		List<Window> winds = new ArrayList<>();

		for (TestObject wind : windows) {

			TestObject[] tos = null;
			Object[] o = null;

			try {
				tos = wind.find(SubitemFactory.atDescendant(this.properties));
			} catch (Exception e) {
				throw new Exception("GUIStateManager - getCurrentGUI: error in sub-widget find, " + e.getMessage());
			}

			// DZ: the test objects are ordered and filtered. Filtering and
			// ordering are done together to optimise the computation
			o = this.orderTOs(tos);
			tos = (TestObject[]) o[1];

			List<Widget> ws = new ArrayList<>();
			for (TestObject to : tos) {
				ws.add(new Widget(to));
			}

			Window w = new Window(wind, ws);
			winds.add(w);

			// the top window is detected
			if (rootWind == null) {
				if (!(wind.getProperty("name") != null && wind.getProperty("name").toString()
						.contains("overrideRedirect"))
						&& tos.length > 0) {
					rootWind = w;
				}
			}
		}

		return new GUI(winds, rootWind);
	}

	/**
	 * @author DZ Method that orders TOs w.r.t their properties. For each TO the
	 *         properties we consider for ordering are defined in JWidget.java
	 *         For ordering the class WidgetWRappe is used
	 * @param widget
	 *            list
	 * @return A ordered widget list and a string array containing the state
	 *         representation of each widget calculated with getPropertyValue
	 */
	private Object[] orderTOs(TestObject[] tos) {

		String[] outS = new String[tos.length];
		TOWrapper[] wws = new TOWrapper[tos.length];
		int cont = 0;
		for (TestObject w : tos) {
			wws[cont] = new TOWrapper(w);
			cont++;
		}
		Arrays.sort(wws);
		TestObject[] outArray = new TestObject[wws.length];

		cont = 0;
		for (TOWrapper ww : wws) {
			outArray[cont] = ww.getTO();
			outS[cont] = ww.getWidgetState();
			cont++;
		}

		ArrayList<TestObject> finalOut = new ArrayList<TestObject>();
		ArrayList<String> finalOutS = new ArrayList<String>();

		if (outArray.length > 0) {
			finalOut.add(outArray[0]);
			finalOutS.add(outS[0]);

			for (int c = 1, d = outArray.length; c < d; c++) {
				// DZ: TOs filtered
				if ((outArray[c].getProperty("uIClassID") != null && !this.testObjToFilter.contains(outArray[c]
						.getProperty("uIClassID"))) && !outArray[c].isSameObject(outArray[c - 1])) {
					finalOut.add(outArray[c]);
					finalOutS.add(outS[c]);
				}
			}
		}
		Object[] out = new Object[2];
		out[0] = finalOutS.toArray(new String[finalOutS.size()]);
		out[1] = finalOut.toArray(new TestObject[finalOut.size()]);
		return out;
	}

	/**
	 * 
	 * @param property
	 * @return
	 */
	private String getPropertyValue(Hashtable<String, Object> property) {

		Set<String> ks = property.keySet();
		String[] app = new String[ks.size()];
		String[] keys = ks.toArray(app);
		Arrays.sort(keys);
		String valueString = "";
		for (String key : keys) {
			valueString = valueString.concat(property.get(key).toString() + "#AbT#");
		}
		return valueString;
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
		private Widget w = null;
		private String widgetState = null;

		public TOWrapper(TestObject to) {

			this.to = to;
			this.w = new Widget(to);
			this.widgetState = GuiStateManager.this.getPropertyValue(this.w.getProperties());
		}

		@Override
		public int compareTo(Object arg0) {

			TOWrapper ww2 = (TOWrapper) arg0;
			String s1 = this.getWidgetState();
			String s2 = ww2.getWidgetState();
			int out = s1.compareTo(s2);
			if (out < 0) {
				return -1;
			}
			if (out > 0) {
				return 1;
			}
			return out;
		}

		public Widget getWidget() {

			return this.w;
		}

		public TestObject getTO() {

			return this.to;
		}

		public String getWidgetState() {

			return this.widgetState;
		}
	}
}
