package usi.application.ui;

import java.io.IOException;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class TextPaneUI extends MainHelper {

	public static void fill(final TestObject testObject, final String text) throws IOException {

		new TextGuiSubitemTestObject(testObject).setText(text);
	}
}
