package usi.action.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextSelectGuiSubitemTestObject;

public class ComboBoxUI extends MainHelper {

	public static void fill(final TestObject testObject, final long itemCount) {

		if (itemCount < 0) {
			return;
		}

		final TextSelectGuiSubitemTestObject combo = new TextSelectGuiSubitemTestObject(testObject);
		combo.click();

		sleep(0.2);

		combo.click(atIndex((int) itemCount));
	}

}
