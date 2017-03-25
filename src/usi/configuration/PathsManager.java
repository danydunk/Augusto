package src.usi.configuration;

import java.io.File;

public class PathsManager {

	private static String PROJECT_ROOT = System.getProperty("user.dir") + File.separator;
	private static String BINS_PATH = System.getProperty("user.dir") + File.separator;
	private static final String GUIPATTERNS_FOLDER = "files" + File.separator + "guipatterns"
			+ File.separator + "";
	private static final String ALLOY_MODULES_PATH = "files" + File.separator + "alloy"
			+ File.separator + "modules" + File.separator + "";
	private static final String ALLOY_MODELS_PATH = "files" + File.separator + "alloy"
			+ File.separator + "";
	private static final String INPUTDATA_FILE_PATH = "files" + File.separator + "inputdata"
			+ File.separator + "dataset.xml";
	private static final String AUT_PATH = "AUT.bat";

	private static final String CONF_FOLDER = System.getProperty("user.dir") + File.separator;
	private static final String OUTPUT_FOLDER = System.getProperty("user.dir") + File.separator;
	private static final String RIPPER_OUTPUT_FOLDER = System.getProperty("user.dir")
			+ File.separator;

	public static void setProjectRoot(final String root) throws Exception {

		if (!new File(root).isDirectory()) {
			throw new Exception("PathsManager - wrong project root.");
		}
		PROJECT_ROOT = root + File.separator;
		BINS_PATH = PROJECT_ROOT + "build" + File.separator + "classes" + File.separator + "main";
	}

	public static String getProjectRoot() {

		return PROJECT_ROOT;
	}

	public static String getBINSPath() {

		return BINS_PATH;
	}

	public static String getAUTPath() {

		return PROJECT_ROOT + AUT_PATH;
	}

	public static String getGUIPatternsFolder() {

		return PROJECT_ROOT + GUIPATTERNS_FOLDER;
	}

	public static String getAlloyModulesFolder() {

		return PROJECT_ROOT + ALLOY_MODULES_PATH;
	}

	public static String getAlloyModelsFolder() {

		return PROJECT_ROOT + ALLOY_MODELS_PATH;
	}

	public static String getInputdataFilePath() {

		return PROJECT_ROOT + INPUTDATA_FILE_PATH;
	}

	public static String getConfigurationFolder() {

		return CONF_FOLDER;
	}

	public static String getOutputFolder() {

		return OUTPUT_FOLDER;
	}

	public static String getRipperOutputFolder() {

		return RIPPER_OUTPUT_FOLDER;
	}
}
