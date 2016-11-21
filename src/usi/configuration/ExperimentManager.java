package src.usi.configuration;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.w3c.dom.Document;

import src.usi.gui.GUIWriter;
import src.usi.gui.structure.GUI;
import src.usi.testcase.GUITestCaseResult;
import src.usi.testcase.GUITestCaseWriter;
import src.usi.testcase.OracleChecker;
import src.usi.util.DateUtility;
import src.usi.xml.XMLUtil;

public class ExperimentManager {

	private static final String SER_FILE_PATH = PathsManager.getProjectRoot() + File.separator
			+ "cobertura.ser";
	private static final String SER_RESET_FILE_PATH = PathsManager.getProjectRoot()
			+ File.separator + "cobertura_reset.ser";

	public static void init() throws Exception {

		// instrument the application for cobertura analysis
		final String args[] = {
				"--auxClasspath",
				ConfigurationManager.getAutClasspath(),
				"--destination",
				(new File(ConfigurationManager.getAutBinDirectory()).getParent() + File.separator + "instrumented"),
				"--datafile", SER_FILE_PATH, ConfigurationManager.getAutBinDirectory(), };

		new File(SER_FILE_PATH).delete();
		final PrintStream stdout = System.out;
		final PrintStream stream = new PrintStream(new FileOutputStream(
				System.getProperty("user.dir") + File.separator + "cobertura_warnings.out"));
		System.setOut(stream);
		net.sourceforge.cobertura.instrument.InstrumentMain.instrument(args);
		stream.close();
		System.setOut(stdout);
		Files.copy(Paths.get(SER_FILE_PATH), Paths.get(SER_RESET_FILE_PATH), REPLACE_EXISTING);

		// generate the file AUT.bat
		FileWriter autBatFile;
		try {
			autBatFile = new FileWriter(PathsManager.getAUTPath());
			final BufferedWriter out = new BufferedWriter(autBatFile);
			out.write("cd " + PathsManager.getProjectRoot());
			out.newLine();
			out.write("call " + ConfigurationManager.getResetScriptPath());
			out.newLine();
			out.newLine();
			out.write("TITLE case_study");
			out.newLine();
			out.write("java -Xbootclasspath/p:" + PathsManager.getProjectRoot() + "lib"
					+ File.separator + "abtJFileChooser.jar;" + " -cp "
					+ PathsManager.getProjectRoot() + "build" + File.separator + "classes"
					+ File.separator + "main;" + PathsManager.getProjectRoot() + "lib"
					+ File.separator + "*;"
					+ new File(ConfigurationManager.getAutBinDirectory()).getParent()
					+ File.separator + "instrumented" + ";"
					+ ConfigurationManager.getAutClasspath() + ";"
					+ ConfigurationManager.getAutBinDirectory() + ";" + " -javaagent:"
					+ PathsManager.getProjectRoot() + "lib" + File.separator + "jmockit-1.29.jar"
					+ " -Dnet.sourceforge.cobertura.datafile=\"" + SER_FILE_PATH + "\""
					+ " src.usi.application.AUTMain " + ConfigurationManager.getAutMainCLass());

			out.flush();
			out.close();
		} catch (final IOException e) {
			throw new Exception("ExperimentManager - init: problems creating AUT.bat, "
					+ e.getMessage());
		}
	}

	public static void dumpGUI(final GUI gui) throws Exception {

		if (gui == null) {
			throw new Exception("ExperimentManager - dumpGUI: null gui.");
		}
		final Document doc = GUIWriter.writeGUI(gui);

		final String out_f = PathsManager.getRipperOutputFolder() + File.separator + "gui_"
				+ (DateUtility.now() + ".xml");

		XMLUtil.save(out_f, doc);
	}

	public static void dumpTCresults(final String directory, final List<GUITestCaseResult> results,
			final GUI gui) throws Exception {

		int cont = 1;
		for (final GUITestCaseResult result : results) {
			XMLUtil.save(directory + File.separator + "testcase_" + cont + ".xml",
					GUITestCaseWriter.writeGUITestCase(result.getTc()));
			final OracleChecker checker = new OracleChecker(gui);
			checker.check(result, false);
			final PrintWriter writer = new PrintWriter(directory + File.separator + "testcase_"
					+ cont + "_result.txt", "UTF-8");
			writer.print(result.getTc().toString() + System.getProperty("line.separator")
					+ checker.getDescriptionOfLastOracleCheck());
			writer.close();
			cont++;
		}

	}

	public static void resetCoverage() throws IOException {

		Files.copy(Paths.get(SER_RESET_FILE_PATH), Paths.get(SER_FILE_PATH), REPLACE_EXISTING);
		// new File(SER_FILE_PATH).delete();
	}

	public static double[] getCoverage() {

		final ProjectData globalCobertura = CoverageDataFileHandler.loadCoverageData(new File(
				SER_FILE_PATH));
		final double[] out = new double[2];
		out[0] = globalCobertura.getLineCoverageRate();
		out[1] = globalCobertura.getBranchCoverageRate();
		return out;
	}

	public static String createResultsFolder() {

		if (!new File(PathsManager.getOutputFolder()).exists()) {
			createFolder(PathsManager.getOutputFolder());

		}
		final String directory = PathsManager.getOutputFolder() + "results_" + DateUtility.now()
				+ File.separator;
		createFolder(directory);
		return directory;
	}

	public static void createFolder(final String path) {

		new File(path).mkdir();
	}

	public static void moveFile(final String file, final String outfolder) throws IOException {

		final File afile = new File(file);
		final File dest = new File(outfolder + File.separator + afile.getName());
		Files.copy(afile.toPath(), dest.toPath());
	}

	public static void moveFile(final InputStream file, final String outfolder) throws IOException {

		final File dest = new File(outfolder);
		Files.copy(file, dest.toPath());
	}

	public static void cleanUP() {

		new File(PathsManager.getAUTPath()).delete();
		new File(SER_FILE_PATH).delete();
		new File(SER_RESET_FILE_PATH).delete();
		new File(System.getProperty("user.dir") + File.separator + "cobertura_warnings.out")
				.delete();
	}
}
