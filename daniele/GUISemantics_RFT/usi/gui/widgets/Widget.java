package usi.gui.widgets;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import com.rational.test.ft.object.interfaces.TestObject;

public class Widget {

	private final TestObject to;
	private final Map<String, String> widgetState;
	private final String id;
	private String descriptor;

	public Widget(final TestObject to, final String id) throws Exception {

		if (to == null || id == null) {
			throw new Exception("Widget: constructor error.");
		}
		this.to = to;
		this.id = id;
		this.widgetState = new HashMap<String, String>();
		try {
			this.addPropertiesToMap(to.getProperty("uIClassID").toString());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("Widget: error loading classid, " + e.getMessage());
		}
	}

	public Widget(final Map<String, String> properties, final String id) throws Exception {

		if (properties == null || !properties.containsKey("class") || id == null) {
			throw new Exception("Widget: constructor error.");
		}
		this.id = id;
		this.to = null;
		this.widgetState = new HashMap<String, String>(properties);
	}

	public boolean isSame(final Widget w) throws Exception {

		if (w == null || !(w instanceof Widget)) {
			throw new Exception("Widget - isSame: wrong input.");
		}
		if (this == w) {
			return true;
		}

		if (!this.getClass().equals(w.getClass())) {
			return false;
		}

		if (this.getProperty("x").equals(w.getProperty("x"))) {
			return false;
		}
		if (this.getProperty("y").equals(w.getProperty("y"))) {
			return false;
		}
		// TODO: FINISH
		return true;
	}

	public Map<String, String> getProperties() {

		return new HashMap<String, String>(this.widgetState);
	}

	public String getProperty(final String propertyName) {

		if (this.widgetState.containsKey(propertyName)) {
			return this.widgetState.get(propertyName);
		}
		return null;
	}

	public String getType() {

		return this.getProperty("class");
	}

	public TestObject getView() {

		return this.to;
	}

	private void addPropertyToMap(final String property, final String value) {

		this.widgetState.put(property, value);
	}

	private void addPropertiesToMap(final String type) {

		this.addPropertyToMap("class", this.to.getProperty("uIClassID").toString().toString());
		Point p = null;
		try {
			p = (Point) this.to.getProperty("locationOnScreen");
		} catch (final Exception e) {
			// widget not visible
			p = (Point) this.to.getMappableParent().getProperty("locationOnScreen");
		}
		this.addPropertyToMap("x", String.valueOf(p.x));
		this.addPropertyToMap("y", String.valueOf(p.y));

		if (type.equals("BusyLabelUI")) {

		} else if (type.equals("ButtonUI")) {
			String label = this.to.getProperty("label").toString();
			if (label == null || label.length() == 0) {
				if (this.to.getProperty("toolTipText") != null) {
					label = this.to.getProperty("toolTipText").toString();
				}
			}
			this.addPropertyToMap("label", label);

		} else if (type.equals("CheckBoxMenuItemUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());
			this.addPropertyToMap("selected", this.to.getProperty("selected").toString());

		} else if (type.equals("CheckBoxUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());
			this.addPropertyToMap("selected", this.to.getProperty("selected").toString());

		} else if (type.equals("ColorChooserUI")) {

		} else if (type.equals("ComboBoxUI")) {
			this.addPropertyToMap("selected", this.to.getProperty("selectedIndex").toString());
			this.addPropertyToMap("size", this.to.getProperty("itemCount").toString());

		} else if (type.equals("ComponentUI")) {

		} else if (type.equals("EditorPaneUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("FileChooserUI")) {
			this.addPropertyToMap("class", this.to.getProperty("uIClassID").toString());

		} else if (type.equals("FormattedTextFieldUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("HyperlinkUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());

		} else if (type.equals("LabelUI")) {
			final Object text = this.to.getProperty("text");
			String label = "";
			if (text != null) {
				label = text.toString();
			}
			this.addPropertyToMap("label", label);

		} else if (type.equals("ListUI")) {
			this.addPropertyToMap("size",
					String.valueOf(Integer.valueOf(this.to.getProperty("lastVisibleIndex").toString()) + 1));
			this.addPropertyToMap("selected", this.to.getProperty("selectedIndex").toString());

		} else if (type.equals("MenuBarUI")) {

		} else if (type.equals("MenuItemUI")) {
			final String label;
			label = this.to.getMappableParent().getProperty("label").toString() + " - "
					+ this.to.getProperty("label").toString();
			this.addPropertyToMap("label", label);

		} else if (type.equals("MenuUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());

		} else if (type.equals("OptionPaneUI")) {

		} else if (type.equals("PanelUI")) {

		} else if (type.equals("PasswordFieldUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("PopupMenuSeparatorUI")) {

		} else if (type.equals("PopupMenuUI")) {

		} else if (type.equals("ProgressBarUI")) {

		} else if (type.equals("RadioButtonUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());
			this.addPropertyToMap("selected", this.to.getProperty("selected").toString());

		} else if (type.equals("ScrollBarUI")) {

		} else if (type.equals("ScrollPaneUI")) {

		} else if (type.equals("SeparatorUI")) {

		} else if (type.equals("SliderUI")) {

		} else if (type.equals("SpinnerUI")) {

		} else if (type.equals("SplitPaneUI")) {

		} else if (type.equals("TabbedPaneUI")) {
			this.addPropertyToMap("selected", this.to.getProperty("selectedIndex").toString());
			this.addPropertyToMap("size", this.to.getProperty("tabCount").toString());
			this.addPropertyToMap("tabs", this.to.getProperty(".tabs").toString());

		} else if (type.equals("TableHeaderUI")) {

		} else if (type.equals("TableUI")) {
			final int rowc = Integer.valueOf(this.to.getProperty("rowCount").toString());
			final int columnc = Integer.valueOf(this.to.getProperty("columnCount").toString());
			final int size = rowc * columnc;
			final int rows = Integer.valueOf(this.to.getProperty("selectedRow").toString());
			final int columns = Integer.valueOf(this.to.getProperty("selectedColumn").toString());
			int selected = 0;
			for (int cont = 0; cont < rows; cont++) {
				selected += columnc;
			}
			selected += columns;
			this.addPropertyToMap("size", String.valueOf(size));
			this.addPropertyToMap("selected", String.valueOf(selected));

		} else if (type.equals("TextAreaUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("TextFieldUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("TextPaneUI")) {
			this.addPropertyToMap("value", this.to.getProperty("text").toString());
			this.addPropertyToMap("editable", this.to.getProperty("editable").toString());

		} else if (type.equals("ToggleButtonUI")) {
			this.addPropertyToMap("label", this.to.getProperty("label").toString());

		} else if (type.equals("ToolBarUI")) {

		} else if (type.equals("TreeUI")) {

		} else if (type.equals("MonthViewUI")) {

		} else if (type.equals("DatePickerUI")) {

		}
	}

	public String getId() {

		return this.id;
	}

	public String getDescriptor() {

		return this.descriptor;
	}

	public void setDescriptor(final String descriptor) {

		this.descriptor = descriptor;
	}
}
