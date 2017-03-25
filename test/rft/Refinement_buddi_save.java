package test.rft;

import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_buddi_saveHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_refine;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_GUI_patternWriter;
import src.usi.gui.structure.GUI;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Refinement_buddi_save extends Refinement_buddi_saveHelper {

	/**
	 * Script Name : <b>Refinement_buddi_save</b> Generated : <b>Feb 20, 2017
	 * 1:18:23 AM</b> Description : Functional Test Script Original Host : WinNT
	 * Version 6.1 Build 7601 (S)
	 *
	 * @since 2017/02/20
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		ConfigurationManager.load(PathsManager.getProjectRoot()
				+ "/files/for_test/config/upm.properties");
		ExperimentManager.init();

		// we load a gui pattern
		Document doc = XMLUtil.read(PathsManager.getProjectRoot() + "/files/guipatterns/save.xml");
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
			final Document a = Instance_GUI_patternWriter.writeInstanceGUIPattern(match);
			XMLUtil.save("savematch.xml", a);

		} catch (final Exception e) {
			e.printStackTrace();
		}
		ExperimentManager.cleanUP();

		if (match == null) {
			throw new Exception("");
		}
	}
}
