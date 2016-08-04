package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.RefinementHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.alloy.entity.Fact;
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
			ConfigurationManager.load("./files/for_test/config/upm_small.properties");
			ExperimentManager.init();

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-small_newripper.xml")
			.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			Instance_GUI_pattern match = res.get(0);
			match.generateSpecificSemantics();
			final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
			match = refiner.refine();
			if (match == null) {
				throw new Exception();
			}
			for (final Fact f : match.getSemantics().getFacts()) {
				if (f.getContent()
						.trim()
						.equals("one Field_4,Field_5:Property_unique|Property_unique = (Field_4+Field_5) and #Property_required = 0 and Field_4.associated_to = (Input_widget_iw7) and Field_5.associated_to = (Input_widget_iw2)")) {
					return;
				}
			}
			throw new Exception();

		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}
}
