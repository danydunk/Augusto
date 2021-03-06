package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class FormattedTextFieldUI extends MainHelper {

	public static void fill(final TestObject testObject, final String text) throws Exception {

		new TextGuiSubitemTestObject(testObject).click();
		new TextGuiSubitemTestObject(testObject).setProperty("text", text);
	}
}