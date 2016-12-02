package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

public class TableUI extends MainHelper {

	public static void click(final TestObject to, final int cell) throws Exception {

		// we consider only the rows
		// final int columnNumber = ((Integer)
		// to.getProperty("columnCount")).intValue();
		// final int selectedRow = cell / columnNumber;
		// final int selectedColumn = cell % columnNumber;

		new GuiSubitemTestObject(to).click(atCell(atRow(cell), atColumn(0)));
		Thread.sleep(200);
		new GuiSubitemTestObject(to).click(atCell(atRow(cell), atColumn(0)));
	}

	public static void doubleClick(final TestObject to, final int cell) throws Exception {

		// we consider only the rows
		// final int columnNumber = ((Integer)
		// to.getProperty("columnCount")).intValue();
		// final int selectedRow = cell / columnNumber;
		// final int selectedColumn = cell % columnNumber;

		new GuiSubitemTestObject(to).doubleClick(atCell(atRow(cell), atColumn(0)));
	}

	public static void deselect(final TestObject testObject) {

		new GuiSubitemTestObject(testObject).setState(DESELECT_ALL);
	}
}
