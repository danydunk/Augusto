package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class CheckBoxUI extends MainHelper {

	public static void select(final TestObject testObject, final long index) throws Exception {

		if (!(index == 0 || index == 1)) {
			throw new Exception("CheckBoxUI - fill: wrong index " + index);
		}

		final ToggleGUITestObject toggle = new ToggleGUITestObject(testObject);

		if (index == 0) {
			if (testObject.getProperty("selected").toString().equals("true")) {
				toggle.click();
			}
		} else {
			if (testObject.getProperty("selected").toString().equals("false")) {
				toggle.click();
			}
		}

	}

	public static void fill(final TestObject testObject, final String text) throws Exception {

		new TextGuiSubitemTestObject(testObject).setText(text);
	}

	public static void click(final TestObject testObject) throws Exception {

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
