package src.usi.configuration;

import java.io.FileInputStream;
import java.util.Properties;

import src.usi.application.ApplicationHelper;

public class ConfigurationManager {

	private static String loaded_file = null;
	// AUT setting
	private static String autClasspath;
	private static String autBinDirectory;
	private static String autMainCLass;
	private static String resetScriptPath;
	private static long sleepTime = 600;
	private static String initial_actions = "";
	private static int alloyRunScope = 4;
	private static String ripperFilters = "";
	// path to the GUI file
	private static String guiFile;
	// refinement settings
	private static long refinementTimeout = 1800000;// 30min
	private static int alloyRefinementTimeScope = 10;
	// testcase settings
	private static int testcaseLength = 8;
	private static boolean pairwiseTestcase = false;
	// for multithreading
	private static int multithreading_batch_size = 8;

	public static void load() throws Exception {

		load(PathsManager.getConfigurationFolder() + "aut.properties");
	}

	public static void load(final String configuration_file_path) throws Exception {

		loaded_file = configuration_file_path;
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configuration_file_path));
		} catch (final Exception e) {
			e.printStackTrace();
			throw new Exception("ConfigurationManager - init: error loading file, "
					+ e.getMessage());
		}
		ConfigurationManager.setPairwiseTestcase(Boolean.valueOf(properties
				.getProperty("pairwise_testcase")));
		ConfigurationManager.setAutBinDirectory(properties.getProperty("aut_bin_directory"));
		ConfigurationManager.setAutMainCLass(properties.getProperty("aut_main_class"));
		ConfigurationManager.setAutClasspath(properties.getProperty("aut_classpath"));
		ConfigurationManager.setResetScriptPath(properties.getProperty("reset_script_path"));
		ConfigurationManager.setAlloyRunScope(Integer.valueOf(properties
				.getProperty("alloy_run_scope")));
		ConfigurationManager.setSleepTime(Long.valueOf(properties.getProperty("sleep_time")));
		setRefinementTimeout(Long.valueOf(properties.getProperty("refinement_timeout")));
		ConfigurationManager.setTestcaseLength(Integer.valueOf(properties
				.getProperty("testcase_length")));
		ConfigurationManager.setGUIFile(properties.getProperty("gui_file"));
		ConfigurationManager.setRefinementAlloyTimeScope(Integer.valueOf(properties
				.getProperty("refinment_alloy_time_scope")));
		ConfigurationManager.setMultithreadingBatchSize(Integer.valueOf(properties
				.getProperty("multithreading_batch_size")));
		try {
			ConfigurationManager.setInitialActions(properties.getProperty("initial_actions"));
		} catch (final Exception e) {
			ConfigurationManager.setInitialActions("");
		}
		try {
			ConfigurationManager.setRipperFilters(properties.getProperty("ripper_filters"));
		} catch (final Exception e) {
			ConfigurationManager.setRipperFilters("");
		}
		ApplicationHelper.reset();
	}

	public static String getLoadedFilePath() {

		return loaded_file;
	}

	public static int getMultithreadingBatchSize() {

		return multithreading_batch_size;
	}

	private static void setMultithreadingBatchSize(final int in) {

		multithreading_batch_size = in;
	}

	public static String getRipperFilters() {

		return ripperFilters;
	}

	private static void setRipperFilters(final String f) {

		if (f == null) {
			ConfigurationManager.ripperFilters = "";

		} else {
			ConfigurationManager.ripperFilters = f;
		}
	}

	public static String getAutClasspath() {

		return autClasspath;
	}

	private static void setAutClasspath(final String autClasspath) {

		ConfigurationManager.autClasspath = autClasspath;
	}

	public static String getAutBinDirectory() {

		return autBinDirectory;
	}

	private static void setAutBinDirectory(final String autBinDirectory) {

		ConfigurationManager.autBinDirectory = autBinDirectory;
	}

	public static String getAutMainCLass() {

		return autMainCLass;
	}

	private static void setAutMainCLass(final String autMainCLass) {

		ConfigurationManager.autMainCLass = autMainCLass;
	}

	private static void setResetScriptPath(final String resetScriptPath) {

		ConfigurationManager.resetScriptPath = resetScriptPath;
	}

	public static long getSleepTime() {

		return sleepTime;
	}

	private static void setSleepTime(final long sleepTime) {

		ConfigurationManager.sleepTime = sleepTime;
	}

	public static int getAlloyRunScope() {

		return ConfigurationManager.alloyRunScope;
	}

	private static void setAlloyRunScope(final int alloyRunScope) {

		ConfigurationManager.alloyRunScope = alloyRunScope;
	}

	public static int getRefinementAlloyTimeScope() {

		return ConfigurationManager.alloyRefinementTimeScope;
	}

	private static void setRefinementAlloyTimeScope(final int alloyRefinementTimeScope) {

		ConfigurationManager.alloyRefinementTimeScope = alloyRefinementTimeScope;
	}

	public static String getResetScriptPath() {

		return ConfigurationManager.resetScriptPath;
	}

	public static long getRefinementTimeout() {

		return ConfigurationManager.refinementTimeout;
	}

	private static void setRefinementTimeout(final long semanticRefinementTimeout) {

		ConfigurationManager.refinementTimeout = semanticRefinementTimeout;
	}

	public static int getTestcaseLength() {

		return testcaseLength;
	}

	private static void setTestcaseLength(final int testcaseLength) {

		ConfigurationManager.testcaseLength = testcaseLength;
	}

	private static void setPairwiseTestcase(final boolean pairwiseTestcase) {

		ConfigurationManager.pairwiseTestcase = pairwiseTestcase;
	}

	public static boolean getPairwiseTestcase() {

		return pairwiseTestcase;
	}

	private static void setGUIFile(final String guiFile) {

		ConfigurationManager.guiFile = guiFile;
	}

	public static String getGUIFile() {

		return guiFile;
	}

	private static void setInitialActions(final String s) {

		if (s == null) {
			ConfigurationManager.initial_actions = "";
		} else {
			ConfigurationManager.initial_actions = s;
		}
	}

	public static String getInitialActions() {

		return ConfigurationManager.initial_actions;
	}
}
