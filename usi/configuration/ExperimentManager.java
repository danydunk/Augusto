package usi.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;

import usi.gui.GUIWriter;
import usi.gui.structure.GUI;
import usi.util.DateUtility;
import usi.xml.XMLUtil;

public class ExperimentManager {

	public static void init() throws Exception {

		// instrument the application for cobertura analysis
		final String args[] = {
				"--destination",
				(new File(ConfigurationManager.getAutBinDirectory()).getParent() + File.separator + "instrumented"),
				"--datafile",
				"lib" + File.separator + "cobertura" + File.separator + "cobertura.ser",
				ConfigurationManager.getAutBinDirectory(), };
		// net.sourceforge.cobertura.instrument.Main.main(args);

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
					+ "jmockit" + File.separator + "jmockit.jar" + ";" + " -javaagent:"
					+ System.getProperty("user.dir") + File.separator + "lib" + File.separator
					+ "jmockit" + File.separator + "jmockit.jar"
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
}
