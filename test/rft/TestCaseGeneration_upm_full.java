package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.TestCaseGeneration_upm_fullHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.GUIFunctionality_validate;
import usi.gui.functionality.mapping.Instance_GUI_pattern;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.GUI_Pattern;
import usi.gui.semantic.SpecificSemantics;
import usi.gui.semantic.alloy.Alloy_Model;
import usi.gui.semantic.alloy.entity.Fact;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class TestCaseGeneration_upm_full extends TestCaseGeneration_upm_fullHelper {

	/**
	 * Script Name : <b>TestCaseGeneration_upm_full</b> Generated : <b>Sep 5,
	 * 2016 6:48:02 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/09/05
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			// we load a gui pattern
			Document doc = XMLUtil.read(new File("./files/xml/crud.xml").getAbsolutePath());
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);
			ConfigurationManager.load("./files/for_test/config/upm_tc.properties");
			ExperimentManager.init();

			// we load the GUI structure
			doc = XMLUtil.read(new File("./files/for_test/xml/upm-full_newripper.xml")
			.getAbsolutePath());
			final GUI gui = GUIParser.parse(doc);

			final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
			final List<Instance_GUI_pattern> res = gfs.match(pattern);
			Instance_GUI_pattern match = res.get(0);

			if (match == null) {
				throw new Exception();
			}
			match.generateSpecificSemantics();

			final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
			match = refiner.refine();
			match.generateSpecificSemantics();

			final String constraint = "one Field_5:Property_unique|#Property_required = 0 and Property_unique = (Field_5) and Field_5.associated_to = (Input_widget_iw23)";
			final Fact newfact = new Fact("", constraint);
			final List<Fact> facts = match.getSemantics().getFacts();
			facts.add(newfact);

			final Alloy_Model mod = new Alloy_Model(match.getSemantics().getSignatures(), facts,
					match.getSemantics().getPredicates(), match.getSemantics().getFunctions(),
					match.getSemantics().getOpenStatements());
			match.setSpecificSemantics(SpecificSemantics.instantiate(mod));

			final GUIFunctionality_validate validator = new GUIFunctionality_validate(match, gui);
			validator.validate();

		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}
}
