package test.gui.semantic.testcase.inputdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import usi.testcase.inputdata.DataManager;

public class DataManagerTest {

	@Test
	public void test() {

		try {
			final DataManager dm = DataManager.getInstance();
			assertEquals(11, dm.getValidGenericData().size());
			// assertEquals(1, dm.getInvalidData("url").size());
			assertEquals(4, dm.getValidData("password and, url").size());
			assertEquals(1, dm.getValidItemizedData("from and, url").size());
			assertEquals("password", dm.getValidData("password").get(0));
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
