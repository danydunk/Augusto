package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class EditorPaneUI extends MainHelper {

	public static void setText(final TestObject testObject, final String text) throws Exception {

		new TextGuiSubitemTestObject(testObject).setProperty("text", text);
	}
}
