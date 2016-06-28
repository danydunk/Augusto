package usi.gui.widgets;

import java.util.Hashtable;

import com.rational.test.ft.object.interfaces.TestObject;

public class Widget {

	private final TestObject to;
	private final Hashtable<String, Object> widgetState;

	public Widget(TestObject to) {

		this.to = to;
		this.widgetState = new Hashtable<String, Object>();
		try {
			this.addPropertiesToMap(to.getProperty("uIClassID").toString());
		} catch (Exception e) {}

	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Widget other = (Widget) obj;
		if (this.to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!this.to.equals(other.to)) {
			return false;
		}
		return true;
	}

	public Hashtable<String, Object> getProperties() {

		return this.widgetState;
	}

	/*
	 * @Override public Object getProperty(String propertyName) { if
	 * (widgetState.containsKey(propertyName)){ return
	 * widgetState.get(propertyName); } else return null;
	 * 
	 * }
	 */

	public Object getProperty(String propertyName) {

		if (this.widgetState.containsKey(propertyName)) {
			return this.widgetState.get(propertyName);
		} else {
			try {
				this.addPropertyToMap(propertyName, this.to.getProperty(propertyName));
				return this.to.getProperty(propertyName);
			} catch (Exception e) {
				return "";
			}
		}

	}

	public boolean isSameWidget(Widget w) {

		return ((TestObject) w.getView()).isSameObject(this.to);
	}

	public String getType() {

		return this.getProperty("uIClassID").toString();
	}

	public boolean isActive() {

		return true;
	}

	public Object getView() {

		return this.to;
	}

	private void addPropertyToMap(String property, Object value) {

		if (value == null) {
			value = "";
		}
		this.widgetState.put(property, value);
	}

	private void addPropertiesToMap(String type) {

		if (type.equals("BusyLabelUI")) {

			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
		} else if (type.equals("ButtonUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
		} else if (type.equals("CheckBoxMenuItemUI")) {

			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
			this.addPropertyToMap("selected", this.to.getProperty("selected"));
		} else if (type.equals("CheckBoxUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
			this.addPropertyToMap("selected", this.to.getProperty("selected"));

		} else if (type.equals("ColorChooserUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
		} else if (type.equals("ComboBoxUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
			this.addPropertyToMap("selectedIndex", this.to.getProperty("selectedIndex"));
			this.addPropertyToMap("itemCount", this.to.getProperty("itemCount"));
		} else if (type.equals("ComponentUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
		} else if (type.equals("EditorPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));
		} else if (type.equals("FileChooserUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("FormattedTextFieldUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));
		} else if (type.equals("HyperlinkUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("label", this.to.getProperty("label"));

		} else if (type.equals("LabelUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("ListUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("minSelectionIndex", this.to.getProperty("minSelectionIndex"));
			this.addPropertyToMap("maxSelectionIndex", this.to.getProperty("maxSelectionIndex"));
			this.addPropertyToMap(".itemCount", this.to.getProperty(".itemCount"));
		} else if (type.equals("MenuBarUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("MenuItemUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
		} else if (type.equals("MenuUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
		} else if (type.equals("OptionPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("PanelUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
		} else if (type.equals("PasswordFieldUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));

		} else if (type.equals("PopupMenuSeparatorUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("PopupMenuUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));

		} else if (type.equals("ProgressBarUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		}

		else if (type.equals("RadioButtonUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("selected", this.to.getProperty("selected"));
		} else if (type.equals("ScrollBarUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("ScrollPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("SeparatorUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("SliderUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("minimum", this.to.getProperty("minimum"));
			this.addPropertyToMap("maximum", this.to.getProperty("maximum"));
			this.addPropertyToMap("value", this.to.getProperty("value"));
		} else if (type.equals("SpinnerUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("SplitPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("TabbedPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("selectedIndex", this.to.getProperty("selectedIndex"));
			this.addPropertyToMap("tabCount", this.to.getProperty("tabCount"));

		} else if (type.equals("TableHeaderUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));

		} else if (type.equals("TableUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("rowCount", this.to.getProperty("rowCount"));
			this.addPropertyToMap("columnCount", this.to.getProperty("columnCount"));
			this.addPropertyToMap("selectedRow", this.to.getProperty("selectedRow"));
			this.addPropertyToMap("selectedColumn", this.to.getProperty("selectedColumn"));
			this.addPropertyToMap("selectedColumnCount", this.to.getProperty("selectedColumnCount"));
			this.addPropertyToMap("selectedRowCount", this.to.getProperty("selectedRowCount"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));

		} else if (type.equals("TextAreaUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));
		} else if (type.equals("TextFieldUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));
		} else if (type.equals("TextPaneUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));
		} else if (type.equals("ToggleButtonUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("label", this.to.getProperty("label"));
			this.addPropertyToMap("text", this.to.getProperty("text"));
		}

		else if (type.equals("ToolBarUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
		} else if (type.equals("TreeUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
			this.addPropertyToMap("rowCount", this.to.getProperty("rowCount"));
			this.addPropertyToMap("maxSelectionRow", this.to.getProperty("maxSelectionRow"));
			this.addPropertyToMap("minSelectionRow", this.to.getProperty("minSelectionRow"));
			this.addPropertyToMap("editable", this.to.getProperty("editable"));

		} else if (type.equals("MonthViewUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
			this.addPropertyToMap("name", this.to.getProperty("name"));
		} else if (type.equals("DatePickerUI")) {
			this.addPropertyToMap("uIClassID", this.to.getProperty("uIClassID"));
		}
	}
}
