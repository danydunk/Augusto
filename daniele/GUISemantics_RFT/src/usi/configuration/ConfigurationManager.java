package usi.configuration;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigurationManager {

	private static String autClasspath;
	private static String autBinDirectory;
	private static String autMainCLass;

	private static long sleepTime;

	public void load() throws Exception {

		this.load("config" + System.getProperty("file.separator") + "aut.properties");
	}

	public void load(String configuration_file_path) throws Exception {

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configuration_file_path));
		} catch (Exception e) {
			throw new Exception("ConfigurationManager - init: error loading file, " + e.getMessage());
		}

		ConfigurationManager.setAutBinDirectory(properties.getProperty("aut_bin_directory"));
		ConfigurationManager.setAutMainCLass(properties.getProperty("aut_main_class"));
		ConfigurationManager.setAutClasspath(properties.getProperty("aut_classpath"));

		ConfigurationManager.setSleepTime(Long.valueOf(properties.getProperty("sleep_time")));
	}

	public static String getAutClasspath() {

		return autClasspath;
	}

	public static void setAutClasspath(String autClasspath) {

		ConfigurationManager.autClasspath = autClasspath;
	}

	public static String getAutBinDirectory() {

		return autBinDirectory;
	}

	public static void setAutBinDirectory(String autBinDirectory) {

		ConfigurationManager.autBinDirectory = autBinDirectory;
	}

	public static String getAutMainCLass() {

		return autMainCLass;
	}

	public static void setAutMainCLass(String autMainCLass) {

		ConfigurationManager.autMainCLass = autMainCLass;
	}

	public static long getSleepTime() {

		return sleepTime;
	}

	public static void setSleepTime(long sleepTime) {

		ConfigurationManager.sleepTime = sleepTime;
	}
}
