package src.usi.application.ui;

import java.io.IOException;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class TextPaneUI extends MainHelper {

	public static void fill(final TestObject testObject, final String text) throws IOException {

		new TextGuiSubitemTestObject(testObject).click();
		new TextGuiSubitemTestObject(testObject).setText(text);
	}
}
