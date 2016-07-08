package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

public class TableUI extends MainHelper {

	public static void click(final Widget testObject, final int cell) throws Exception {

		final TestObject to = testObject.getTo();
		final int columnNumber = ((Integer) to.getProperty("columnCount")).intValue();
		final int selectedRow = cell / columnNumber;
		final int selectedColumn = cell % columnNumber;

		new GuiSubitemTestObject(to).click(atCell(atRow(selectedRow), atColumn(selectedColumn)));
	}

	public static void doubleClick(final Widget testObject, final int cell) throws Exception {

		final TestObject to = testObject.getTo();
		final int columnNumber = ((Integer) to.getProperty("columnCount")).intValue();
		final int selectedRow = cell / columnNumber;
		final int selectedColumn = cell % columnNumber;

		new GuiSubitemTestObject(to).doubleClick(atCell(atRow(selectedRow),
				atColumn(selectedColumn)));
	}
}
