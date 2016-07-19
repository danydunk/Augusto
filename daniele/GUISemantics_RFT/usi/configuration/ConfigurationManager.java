package usi.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ConfigurationManager {

	private static String autClasspath;
	private static String autBinDirectory;
	private static String autMainCLass;
	private static String resetScriptPath;
	private static int alloyRunScope;
	private static long sleepTime;

	public static void load() throws Exception {

		load("config" + File.separator + "aut.properties");
	}

	public static void load(final String configuration_file_path) throws Exception {

		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configuration_file_path));
		} catch (final Exception e) {
			throw new Exception("ConfigurationManager - init: error loading file, "
					+ e.getMessage());
		}

		ConfigurationManager.setAutBinDirectory(properties.getProperty("aut_bin_directory"));
		ConfigurationManager.setAutMainCLass(properties.getProperty("aut_main_class"));
		ConfigurationManager.setAutClasspath(properties.getProperty("aut_classpath"));
		ConfigurationManager.setResetScriptPath(properties.getProperty("reset_script_path"));
		ConfigurationManager.setAlloyRunScope(Integer.valueOf(properties
				.getProperty("alloy_run_scope")));
		ConfigurationManager.setSleepTime(Long.valueOf(properties.getProperty("sleep_time")));
	}

	public static String getAutClasspath() {

		return autClasspath;
	}

	public static void setAutClasspath(final String autClasspath) {

		ConfigurationManager.autClasspath = autClasspath;
	}

	public static String getAutBinDirectory() {

		return autBinDirectory;
	}

	public static void setAutBinDirectory(final String autBinDirectory) {

		ConfigurationManager.autBinDirectory = autBinDirectory;
	}

	public static String getAutMainCLass() {

		return autMainCLass;
	}

	public static void setAutMainCLass(final String autMainCLass) {

		ConfigurationManager.autMainCLass = autMainCLass;
	}

	public static void setResetScriptPath(final String resetScriptPath) {

		ConfigurationManager.resetScriptPath = resetScriptPath;
	}

	public static long getSleepTime() {

		return sleepTime;
	}

	public static void setSleepTime(final long sleepTime) {

		ConfigurationManager.sleepTime = sleepTime;
	}

	public static int getAlloyRunScope() {

		return ConfigurationManager.alloyRunScope;
	}

	public static void setAlloyRunScope(final int alloyRunScope) {

		ConfigurationManager.alloyRunScope = alloyRunScope;
	}

	public static String getResetScriptPath() {

		return ConfigurationManager.resetScriptPath;
	}
}
