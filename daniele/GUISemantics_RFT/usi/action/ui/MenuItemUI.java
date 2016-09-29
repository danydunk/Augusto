package usi.action.ui;

import resources.usi.MainHelper;

import com.rational.test.ft.object.interfaces.TestObject;
import com.rational.test.ft.object.interfaces.ToggleGUITestObject;

public class MenuItemUI extends MainHelper {

	public static void click(final TestObject testObject) throws Exception {

		TestObject father = testObject.getMappableParent();
		String fatherlabel = null;
		while (fatherlabel == null) {
			try {
				fatherlabel = father.getProperty("label").toString();
			} catch (final Exception e) {
				father = father.getMappableParent();
			}
		}

		new ToggleGUITestObject(father).click();
		sleep(0.2);
		new ToggleGUITestObject(testObject).click();
	}

}
