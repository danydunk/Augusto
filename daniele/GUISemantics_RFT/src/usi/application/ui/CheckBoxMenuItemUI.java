package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class CheckBoxMenuItemUI extends MainHelper {

	public static void select(final TestObject testObject, final long index) throws Exception {

		if (!(index == 0 || index == 1)) {
			throw new Exception("CheckBoxMenuItemUI - fill: wrong index " + index);
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

	public static void click(final TestObject testObject) throws Exception {

		new ToggleGUITestObject(testObject).click();
	}
}
