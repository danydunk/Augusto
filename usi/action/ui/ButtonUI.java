package usi.action.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.GuiTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

public class ButtonUI extends MainHelper {

	public static void click(final TestObject testObject) throws Exception {

		new GuiTestObject(testObject).click();
	}
}
