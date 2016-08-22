package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.UPM_fullHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.pattern.Pattern_window;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class UPM_full extends UPM_fullHelper {

	/**
	 * Script Name : <b>UPM_full</b> Generated : <b>Aug 7, 2016 2:20:28 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/08/07
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			ConfigurationManager.load("./files/for_test/config/upm.properties");
			ExperimentManager.init();
			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/crud.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			final List<Pattern_window> target_pw = pattern.getStaticForwardLinks(pattern
					.getWindow("confirmation_del").getWidget("paw12").getId());

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
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}
}
