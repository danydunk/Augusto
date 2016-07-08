package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.GuiTestObject;

public class ButtonUI extends MainHelper {

	public static void click(final Widget testObject) throws Exception {

		new GuiTestObject(testObject.getTo()).click();
	}
}
