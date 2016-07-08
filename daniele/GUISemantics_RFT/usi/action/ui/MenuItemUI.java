package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class MenuItemUI extends MainHelper {

	public static void click(final Widget testObject) throws Exception {

		new ToggleGUITestObject(testObject.getTo().getMappableParent()).click();
		sleep(0.2);
		new ToggleGUITestObject(testObject.getTo()).click();
	}

}
