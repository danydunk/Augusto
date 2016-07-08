package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class RadioButtonUI extends MainHelper {

	public static void clickSelect(final Widget testObject) throws Exception {

		new ToggleGUITestObject(testObject.getTo()).clickToState(SELECTED);
	}

	public static void clickNotSelect(final Widget testObject) throws Exception {

		new ToggleGUITestObject(testObject.getTo()).clickToState(NOT_SELECTED);
	}
}
