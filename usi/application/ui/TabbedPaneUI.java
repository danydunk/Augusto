package usi.application.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.GuiSubitemTestObject;
import com.rational.test.ft.object.interfaces.TestObject;

public class TabbedPaneUI extends MainHelper {

	// we use the tab label to distinguish the tabs
	public static void click(final TestObject testObject, final String tabLabel) throws Exception {

		final Object[] titles = (Object[]) testObject.getProperty("titleAt");

		int index = -1;
		for (int cont = 0; cont < titles.length; cont++) {
			if (titles[cont].toString().equals(tabLabel)) {
				index = cont;
			}
		}

		if (index == -1) {
			throw new Exception("TabbedPaneUI - click: tab label not found.");
		}
		new GuiSubitemTestObject(testObject).click(atIndex(index));
	}
}
