package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.SelectGuiSubitemTestObject;

public class ListUI extends MainHelper {

	public static void click(final Widget testObject, final long itemIndex) throws Exception {

		new SelectGuiSubitemTestObject(testObject.getTo()).click(atIndex((int) itemIndex));
	}

	public static void doubleClick(final Widget testObject, final long itemIndex) throws Exception {

		new SelectGuiSubitemTestObject(testObject.getTo()).doubleClick(atIndex((int) itemIndex));
	}
}
