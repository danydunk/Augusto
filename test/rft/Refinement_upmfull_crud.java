package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_upmfull_crudHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIParser;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.instance.Instance_GUI_pattern;
import usi.gui.structure.GUI;
import usi.pattern.GUIPatternParser;
import usi.pattern.structure.GUI_Pattern;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Refinement_upmfull_crud extends Refinement_upmfull_crudHelper {

	/**
	 * Script Name : <b>Refinement_upmfull_new_crud</b> Generated : <b>Sep 7,
	 * 2016 2:30:37 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/09/07
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			ConfigurationManager.load("./files/for_test/config/upm.properties");
			ExperimentManager.init();
			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/guipatterns/crud.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-full_newripper.xml")
			.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);

			Instance_GUI_pattern match = res.get(0);

			match.generateSpecificSemantics();
			final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
			match = refiner.refine();
			if (match == null) {
				throw new Exception("");
			}
		} catch (final AssertionError ee) {
			System.out.println("assertion");
			ee.printStackTrace();

		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}
}
