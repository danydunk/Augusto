package src.usi.gui.structure;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import src.usi.util.IDManager;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.vp.impl.TestDataText;

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

	// function that checks if the input widget has exactly the same properties
	protected boolean sameProperties_strong(final Widget w) {

		// // we use the position +- delta to match
		// final int delta = 1;
		// if (w.x > this.x + delta || w.x < this.x - delta) {
		// return false;
		// }
		// if (w.y > this.y + delta || w.y < this.y - delta) {
		// return false;
		// }

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

	// function that checks if the input widget has the same most important
	// properties
	// protected boolean sameProperties_weak(final Widget w) {
	//
	// // position and label can vary
	//
	// if (w.label == null && this.label != null) {
	// return false;
	// }
	// if (w.label != null && this.label == null) {
	// return false;
	// }
	// if (w.label != null && w.label.length() > 0 && this.label.length() == 0)
	// {
	// return false;
	// }
	// if (w.label != null && w.label.length() == 0 && this.label.length() > 0)
	// {
	// return false;
	// }
	//
	// if (w.descriptor == null && this.descriptor != null) {
	// return false;
	// }
	// if (w.descriptor != null && this.descriptor == null) {
	// return false;
	// }
	// if (w.descriptor != null && w.descriptor.length() > 0 &&
	// this.descriptor.length() == 0) {
	// return false;
	// }
	// if (w.descriptor != null && w.descriptor.length() == 0 &&
	// this.descriptor.length() > 0) {
	// return false;
	// }
	//
	// if (!w.classs.equals(this.classs)) {
	// return false;
	// }
	// return true;
	// }

	/**
	 * method used to recognise widgets
	 *
	 * @param w
	 * @return
	 */
	abstract public boolean isSame(Widget w);

	// abstract public boolean isSimilar(Widget w);

	/**
	 * method that transform a TO in the corresponding list of widgets it
	 * returns a list of widgets cause some TOs are transformed in several
	 * widgets
	 *
	 * @param to
	 * @param idm
	 * @return
	 * @throws Exception
	 */
	public static List<Widget> getWidgets(final TestObject to, final IDManager idm)
			throws Exception {

		final List<Widget> out = new ArrayList<>();
		TestObject support = to;

		Point p = null;

		try {
			p = (Point) support.getProperty("locationOnScreen");
		} catch (final Exception e) {}

		String type = null;
		try {
			type = to.getProperty("uIClassID").toString();
		} catch (final Exception e) {
			// it is a window
		}
		if (type == null) {
			// if it is a window
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			String title = "";
			try {
				title = to.getProperty("title").toString();
			} catch (final Exception e) {}
			final String classs = to.getProperty("class").toString();

			boolean modale;
			try {
				modale = Boolean.valueOf(to.getProperty("modal").toString());
			} catch (final Exception e) {
				// modal property not found
				modale = false;
			}
			out.add(new Window(to, idm.nextWindowId(), title, classs, x, y, modale));
			return out;
		}

		String label = null;
		try {
			label = to.getProperty("label").toString();
		} catch (final Exception e) {
			// label not found
		}

		try {
			if (label == null || label.length() == 0) {
				if (to.getProperty("hint") != null) {
					label = to.getProperty("hint").toString();
				}
			}
		} catch (final Exception e) {}

		if (type.equals("ButtonUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			out.add(new Action_widget(to, idm.nextAWId(), label, type, x, y));
			return out;

		} else if (type.equals("CheckBoxMenuItemUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final int size = 2;
			final int selected = Integer.valueOf(to.getProperty("selected").toString());

			if ((label == null || label.length() == 0) && selected == -1) {
				final TestDataText element = (TestDataText) to.getTestData("selected");
				if (element != null) {
					label = element.getText();
				}
			}

			out.add(new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, selected));
			return out;

		} else if (type.equals("CheckBoxUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final int size = 2;
			final boolean selected = Boolean.valueOf(to.getProperty("selected").toString());
			int index;
			if (selected) {
				index = 1;
			} else {
				index = 0;
			}
			out.add(new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, index));
			return out;

		} else if (type.equals("ComboBoxUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final boolean editable = Boolean.valueOf(to.getProperty("editable").toString());
			if (editable) {
				final String value = to.getProperty("text").toString();
				out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
				return out;
			}

			final int selected = Integer.valueOf(to.getProperty("selectedIndex").toString());
			final int size = Integer.valueOf(to.getProperty("itemCount").toString());

			if ((label == null || label.length() == 0) && selected == -1) {
				final TestDataText element = (TestDataText) to.getTestData("selected");
				if (element != null) {
					label = element.getText();
				}
			}

			out.add(new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, selected));
			return out;

		} else if (type.equals("EditorPaneUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("FormattedTextFieldUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("HyperlinkUI")) {
			if (p == null) {
				return out;
			}
			// TODO:

		} else if (type.equals("ListUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final int size = Integer.valueOf(to.getProperty(".itemCount").toString());
			final int selected = Integer.valueOf(to.getProperty("selectedIndex").toString());
			out.add(new Selectable_widget(to, idm.nextSWId(), label, type, x, y, size, selected));
			return out;

		} else if (type.equals("MenuItemUI")) {
			support = to.getMappableParent();
			while (p == null) {
				try {
					p = (Point) support.getProperty("locationOnScreen");
				} catch (final Exception e) {
					// widget not visible
					// we use the position of the first visible ancestor
					// System.out.println(to.getProperty("uIClassID"));
					support = support.getMappableParent();
				}
			}
			final int x = p.x;
			final int y = p.y;

			TestObject father = to.getMappableParent();
			String fatherlabel = null;
			while (fatherlabel == null) {
				try {
					fatherlabel = father.getProperty("label").toString();
				} catch (final Exception e) {
					father = father.getMappableParent();
				}
			}
			out.add(new Action_widget(to, idm.nextAWId(), fatherlabel + " - " + label, type, x, y));
			return out;

		} else if (type.equals("PasswordFieldUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("RadioButtonUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final boolean selected = Boolean.valueOf(to.getProperty("selected").toString());
			int index;
			if (selected) {
				index = 1;
			} else {
				index = 0;
			}
			final int size = 2;
			out.add(new Option_input_widget(to, idm.nextIWId(), label, type, x, y, size, index));
			return out;

		} else if (type.equals("TabbedPaneUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final int tabs = Integer.valueOf(to.getProperty("tabCount").toString());
			final int selected = Integer.valueOf(to.getProperty("selectedIndex").toString());
			final Object[] titles = (Object[]) to.getProperty("titleAt");

			// we add a action widget for each tab
			for (int cont = 0; cont < tabs; cont++) {
				if (cont == selected) {
					// the selected is skipped
					continue;
				}
				out.add(new Action_widget(to, idm.nextAWId(), titles[cont].toString(), type, x, y));
			}

		} else if (type.equals("TableUI")) {
			if (p == null) {
				return out;
			}

			// we consider only the rows
			final int x = p.x;
			final int y = p.y;
			final int rowc = Integer.valueOf(to.getProperty("rowCount").toString());
			// final int columnc =
			// Integer.valueOf(to.getProperty("columnCount").toString());
			// final int size = rowc * columnc;
			final int rows = Integer.valueOf(to.getProperty("selectedRow").toString());
			// final int columns =
			// Integer.valueOf(to.getProperty("selectedColumn").toString());
			// int selected = 0;
			// for (int cont = 0; cont < rows; cont++) {
			// selected += columnc;
			// }
			// selected += columns;
			out.add(new Selectable_widget(to, idm.nextSWId(), label, type, x, y, rowc, rows));
			return out;

		} else if (type.equals("TextAreaUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("TextFieldUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("TextPaneUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			final String value = to.getProperty("text").toString();
			out.add(new Input_widget(to, idm.nextIWId(), label, type, x, y, value));
			return out;

		} else if (type.equals("ToggleButtonUI")) {
			if (p == null) {
				return out;
			}
			final int x = p.x;
			final int y = p.y;
			out.add(new Action_widget(to, idm.nextAWId(), label, type, x, y));
			return out;

		}
		return out;
	}

	public void setTO(final TestObject to) {

		this.to = to;
	}
}
