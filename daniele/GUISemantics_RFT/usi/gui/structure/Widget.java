package usi.gui.structure;

import java.awt.Point;

import usi.util.IDManager;

import com.rational.test.ft.object.interfaces.TestObject;

public abstract class Widget implements Comparable<Widget> {

	protected final String id;
	protected final String label;
	protected String descriptor;
	protected final String classs;
	protected final int x;
	protected final int y;
	protected TestObject to;

	public Widget(final TestObject to, final String id, final String label, final String classs,
			final int x, final int y) throws Exception {

		if (to == null) {
			throw new Exception("Widget: null to.");
		}
		this.to = to;

		if (id == null || id.length() == 0) {
			throw new Exception("Widget: missing id.");
		}
		this.id = id;

		if (classs == null) {
			throw new Exception("Widget: missing class.");
		}
		this.classs = classs;

		if (x < 0 || y < 0) {
			throw new Exception("Widget:wrong position.");
		}
		this.x = x;
		this.y = y;

		this.label = label;
	}

	public Widget(final String id, final String label, final String classs, final int x, final int y)
			throws Exception {

		this.to = null;

		if (id == null || id.length() == 0) {
			throw new Exception("Widget: missing id.");
		}
		this.id = id;

		if (classs == null) {
			throw new Exception("Widget: missing class.");
		}
		this.classs = classs;

		if (x < 0 || y < 0) {
			throw new Exception("Widget:wrong position.");
		}
		this.x = x;
		this.y = y;

		this.label = label;
	}

	public String getId() {

		return this.id;
	}

	public String getLabel() {

		return this.label;
	}

	public String getClasss() {

		return this.classs;
	}

	public String getDescriptor() {

		return this.descriptor;
	}

	public void setDescriptor(final String descriptor) {

		this.descriptor = descriptor;
	}

	public int getX() {

		return this.x;
	}

	public int getY() {

		return this.y;
	}

	public TestObject getTo() {

		return this.to;
	}

	@Override
	public int compareTo(final Widget in) {

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

	protected boolean sameProperties(final Widget w) {

		// // we use the position +- delta to match
		// final int delta = 1;
		// if (w.x > this.x + delta || w.x < this.x - delta) {
		// return false;
		// }
		// if (w.y > this.y + delta || w.y < this.y - delta) {
		// return false;
		// }

		// TODO: what if the label changes?
		if (w.label == null && this.label != null) {
			return false;
		}
		if (w.label != null && this.label == null) {
			return false;
		}
		if (w.label != null && !w.label.equals(this.label)) {
			return false;
		}
		if (w.descriptor == null && this.descriptor != null) {
			return false;
		}
		if (w.descriptor != null && this.descriptor == null) {
			return false;
		}
		if (w.descriptor != null && !w.descriptor.equals(this.descriptor)) {
			return false;
		}
		if (!w.classs.equals(this.classs)) {
			return false;
		}
		return true;
	}

	/**
	 * method used to recognise widgets
	 *
	 * @param w
	 * @return
	 */
	abstract public boolean isSame(Widget w);

	public static Widget getWidget(final TestObject to, final IDManager idm) throws Exception {

		Point p = null;
		try {
			p = (Point) to.getProperty("locationOnScreen");
		} catch (final Exception e) {
			// widget not visible
			p = (Point) to.getMappableParent().getProperty("locationOnScreen");
		}

		final int x = p.x;
		final int y = p.y;

		String type = null;
		try {
			type = to.getProperty("uIClassID").toString();
		} catch (final Exception e) {
			// it is a window
		}
		if (type == null) {
			// if it is a window
			final String title = to.getProperty("title").toString();
			final String classs = to.getProperty("class").toString();

			boolean modale;
			try {
				modale = Boolean.valueOf(to.getProperty("modal").toString());
			} catch (final Exception e) {
				// modal property not found
				modale = false;
			}

			return new Window(to, idm.nextWindowId(), title, classs, x, y, modale);
		}

		String label = null;
		try {
			label = to.getProperty("label").toString();
		} catch (final Exception e) {
			// label not found
		}
		if (label == null || label.length() == 0) {
			if (to.getProperty("toolTipText") != null) {
				label = to.getProperty("toolTipText").toString();
			}
		}

		if (type.equals("ButtonUI")) {
			return new Action_widget(to, idm.nextAWId(), label, type, x, y);

		} else if (type.equals("CheckBoxMenuItemUI")) {
			final int size = 2;
			final int selected = Integer.valueOf(to.getProperty("selected").toString());
			return new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, selected);

		} else if (type.equals("CheckBoxUI")) {
			final int size = 2;
			final boolean selected = Boolean.valueOf(to.getProperty("selected").toString());
			int index;
			if (selected) {
				index = 1;
			} else {
				index = 0;
			}
			return new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, index);

		} else if (type.equals("ComboBoxUI")) {
			final int selected = Integer.valueOf(to.getProperty("selectedIndex").toString());
			final int size = Integer.valueOf(to.getProperty("itemCount").toString());
			return new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, selected);

		} else if (type.equals("EditorPaneUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("FormattedTextFieldUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("HyperlinkUI")) {
			// TODO:

		} else if (type.equals("ListUI")) {
			final int size = Integer.valueOf(to.getProperty("lastVisibleIndex").toString()) + 1;
			final int selected = Integer.valueOf(to.getProperty("selectedIndex").toString());
			return new Selectable_widget(to, idm.nextSWId(), label, type, x, y, size, selected);

		} else if (type.equals("MenuItemUI")) {
			final TestObject father = to.getMappableParent();
			final String fatherlabel = father.getProperty("label").toString();
			return new Action_widget(to, idm.nextAWId(), fatherlabel + " - " + label, type, x, y);

		} else if (type.equals("PasswordFieldUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("RadioButtonUI")) {
			final boolean selected = Boolean.valueOf(to.getProperty("selected").toString());
			int index;
			if (selected) {
				index = 1;
			} else {
				index = 0;
			}
			final int size = Integer.valueOf(to.getProperty("itemCount").toString());
			return new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, index);

		} else if (type.equals("TabbedPaneUI")) {
			// TODO: deal with tabbed pane
			// this.addPropertyToMap("selected",
			// this.to.getProperty("selectedIndex").toString());
			// this.addPropertyToMap("size",
			// this.to.getProperty("tabCount").toString());
			// this.addPropertyToMap("tabs",
			// this.to.getProperty(".tabs").toString());

		} else if (type.equals("TableUI")) {
			final int rowc = Integer.valueOf(to.getProperty("rowCount").toString());
			final int columnc = Integer.valueOf(to.getProperty("columnCount").toString());
			final int size = rowc * columnc;
			final int rows = Integer.valueOf(to.getProperty("selectedRow").toString());
			final int columns = Integer.valueOf(to.getProperty("selectedColumn").toString());
			int selected = 0;
			for (int cont = 0; cont < rows; cont++) {
				selected += columnc;
			}
			selected += columns;
			return new Selectable_widget(to, idm.nextSWId(), label, type, x, y, size, selected);

		} else if (type.equals("TextAreaUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("TextFieldUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("TextPaneUI")) {
			final String value = to.getProperty("text").toString();
			return new Input_widget(to, idm.nextIWId(), label, type, x, y, value);

		} else if (type.equals("ToggleButtonUI")) {
			return new Action_widget(to, idm.nextAWId(), label, type, x, y);

		}
		return null;
	}

	public void setTO(final TestObject to) {

		this.to = to;
	}
}
