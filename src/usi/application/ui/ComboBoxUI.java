package src.usi.application.ui;

import resources.src.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.TextSelectGuiSubitemTestObject;

public class ComboBoxUI extends MainHelper {

	public static void select(final TestObject testObject, final long itemCount) {

		if (itemCount < 0) {
			return;
		}

		final TextSelectGuiSubitemTestObject combo = new TextSelectGuiSubitemTestObject(testObject);
		combo.click();

		sleep(0.2);

		combo.click(atIndex((int) itemCount));
	}

	public static void fill(final TestObject testObject, final String text) throws Exception {

		new TextSelectGuiSubitemTestObject(testObject).click();
		new TextSelectGuiSubitemTestObject(testObject).setProperty("text", text);
	}
}
