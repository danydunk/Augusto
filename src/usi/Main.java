package src.usi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resources.src.usi.MainHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.functionality.GUIFunctionality_refine;
import src.usi.gui.functionality.GUIFunctionality_search;
import src.usi.gui.functionality.GUIFunctionality_validate;
import src.usi.gui.functionality.instance.Instance_GUI_pattern;
import src.usi.gui.functionality.instance.Instance_GUI_patternWriter;
import src.usi.gui.functionality.instance.Instance_window;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Input_widget;
import src.usi.gui.structure.Selectable_widget;
import src.usi.gui.structure.Window;
import src.usi.pattern.GUIPatternParser;
import src.usi.pattern.Patterns;
import src.usi.pattern.structure.GUI_Pattern;
import src.usi.pattern.structure.Pattern_action_widget;
import src.usi.pattern.structure.Pattern_input_widget;
import src.usi.pattern.structure.Pattern_selectable_widget;
import src.usi.testcase.GUITestCaseResult;
import src.usi.xml.XMLUtil;

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

		final long beginTime = System.currentTimeMillis();
		switch (args.length) {
		case 1:
			PathsManager.setProjectRoot(args[0].toString());
			break;

		default:
			System.out.println("Error: wrong number of parameters.");
			return;
		}
		ConfigurationManager.load();
		ExperimentManager.init();
		final String out_folder = ExperimentManager.createResultsFolder();
		// we set the stdout as a log file
		final PrintStream generallog = new PrintStream(new FileOutputStream(out_folder + "out.log"));
		final PrintStream generalerr = new PrintStream(new FileOutputStream(out_folder + "out.err"));
		try {
			System.setOut(generallog);
			System.setErr(generalerr);

			ExperimentManager.moveFile(ConfigurationManager.getLoadedFilePath(), out_folder);
			// GUI
			final GUI gui = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));
			ExperimentManager.moveFile(ConfigurationManager.getGUIFile(), out_folder);
			// guipatterns
			final GUI_Pattern[] patterns = new GUI_Pattern[Patterns.values().length];
			final String[] patterns_name = new String[Patterns.values().length];
			for (int x = 0; x < Patterns.values().length; x++) {
				patterns[x] = GUIPatternParser.parse(XMLUtil.read(PathsManager
						.getGUIPatternsFolder() + Patterns.values()[x].name));
				patterns_name[x] = Patterns.values()[x].name.replace(".xml", "");
			}
			// final Map<GUI_Pattern, List<Instance_GUI_pattern>>
			// candidate_instances = new HashMap<>();

			final GUI gui_app = GUIParser.parse(XMLUtil.read(ConfigurationManager.getGUIFile()));
			final Map<GUI_Pattern, List<Instance_GUI_pattern>> true_instances = new HashMap<>();

			for (int x = 0; x < patterns.length; x++) {
				final GUI_Pattern pattern = patterns[x];

				// search
				final GUIFunctionality_search searcher = new GUIFunctionality_search(gui_app);
				final List<Instance_GUI_pattern> new_instances = searcher.match(pattern);
				// candidate_instances.put(pattern, new_instances);
				System.out.println("FOUND " + new_instances.size() + " CANDIDATE MATCHES FOR "
						+ patterns_name[x]);

				// refine
				final List<Instance_GUI_pattern> refined_instances = new ArrayList<>();
				true_instances.put(pattern, refined_instances);
				for (int y = 0; y < new_instances.size(); y++) {

					final String match_folder = out_folder + File.separator + patterns_name[x]
							+ "_match_" + (y + 1) + File.separator;
					ExperimentManager.createFolder(match_folder);
					// we set the stdout as a log file
					final PrintStream reflog = new PrintStream(new FileOutputStream(match_folder
							+ "refinement.log"));
					System.setOut(reflog);

					final Instance_GUI_pattern inst = new_instances.get(y);

					// we need to rebuild the instance
					final GUI newgui = new GUI();
					final Instance_GUI_pattern instance = new Instance_GUI_pattern(newgui,
							inst.getGuipattern());
					for (final Instance_window iiww : inst.getWindows()) {
						final Instance_window newiw = new Instance_window(iiww.getPattern(),
								gui.getWindow(iiww.getInstance().getId()));

						instance.addWindow(newiw);

						if (newgui.containsWindow(iiww.getInstance().getId())) {
							newgui.addWindow(gui.getWindow(iiww.getInstance().getId()));
						}
						for (final Pattern_action_widget paw : iiww.getPattern().getActionWidgets()) {
							final List<Action_widget> mapping = new ArrayList<>();
							for (final Action_widget aw : iiww.getAWS_for_PAW(paw.getId())) {
								mapping.add((Action_widget) gui.getWindow(
										iiww.getInstance().getId()).getWidget(aw.getId()));
							}
							newiw.addAW_mapping(paw, mapping);
						}
						for (final Pattern_input_widget piw : iiww.getPattern().getInputWidgets()) {
							final List<Input_widget> mapping = new ArrayList<>();
							for (final Input_widget iw : iiww.getIWS_for_PIW(piw.getId())) {
								mapping.add((Input_widget) gui
										.getWindow(iiww.getInstance().getId())
										.getWidget(iw.getId()));
							}
							newiw.addIW_mapping(piw, mapping);
						}
						for (final Pattern_selectable_widget psw : iiww.getPattern()
								.getSelectableWidgets()) {
							final List<Selectable_widget> mapping = new ArrayList<>();
							for (final Selectable_widget sw : iiww.getSWS_for_PSW(psw.getId())) {
								mapping.add((Selectable_widget) gui.getWindow(
										iiww.getInstance().getId()).getWidget(sw.getId()));
							}
							newiw.addSW_mapping(psw, mapping);
						}
					}
					for (final Action_widget aw : inst.getGui().getAction_widgets()) {
						for (final Window w : inst.getGui().getStaticForwardLinks(aw.getId())) {
							newgui.addStaticEdge(aw.getId(), w.getId());
						}
					}

					final GUIFunctionality_refine refiner = new GUIFunctionality_refine(instance,
							gui);
					final Instance_GUI_pattern refined_instance = refiner.refine();
					refined_instances.add(refined_instance);

					if (refined_instance != null) {
						XMLUtil.save(match_folder + File.separator + "match.xml",
								Instance_GUI_patternWriter
										.writeInstanceGUIPattern(refined_instance));
						// we remove the instance elements from the gui so that
						// they
						// cannot be added to a match of another pattern
						for (final Instance_window iw : refined_instance.getWindows()) {
							final Window w = gui_app.getWindow(iw.getInstance().getId());
							if (w == null) {
								continue;
							}
							for (final Pattern_action_widget paw : iw.getPattern()
									.getActionWidgets()) {
								for (final Action_widget aw : iw.getAWS_for_PAW(paw.getId())) {
									for (final Window tw : gui_app.getDynamicForwardLinks(aw
											.getId())) {
										gui_app.removeDynamicEdge(aw.getId(), tw.getId());
									}
									for (final Window tw : gui_app
											.getStaticForwardLinks(aw.getId())) {
										gui_app.removeStaticEdge(aw.getId(), tw.getId());
									}
									w.removeWidget(aw.getId());
								}
							}
							for (final Pattern_input_widget piw : iw.getPattern().getInputWidgets()) {
								for (final Input_widget iiw : iw.getIWS_for_PIW(piw.getId())) {
									w.removeWidget(iiw.getId());
								}
							}
							for (final Pattern_selectable_widget psw : iw.getPattern()
									.getSelectableWidgets()) {
								for (final Selectable_widget sw : iw.getSWS_for_PSW(psw.getId())) {
									w.removeWidget(sw.getId());
								}
							}
						}

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
					// if (y != 4) {
					// continue;
					// }
					if (true_instances.get(pattern).get(y) != null) {
						final String match_folder = out_folder + File.separator + patterns_name[x]
								+ "_match_" + (y + 1) + File.separator + "testcases"
								+ File.separator;
						ExperimentManager.createFolder(match_folder);
						final PrintStream vallog = new PrintStream(new FileOutputStream(
								match_folder + "validator.log"));
						System.setOut(vallog);
						final Instance_GUI_pattern instance = true_instances.get(pattern).get(y);

						final GUIFunctionality_validate validator = new GUIFunctionality_validate(
								instance, gui);
						final List<GUITestCaseResult> results = validator.validate();
						ExperimentManager.dumpTCresults(match_folder, results, gui);
						vallog.close();
					}
				}
			}
			System.setOut(generallog);
			// validation coverage
			final double[] cov_after = ExperimentManager.getCoverage();
			System.out.println("COVERAGE ACHIEVED DURING VALIDATION:" + System.lineSeparator()
					+ "statement " + cov_after[0] + ", branch " + cov_after[1]);
			final long tottime = (System.currentTimeMillis() - beginTime) / 1000;
			System.out.println("MAIN ELAPSED TIME: " + tottime);
			generallog.close();
			ExperimentManager.moveFile(System.getProperty("user.dir") + File.separator + "aut.log",
					out_folder);
			ExperimentManager.cleanUP();

		} catch (final Exception e) {
			System.setOut(generallog);
			System.out.println("ERROR");
			e.printStackTrace();
		} finally {
			ExperimentManager.cleanUP();
		}
	}
}
