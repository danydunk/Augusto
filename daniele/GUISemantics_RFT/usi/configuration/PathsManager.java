package usi.configuration;

import java.io.File;

public class PathsManager {

	private static final String GUIPATTERNS_FOLDER = "." + File.separator + "files"
			+ File.separator + "guipatterns" + File.separator;
	private static final String CONF_FOLDER = "." + File.separator + "config" + File.separator;
	private static final String ALLOY_MODULES_PATH = "." + File.separator + "files"
			+ File.separator + "alloy" + File.separator + "modules" + File.separator;
	private static final String ALLOY_MODELS_PATH = "." + File.separator + "files" + File.separator
			+ "alloy" + File.separator;
	private static final String INPUTDATA_FILE_PATH = "." + File.separator + "files"
			+ File.separator + "inputdata" + File.separator + "dataset.xml";
	private static final String OUTPUT_FOLDER = "." + File.separator + "output" + File.separator;
	private static final String RIPPER_OUTPUT_FOLDER = "." + File.separator + "output"
			+ File.separator + "ripping" + File.separator;

	public static String getOutputFolder() {

		return OUTPUT_FOLDER;
	}

	public static String getRipperOutputFolder() {

		return RIPPER_OUTPUT_FOLDER;
	}

	public static String getGUIPatternsFolder() {

		return GUIPATTERNS_FOLDER;
	}

	public static String getAlloyModulesFolder() {

		return ALLOY_MODULES_PATH;
	}

	public static String getAlloyModelsFolder() {

		return ALLOY_MODELS_PATH;
	}

	public static String getInputdataFilePath() {

		return INPUTDATA_FILE_PATH;
	}

	public static String getConfigurationFolder() {

		return CONF_FOLDER;
	}

}
