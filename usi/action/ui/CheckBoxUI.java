package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class CheckBoxUI extends MainHelper {

	public static void click(final Widget widget) throws Exception {

		final TestObject testObject = widget.getTo();
		final ToggleGUITestObject toggle = new ToggleGUITestObject(testObject);

		Object ob;
		try {
			ob = testObject.getParent().getParent().getProperty("uIClassID");

			if (ob != null) {
				final String proper = ob.toString();
				if (proper.equals("ViewportUI")) {
					if (testObject.getProperty("selected").toString().equals("true")) {
						toggle.setState(NOT_SELECTED);

					} else {
						toggle.setState(SELECTED);
					}
				} else {
					if (testObject.getProperty("selected").toString().equals("true")) {
						toggle.clickToState(NOT_SELECTED);

					} else {
						toggle.clickToState(SELECTED);
					}
				}
			}
		} catch (final Exception e) {
			if (testObject.getProperty("selected").toString().equals("true")) {
				toggle.clickToState(NOT_SELECTED);

			} else {
				toggle.clickToState(SELECTED);
			}
		}
	}
}
