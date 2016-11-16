package test.rft;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.w3c.dom.Document;

import resources.test.rft.Refinement_upmfull_crudHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_refine;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.structure.GUI;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.structure.GUI_Pattern;
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
	 */
	public void testMain(final Object[] args) {

		try {
			Files.copy(Refinement_upmfull_crud.class
					.getResourceAsStream("/files/for_test/config/upm.properties"), Paths.get(System
					.getProperty("user.dir") + File.separator + "conf.properties"),
					REPLACE_EXISTING);

			ConfigurationManager.load(System.getProperty("user.dir") + File.separator
					+ "conf.properties");
			ExperimentManager.init();
			Files.delete(Paths.get(System.getProperty("user.dir") + File.separator
					+ "conf.properties"));

			// we load a gui pattern
			Document doc = XMLUtil.read(Refinement_upmfull_crud.class
					.getResourceAsStream("/files/guipatterns/crud.xml"));
			final GUI_Pattern pattern = GUIPatternParser.parse(doc);

			// we load the GUI structure
			doc = XMLUtil.read(Refinement_upmfull_crud.class
					.getResourceAsStream("/files/for_test/xml/upm-full_newripper.xml"));
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
