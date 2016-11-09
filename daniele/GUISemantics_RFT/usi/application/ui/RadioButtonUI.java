package usi.application.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class RadioButtonUI extends MainHelper {

	public static void fill(final TestObject testObject) throws Exception {

		new ToggleGUITestObject(testObject).clickToState(SELECTED);
	}

	// public static void clickNotSelect(final TestObject testObject) throws
	// Exception {
	//
	// new ToggleGUITestObject(testObject).clickToState(NOT_SELECTED);
	// }
}
