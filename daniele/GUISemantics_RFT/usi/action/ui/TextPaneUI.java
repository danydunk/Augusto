package usi.action.ui;

import java.io.IOException;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.TextGuiSubitemTestObject;

public class TextPaneUI extends MainHelper {

	public static void setText(final Widget testObject, final String text) throws IOException {

		new TextGuiSubitemTestObject(testObject.getTo()).setProperty("text", text);
	}
}
