package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.RefinementHelper;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Refinement extends RefinementHelper {

	/**
	 * Script Name : <b>Refinement</b> Generated : <b>Jul 21, 2016 12:45:04
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/07/21
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/add_pattern.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-small_newripper.xml")
					.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			final Instance_GUI_pattern match = res.get(0);
			match.generateSpecificSemantics();
			final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
			refiner.refine();
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}
}
