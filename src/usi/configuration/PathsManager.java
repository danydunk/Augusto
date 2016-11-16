package src.usi.configuration;

import java.io.File;

public class PathsManager {

	private static final String GUIPATTERNS_FOLDER = "/files/guipatterns/";
	private static final String CONF_FOLDER = System.getProperty("user.dir") + File.separator;
	private static final String ALLOY_MODULES_PATH = "/files/alloy/modules/";
	private static final String ALLOY_MODELS_PATH = "/files/alloy/";
	private static final String INPUTDATA_FILE_PATH = "/files/inputdata/dataset.xml";
	private static final String OUTPUT_FOLDER = System.getProperty("user.dir") + File.separator
			+ "output" + File.separator;
	private static final String RIPPER_OUTPUT_FOLDER = OUTPUT_FOLDER + File.separator + "ripping"
			+ File.separator;
	private static final String AUT_PATH = System.getProperty("user.dir") + File.separator;

	public static String getAUTPath() {

		return AUT_PATH;
	}

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
