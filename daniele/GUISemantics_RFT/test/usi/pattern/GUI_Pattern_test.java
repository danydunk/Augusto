package test.usi.pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.GUI;
import src.usi.pattern.structure.GUI_Pattern;
import test.usi.gui.GUIStructureMaker;

public class GUI_Pattern_test {

	@Test
	public void isInstance_test1() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());
			assertTrue(pattern.isInstance(res.get(0)));

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void isInstance_test2() {

		try {
			final GUI gui = GUIStructureMaker.instance1();

			final GUI_Pattern pattern = GUIPatternMaker.instance1();

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			assertEquals(1, res.size());
			assertEquals(2, res.get(0).getWindows().size());
			res.get(0).getGui().removeStaticEdge("aw1", "w2");
			assertFalse(pattern.isInstance(res.get(0)));

		} catch (final Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
