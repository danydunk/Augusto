package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_rachota_testHelper;
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
public class Refinement_rachota_test extends Refinement_rachota_testHelper {

	/**
	 * Script Name : <b>Refinement_rachota_test</b> Generated : <b>Dec 8, 2016
	 * 11:30:08 AM</b> Description : Functional Test Script Original Host :
	 * WinNT Version 6.1 Build 7601 (S)
	 *
	 * @since 2016/12/08
	 * @author usi
	 */
	public void testMain(final Object[] args) throws Exception {

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/rachota.properties");
		ExperimentManager.init();

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot()
				+ "/files/guipatterns/crud_no_read.xml");
		final GUI_Pattern pattern = GUIPatternParser.parse(doc);

		// we load the GUI structure
		doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/for_test/xml/rachota.xml");
		final GUI gui = GUIParser.parse(doc);

		final GUIFunctionality_search gfs = new GUIFunctionality_search(gui);
		final List<Instance_GUI_pattern> res = gfs.match(pattern);

		Instance_GUI_pattern match = res.get(1);

		match.generateSpecificSemantics();
		final GUIFunctionality_refine refiner = new GUIFunctionality_refine(match, gui);
		match = refiner.refine();

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

		if (sem_prop.length() == 0 || !sem_prop.contains(".requireds = (Input_widget_iw25)")) {
			throw new Exception("");
		}
	}
}
