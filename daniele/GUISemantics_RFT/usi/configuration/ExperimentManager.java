package usi.configuration;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.w3c.dom.Document;

import usi.gui.GUIWriter;
import usi.gui.structure.GUI;
import usi.util.DateUtility;
import usi.xml.XMLUtil;

public class ExperimentManager {

	private static final String SER_FILE_PATH = "." + File.separator + "lib" + File.separator
			+ "cobertura" + File.separator + "cobertura.ser";
	private static final String SER_RESET_FILE_PATH = "." + File.separator + "lib" + File.separator
			+ "cobertura" + File.separator + "cobertura_reset.ser";

	public static void init() throws Exception {

		// instrument the application for cobertura analysis
		final String args[] = {
				"--destination",
				(new File(ConfigurationManager.getAutBinDirectory()).getParent() + File.separator + "instrumented"),
				"--datafile",
				"lib" + File.separator + "cobertura" + File.separator + "cobertura.ser",
				ConfigurationManager.getAutBinDirectory(), };
		new File(SER_FILE_PATH).delete();
		final PrintStream stdout = System.out;
		System.setOut(new PrintStream(new FileOutputStream("cobertura_warnings.out")));
		net.sourceforge.cobertura.instrument.Main.main(args);
		System.setOut(stdout);
		Files.copy(Paths.get(SER_FILE_PATH), Paths.get(SER_RESET_FILE_PATH), REPLACE_EXISTING);

		// generate the file AUT.bat
		FileWriter autBatFile;
		try {
			autBatFile = new FileWriter("scripts" + File.separator + "AUT.bat");
			final BufferedWriter out = new BufferedWriter(autBatFile);
			out.write("call " + ConfigurationManager.getResetScriptPath());
			out.newLine();
			out.newLine();
			out.write("TITLE case_study");
			out.newLine();
			out.write("java -Xbootclasspath/p:" + System.getProperty("user.dir") + File.separator
					+ "lib" + File.separator + "abtJFileChooser.jar;" + " -cp %CLASSPATH%;"
					+ System.getProperty("user.dir") + ";" + System.getProperty("user.dir")
					+ File.separator + "lib" + File.separator + "cobertura" + File.separator
					+ "cobertura.jar;" + ConfigurationManager.getAutClasspath() + ";"
					+ new File(ConfigurationManager.getAutBinDirectory()).getParent() + ""
					+ File.separator + "instrumented" + ";"
					+ ConfigurationManager.getAutBinDirectory() + ";"
					+ System.getProperty("user.dir") + File.separator + "lib" + File.separator
					+ "jmockit.jar" + ";" + " -javaagent:" + System.getProperty("user.dir")
					+ File.separator + "lib" + File.separator + "jmockit.jar"
					+ " -Dnet.sourceforge.cobertura.datafile=\"" + System.getProperty("user.dir")
					+ File.separator + "lib" + File.separator + "cobertura" + File.separator
					+ "cobertura.ser\"" + " " + "usi.rmi.AUTMain" + " "
					+ ConfigurationManager.getAutMainCLass() + " 1> \""
					+ System.getProperty("user.dir") + File.separator + "output" + File.separator
					+ "autOut" + File.separator + "stdOUT.log\"" + " 2> \""
					+ System.getProperty("user.dir") + File.separator + "output" + File.separator
					+ "autOut" + File.separator + "stdERR.log\"");

			out.flush();
			out.close();
		} catch (final IOException e) {
			throw new Exception("ExperimentManager - init: problems creating AUT.bat, "
					+ e.getMessage());
		}
	}

	public static void dumpGUI(final GUI gui, final String path) throws Exception {

		if (gui == null) {
			throw new Exception("ExperimentManager - dumpGUI: null gui.");
		}
		final GUIWriter writer = new GUIWriter();
		final Document doc = writer.writeGUI(gui);

		String out_f;
		if (path == null) {
			out_f = "output" + File.separator + "ripping" + File.separator + "gui_"
					+ (DateUtility.now() + ".xml");
		} else {
			out_f = path;
		}
		XMLUtil.save(out_f, doc);
	}

	public static void dumpTCresult(final List<String> results, final String coverage)
			throws Exception {

		final String directory = "output" + File.separator + "testcases_result_"
				+ DateUtility.now();
		new File(directory).mkdir();
		int cont = 1;
		for (final String result : results) {
			final PrintWriter writer = new PrintWriter(directory + File.separator + "testcase"
					+ cont + "_result.txt", "UTF-8");
			writer.print(result);
			writer.close();
			cont++;
		}
		final PrintWriter writer = new PrintWriter(
				directory + File.separator + "coverage_info.txt", "UTF-8");
		writer.print(coverage);
		writer.close();
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
}
