package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_upmfull_crudHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_refine;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.GUI;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.semantic.alloy.structure.Fact;
import src.usi.xml.XMLUtil;

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
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		if (args.length == 1) {
			PathsManager.setProjectRoot(args[0].toString());
		}
		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm.properties");
		ExperimentManager.init();

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot()
				+ "/files/guipatterns/crud_no_read.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/upm.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);

		Instance_GUI_pattern match = res.get(0);

		match.generateSpecificSemantics();
		final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
		try {
			match = refiner.refine();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		ExperimentManager.cleanUP();

		if (match == null) {
			throw new Exception("");
		}

		String sem_prop = null;
		for (final Fact fact : match.getSemantics().getFacts()) {
			if (fact.getIdentifier().equals("semantic_property")) {
				sem_prop = fact.getContent();
			}
		}

		if (sem_prop.startsWith("not") || !sem_prop.contains("#Property_semantic_0.requireds = 0")
				|| !sem_prop.contains("Property_semantic_0.uniques = (Input_widget_iw17)")) {
			throw new Exception("");
		}
		// XMLUtil.save("upmmatch.xml",
		// Instance_GUI_patternWriter.writeInstanceGUIPattern(match));
	}
}
