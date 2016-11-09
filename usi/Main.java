package usi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import resources.usi.MainHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.configuration.PathsManager;
import usi.gui.GUIParser;
import usi.gui.functionality.GUIFunctionality_refine;
import usi.gui.functionality.GUIFunctionality_search;
import usi.gui.functionality.GUIFunctionality_validate;
import usi.gui.functionality.instance.Instance_GUI_pattern;
import usi.gui.functionality.instance.Instance_GUI_patternWriter;
import usi.gui.structure.GUI;
import usi.pattern.GUIPatternParser;
import usi.pattern.structure.GUI_Pattern;
import usi.testcase.GUITestCaseResult;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Main extends MainHelper {

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
		final String out_folder = ExperimentManager.createResultsFolder();
		// we set the stdout as a log file
		final PrintStream generallog = new PrintStream(new FileOutputStream(out_folder + "out.log"));
		System.setOut(generallog);

		ExperimentManager.moveFile(ConfigurationManager.getLoadedFilePath(), out_folder);
		// GUI
		final GUI gui = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));
		ExperimentManager.moveFile(ConfigurationManager.getGUIFile(), out_folder);
		// guipatterns
		final List<File> files = Files.walk(Paths.get(PathsManager.getGUIPatternsFolder()))
				.filter(e -> Files.isRegularFile(e) && e.endsWith(".xml")).map(Path::toFile)
				.collect(Collectors.toList());
		final GUI_Pattern[] patterns = new GUI_Pattern[files.size()];
		final String[] patterns_name = new String[files.size()];
		for (int x = 0; x < files.size(); x++) {
			patterns[x] = GUIPatternParser.parse(XMLUtil.read(PathsManager.getGUIPatternsFolder()
					+ files.get(x).getName()));
			patterns_name[x] = files.get(x).getName().replace(".xml", "");
		}
		for (final File file : files) {
			ExperimentManager.moveFile(file.toPath().toString(), out_folder);
		}

		// search
		final GUIFunctionality_search searcher = new GUIFunctionality_search(gui);
		final Map<GUI_Pattern, List<Instance_GUI_pattern>> candidate_instances = new HashMap<>();
		for (int x = 0; x < patterns.length; x++) {
			final GUI_Pattern pattern = patterns[x];
			final List<Instance_GUI_pattern> new_instances = searcher.match(pattern);
			candidate_instances.put(pattern, new_instances);
			System.out.println("FOUND " + new_instances.size() + " CANDIDATE MATCHES FOR "
					+ patterns_name[x]);
		}
		// refine
		final Map<GUI_Pattern, List<Instance_GUI_pattern>> true_instances = new HashMap<>();
		for (int x = 0; x < patterns.length; x++) {
			final GUI_Pattern pattern = patterns[x];
			final List<Instance_GUI_pattern> refined_instances = new ArrayList<>();
			true_instances.put(pattern, refined_instances);
			for (int y = 0; y < candidate_instances.get(pattern).size(); y++) {
				final String match_folder = out_folder + File.separator + patterns_name[x]
						+ "_match_" + y;
				ExperimentManager.createFolder(match_folder);
				// we set the stdout as a log file
				final PrintStream reflog = new PrintStream(new FileOutputStream(match_folder
						+ "refinement.log"));
				System.setOut(reflog);
				final Instance_GUI_pattern instance = candidate_instances.get(pattern).get(y);
				final GUIFunctionality_refine refiner = new GUIFunctionality_refine(instance, gui);
				final Instance_GUI_pattern refined_instance = refiner.refine();
				if (refined_instance != null) {
					refined_instances.add(refined_instance);
					XMLUtil.save(match_folder + File.separator + "match.xml",
							Instance_GUI_patternWriter.writeInstanceGUIPattern(instance));
				}
				reflog.close();
			}
			System.out.println(patterns_name[x] + ": FOUND " + refined_instances.size()
					+ " true instances.");
		}
		System.setOut(generallog);
		// refinement coverage
		final double[] cov_before = ExperimentManager.getCoverage();
		System.out.println("COVERAGE ACHIEVED DURING REFINEMENT:" + System.lineSeparator()
				+ "statement " + cov_before[0] + ", branch " + cov_before[1]);
		// reset coverage
		ExperimentManager.resetCoverage();
		// validate
		for (int x = 0; x < patterns.length; x++) {
			final GUI_Pattern pattern = patterns[x];
			for (int y = 0; y < true_instances.get(pattern).size(); y++) {
				final String match_folder = out_folder + File.separator + patterns_name[x]
						+ "_match_" + y + File.separator + "testcases" + File.separator;
				ExperimentManager.createFolder(match_folder);
				final PrintStream vallog = new PrintStream(new FileOutputStream(match_folder
						+ "validator.log"));
				System.setOut(vallog);
				final Instance_GUI_pattern instance = candidate_instances.get(pattern).get(y);

				final GUIFunctionality_validate validator = new GUIFunctionality_validate(instance,
						gui);
				final List<GUITestCaseResult> results = validator.validate();
				ExperimentManager.dumpTCresults(match_folder, results, gui);
				vallog.close();
			}
		}
		System.setOut(generallog);
		// validation coverage
		final double[] cov_after = ExperimentManager.getCoverage();
		System.out.println("COVERAGE ACHIEVED DURING VALIDATION:" + System.lineSeparator()
				+ "statement " + cov_after[0] + ", branch " + cov_after[1]);
		generallog.close();
	}
}
