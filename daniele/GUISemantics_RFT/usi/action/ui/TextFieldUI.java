package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class TextFieldUI extends MainHelper {

	public static void setText(final Widget testObject, final String text) throws Exception {

		new TextGuiSubitemTestObject(testObject.getTo()).setProperty("text", text);
	}
}