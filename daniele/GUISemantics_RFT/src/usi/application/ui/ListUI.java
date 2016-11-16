package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.SelectGuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

public class ListUI extends MainHelper {

	public static void click(final TestObject testObject, final long itemIndex) throws Exception {

		new SelectGuiSubitemTestObject(testObject).click(atIndex((int) itemIndex));
	}

	public static void doubleClick(final TestObject testObject, final long itemIndex)
			throws Exception {

		new SelectGuiSubitemTestObject(testObject).doubleClick(atIndex((int) itemIndex));
	}
}
