package test.usi.testcase.inputdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import src.usi.testcase.inputdata.DataManager;

public class DataManagerTest {

	@Test
	public void test() {

		try {
			final DataManager dm = DataManager.getInstance();

			assertEquals(19, dm.getGenericData().size());
			assertEquals(1, dm.getValidItemizedData("").size());
			assertEquals(5, dm.getValidData("starting on").size());
			assertEquals(0,
					dm.getValidItemizedData("<html><font color='gray'>&lt;from&gt;</font></html>")
					.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
