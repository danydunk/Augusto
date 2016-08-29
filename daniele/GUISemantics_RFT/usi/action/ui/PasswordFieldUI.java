package usi.action.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class PasswordFieldUI extends MainHelper {

	public static void fill(final TestObject testObject, final String text) throws Exception {

		new TextGuiSubitemTestObject(testObject).setProperty("text", text);
	}
}
