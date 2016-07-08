package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class CheckBoxMenuItemUI extends MainHelper {

	public static void click(final Widget testObject) throws Exception {

		new ToggleGUITestObject(testObject.getTo()).click();
	}
}
