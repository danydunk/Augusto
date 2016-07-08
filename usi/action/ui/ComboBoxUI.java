package usi.action.ui;

import resources.usi.MainHelper;
import usi.gui.structure.Widget;

import com.rational.test.ft.object.interfaces.TextSelectGuiSubitemTestObject;

public class ComboBoxUI extends MainHelper {

	public static void click(final Widget testObject, final long itemCount) {

		if (itemCount < 0) {
			return;
		}

		final TextSelectGuiSubitemTestObject combo = new TextSelectGuiSubitemTestObject(
				testObject.getTo());
		combo.click();

		sleep(0.2);

		combo.click(atIndex((int) itemCount));
	}

}
