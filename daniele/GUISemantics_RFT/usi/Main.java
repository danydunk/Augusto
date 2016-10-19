package usi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resources.usi.MainHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
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
public class Main extends MainHelper {

	private final String[] PATTERNS = { "crud" };
	private final String PATTERNS_DIR = "./files/xml/";

	public static void main(final Object[] args) throws Exception {

		final Main r = new Main();
		r.testMain(args);
	}

	/**
	 * Script Name : <b>Main</b> Generated : <b>Jul 7, 2016 7:55:06 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/07/07
	 * @author usi
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		switch (args.length) {
		case 0:
			ConfigurationManager.load();
			break;

		case 2:
			if (args[0].toString().equals("--conf")) {
				ConfigurationManager.load(args[1].toString());
			} else {
				System.out.println("Error: unknown input parameter.");
				return;
			}
			break;

		default:
			System.out.println("Error: wrong number of parameters.");
			return;
		}
		ExperimentManager.init();
		final GUI_Pattern[] patterns = new GUI_Pattern[this.PATTERNS.length];
		for (int x = 0; x < this.PATTERNS.length; x++) {
			final String p = this.PATTERNS[x];
			XMLUtil.read(this.PATTERNS_DIR + p + ".xml");
			patterns[x] = GUIPatternParser.parse(XMLUtil.read(this.PATTERNS_DIR + p + ".xml"));
		}

		final GUI gui = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));
		final GUIFunctionality_search searcher = new GUIFunctionality_search(gui);
		final Map<GUI_Pattern, List<Instance_GUI_pattern>> candidate_instances = new HashMap<>();
		for (int x = 0; x < patterns.length; x++) {
			final GUI_Pattern pattern = patterns[x];
			final List<Instance_GUI_pattern> new_instances = searcher.match(pattern);
			System.out.println(this.PATTERNS[x] + " functionality: found " + new_instances.size()
					+ " candidate instances.");
			candidate_instances.put(pattern, new_instances);
		}

		final Map<GUI_Pattern, List<Instance_GUI_pattern>> true_instances = new HashMap<>();
		for (int x = 0; x < patterns.length; x++) {
			final GUI_Pattern pattern = patterns[x];

			final List<Instance_GUI_pattern> refined_instances = new ArrayList<>();
			true_instances.put(pattern, refined_instances);
			for (final Instance_GUI_pattern instance : candidate_instances.get(pattern)) {
				final GUIFunctionality_refine refiner = new GUIFunctionality_refine(instance, gui);
				final Instance_GUI_pattern refined_instance = refiner.refine();
				if (refined_instance != null) {
					refined_instances.add(refined_instance);
				}
			}
			System.out.println(this.PATTERNS[x] + " functionality: found "
					+ refined_instances.size() + " true instances.");
		}
	}
}
