package usi.application.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class ToggleButtonUI extends MainHelper {

	public static void click(final TestObject testObject) throws Exception {

		new ToggleGUITestObject(testObject).click();
	}

}
