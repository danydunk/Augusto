package usi.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExperimentManager {

	public static void init() throws Exception {

		// instrument the application for cobertura analysis
		final String args[] = {
				"--destination",
				(new File(ConfigurationManager.getAutBinDirectory()).getParent()
						+ System.getProperty("file.separator") + "instrumented"),
						"--datafile",
						"lib" + System.getProperty("file.separator") + "cobertura"
								+ System.getProperty("file.separator") + "cobertura.ser",
								ConfigurationManager.getAutBinDirectory(), };
		net.sourceforge.cobertura.instrument.Main.main(args);

		// generate the file AUT.bat
		FileWriter autBatFile;
		try {
			autBatFile = new FileWriter("scripts" + System.getProperty("file.separator")
					+ "AUT.bat");
			final BufferedWriter out = new BufferedWriter(autBatFile);
			out.write("call scripts" + System.getProperty("file.separator") + "AUTreset.bat");
			out.newLine();
			out.newLine();
			out.write("TITLE case_study");
			out.newLine();
			out.write("java -Xbootclasspath/p:" + System.getProperty("user.dir")
					+ System.getProperty("file.separator") + "lib"
					+ System.getProperty("file.separator") + "abtJFileChooser.jar;"
					+ " -cp %CLASSPATH%;" + System.getProperty("user.dir") + File.separator
					+ "bin;" + System.getProperty("user.dir") + File.separator + "lib"
					+ System.getProperty("file.separator") + "cobertura"
					+ System.getProperty("file.separator") + "cobertura.jar;"
					+ ConfigurationManager.getAutClasspath() + ";"
					+ new File(ConfigurationManager.getAutBinDirectory()).getParent() + ""
					+ System.getProperty("file.separator") + "instrumented" + ";"
					+ ConfigurationManager.getAutBinDirectory() + ";"
					+ System.getProperty("user.dir") + File.separator + "lib"
					+ System.getProperty("file.separator") + "jmockit"
					+ System.getProperty("file.separator") + "jmockit.jar" + ";" + " -javaagent:"
					+ System.getProperty("user.dir") + File.separator + "lib"
					+ System.getProperty("file.separator") + "jmockit"
					+ System.getProperty("file.separator") + "jmockit.jar"
					+ " -Dnet.sourceforge.cobertura.datafile=\"" + System.getProperty("user.dir")
					+ File.separator + "lib" + System.getProperty("file.separator") + "cobertura"
					+ System.getProperty("file.separator") + "cobertura.ser\"" + " "
					+ "usi.rmi.AUTMain" + " " + ConfigurationManager.getAutMainCLass() + " 1> \""
					+ System.getProperty("user.dir") + File.separator + "output"
					+ System.getProperty("file.separator") + "stdOUT.log\"" + " 2> \""
					+ System.getProperty("user.dir") + File.separator + "output"
					+ System.getProperty("file.separator") + "stdERR.log\"");

			out.flush();
			out.close();
		} catch (final IOException e) {
			throw new Exception("ExperimentManager - init: problems creating AUT.bat, "
					+ e.getMessage());
		}
	}
}
