package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_buddi_crud_invalidHelper;
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
public class Refinement_buddi_crud_invalid extends Refinement_buddi_crud_invalidHelper {

	/**
	 * Script Name : <b>Refinement_buddi_crud_invalid</b> Generated : <b>Nov 28,
	 * 2016 1:51:46 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/11/28
	 * @author usi
	 */
	public void testMain(final Object[] args) throws Exception {

		if (args.length == 1) {
			PathsManager.setProjectRoot(args[0].toString());
		}
		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/buddi.properties");
		ExperimentManager.init();

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/crud.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/buddi.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);

		Instance_GUI_pattern match = res.get(1);

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

		if (sem_prop == null) {
			throw new Exception("");
		}
	}
}
