package test.usi.gui.functionality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.GUI;
import src.usi.pattern.structure.GUI_Pattern;
import test.usi.gui.GUIStructureMaker;
import test.usi.pattern.GUIPatternMaker;

public class GUIFunctionality_search_Test {

	@Test
	public void test1() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = GUIPatternMaker.instance2();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test4() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test5() {

		try {
			final GUI gui = GUIStructureMaker.instance3();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test6() {

		try {
			final GUI gui = GUIStructureMaker.instance3();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test7() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test8() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(4, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;
			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test9() {

		try {
			final GUI gui = GUIStructureMaker.instance5();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(1).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(2, res.size());
			assertEquals(2, res.get(0).getWindows().size());

			ww1 = null;
			ww2 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test10() {

		try {
			final GUI gui = GUIStructureMaker.instance6();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				case "w3b":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(4, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test11() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test14() {

		try {
			final GUI gui = GUIStructureMaker.instance2();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test15() {

		try {
			final GUI gui = GUIStructureMaker.instance3();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			assertEquals(1, res.size());
			assertEquals(3, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test18() {

		try {
			final GUI gui = GUIStructureMaker.instance4();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(4, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test19() {

		try {
			final GUI gui = GUIStructureMaker.instance5();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(2, res.get(0).getWindows().size());
			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);

			assertEquals(3, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test20() {

		try {
			final GUI gui = GUIStructureMaker.instance6();

			final GUI_Pattern pattern = GUIPatternMaker.instance3();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(2, res.size());
			assertEquals(3, res.get(0).getWindows().size());

			Instance_window ww1 = null;
			Instance_window ww2 = null;
			Instance_window ww3 = null;
			Instance_window ww4 = null;

			for (final Instance_window ww : res.get(0).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1b":
					ww1 = ww;
					break;
				case "w2b":
					ww2 = ww;
					break;
				case "w3b":
					ww3 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);

			assertEquals(4, res.get(1).getWindows().size());
			ww1 = null;
			ww2 = null;
			ww3 = null;

			for (final Instance_window ww : res.get(1).getWindows()) {
				switch (ww.getInstance().getId()) {
				case "w1":
					ww1 = ww;
					break;
				case "w2":
					ww2 = ww;
					break;
				case "w4":
					ww3 = ww;
					break;
				case "w5":
					ww4 = ww;
					break;
				}
			}
			assertTrue(ww1 != null);
			assertTrue(ww2 != null);
			assertTrue(ww3 != null);
			assertTrue(ww4 != null);
		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
