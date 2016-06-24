package usi.guisemantic.testcase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.guisemantic.testcase.interfaces.IGUITestCaseRunner;
import usi.guistructure.Widget;
import usi.guistructure.Window;
import usi.xml.XMLUtil;

public class GUIRippingTestCaseRunner implements IGUITestCaseRunner {

	final private String REPLAYER_PATH = "." + File.separator + "resources" + File.separator + "tools" + File.separator
			+ "guiripping" + File.separator + "jfc-replayer.sh";
	final private String OUTPUT_FOLDER_PATH;
	final private String TESTCASES_FOLDER_PATH;
	final private String STATES_FOLDER_PATH;
	final private String LOG_FOLDER_PATH;
	final private String RESULTS_FOLDER_PATH;

	final private int INITIAL_WAIT = 1000;
	final private int STEP_DELAY = 400;
	final private int STEP_TIMEOUT = 1000;

	// final private GUI gui;
	final private String aut_classpath;
	final private String aut_mainclass;
	final private String efg_path;
	final private String gui_path;

	public GUIRippingTestCaseRunner(final String out_dir, final String aut_classpath, final String auto_mainclass,
			final String efg_path, final String gui_path) throws Exception {

		if (out_dir == null || out_dir.length() == 0) {
			throw new Exception("GUIRippingTestCaseRunner: null out dir path.");
		}
		this.OUTPUT_FOLDER_PATH = out_dir;
		this.TESTCASES_FOLDER_PATH = this.OUTPUT_FOLDER_PATH + File.separator + "testcases";
		this.STATES_FOLDER_PATH = this.TESTCASES_FOLDER_PATH + File.separator + "states";
		this.LOG_FOLDER_PATH = this.TESTCASES_FOLDER_PATH + File.separator + "logs";
		this.RESULTS_FOLDER_PATH = this.TESTCASES_FOLDER_PATH + File.separator + "results";

		if (aut_classpath == null || aut_classpath.length() == 0) {
			throw new Exception("GUIRippingTestCaseRunner: null AUT path.");
		}
		this.aut_classpath = aut_classpath;

		if (auto_mainclass == null || auto_mainclass.length() == 0) {
			throw new Exception("GUIRippingTestCaseRunner: null AUT main class.");
		}
		this.aut_mainclass = auto_mainclass;

		// if (gui == null) {
		// throw new Exception("GUIRippingTestCaseRunner: null GUI.");
		// }
		//
		// this.gui = gui;

		this.efg_path = efg_path;
		this.gui_path = gui_path;

		File f = new File(this.TESTCASES_FOLDER_PATH);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdir();
		}

		f = new File(this.STATES_FOLDER_PATH);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdir();
		}

		f = new File(this.LOG_FOLDER_PATH);
		if (!f.exists() || !f.isDirectory()) {
			f.mkdir();
		}

		// this.createFiles();
	}

	@Override
	public boolean runGUITestCase(final GUITestCase test) throws Exception {

		final String filename = this.createTestCaseFile(test);
		final String fn_subfix = filename.replace(".xml", "");
		final String testcase = this.TESTCASES_FOLDER_PATH + File.separator + filename;
		String cmd = this.REPLAYER_PATH + " -cp " + this.aut_classpath + " -c " + this.aut_mainclass + " -g "
				+ this.gui_path + " -e " + this.efg_path + " -t " + testcase + " -i " + this.INITIAL_WAIT + " -d "
				+ this.STEP_DELAY + " -so " + this.STEP_TIMEOUT + " -l " + this.LOG_FOLDER_PATH + File.separator
				+ fn_subfix + ".log -gs " + this.STATES_FOLDER_PATH + File.separator + fn_subfix + ".sta";
		// + " > /dev/null ";
		// System.out.println(cmd);
		final PrintWriter writer = new PrintWriter("test.sh", "UTF-8");
		writer.write(cmd);
		writer.close();

		// final Runtime rt = Runtime.getRuntime();
		// Process pr = rt.exec(cmd);
		// pr.waitFor();
		// final BufferedReader stdInput = new BufferedReader(new
		// InputStreamReader(pr.getInputStream()));
		// String s = null;
		// while ((s = stdInput.readLine()) != null) {
		// System.out.println(s);
		// }

		cmd = "sh runner.sh";
		// System.out.println(cmd);
		final Runtime rt = Runtime.getRuntime();
		final Process pr = rt.exec(cmd);
		pr.waitFor();
		// final BufferedReader stdInput = new BufferedReader(new
		// InputStreamReader(pr.getInputStream()));
		// String s = null;
		// while ((s = stdInput.readLine()) != null) {
		// System.out.println(s);
		// }

		return this.checkOracle(fn_subfix, test);
	}

	@Override
	public boolean[] runGUITestSuite(final GUITestCase[] tests) throws Exception {

		final boolean[] out = new boolean[tests.length];

		int cont = 0;
		for (final GUITestCase tc : tests) {
			out[cont] = this.runGUITestCase(tc);
			cont++;
		}
		return out;
	}

	// void createFiles() throws Exception {
	//
	// final GUIRippingConverter conv = new GUIRippingConverter();
	// final Document[] docs = conv.fromGUItoXML(this.gui);
	//
	// this.efg_path = XMLUtil.saveTMP(docs[1]);
	// this.gui_path = XMLUtil.saveTMP(docs[0]);
	// System.out.println(this.efg_path);
	// System.out.println(this.gui_path);
	//
	// }

	private boolean checkOracle(final String filename, final GUITestCase tc) throws Exception {

		final String outfile = this.RESULTS_FOLDER_PATH + File.separator + filename + ".out";
		final PrintWriter writer = new PrintWriter(new File(outfile), "UTF-8");

		final String logfilename = this.LOG_FOLDER_PATH + File.separator + filename + ".log";
		final int passStatus = this.logPassStatus(logfilename);
		if (passStatus != 0) {
			writer.println("TEST FAILED.");
			writer.println("Exception while executing test. Check log.");
			writer.close();
			return false;
		}

		// TODO: fix
		// // the oracle after each executed action is verified
		// final String sta_file = this.STATES_FOLDER_PATH + File.separator +
		// filename + ".sta";
		// final Document xml = XMLUtil.read(sta_file);
		// final NodeList steps = xml.getElementsByTagName("Step");
		// // if (tc.getActions().size() != steps.getLength()) {
		// // writer.close();
		// // throw new Exception("GUIRippingTestCaseRunner - checkOracle: wrong
		// // number of actions.");
		// // }
		//
		// int cont = 0;
		// for (final GUIAction act : tc.getActions()) {
		// // TODO fix
		// if (act instanceof Go) {
		// continue;
		// }
		// final Element step = (Element) steps.item(cont);
		//
		// if (act.getOracle() != null) {
		// // we create a new xml doc
		// final DocumentBuilderFactory docFactory =
		// DocumentBuilderFactory.newInstance();
		// final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// final Document gui_after_step = docBuilder.newDocument();
		// final Element gsel = gui_after_step.createElement("GUIStructure");
		// gui_after_step.appendChild(gsel);
		//
		// final Document efg = docBuilder.newDocument();
		// final Element n1 = efg.createElement("EFG");
		// final Element n2 = efg.createElement("Events");
		// n1.appendChild(n2);
		// efg.appendChild(n1);
		//
		// final List<Element> guistructure = XMLUtil.searchChildren(step,
		// "GUIStructure");
		// if (guistructure.size() != 1) {
		// writer.close();
		// throw new Exception("GUIRippingTestCaseRunner - checkOracle: error
		// parsing state file.");
		// }
		//
		// final List<Element> guis =
		// XMLUtil.searchChildren(guistructure.get(0), "GUI");
		// // TO FIX HERE
		// final Element copy = (Element) guis.get(0).cloneNode(true);
		// gui_after_step.adoptNode(copy);
		// gsel.appendChild(copy);
		//
		// final Object[] inputs = new Object[2];
		// inputs[0] = gui_after_step;
		// inputs[1] = efg;
		// // the GUI is converted
		// final GUIRippingConverter converter = new GUIRippingConverter();
		// final Document converted_gui = converter.convert(inputs);
		// final GUI out = GUIParser.parse(converted_gui);
		// if (out.getWindows().size() != 1) {
		// writer.close();
		// throw new Exception("GUIRippingTestCaseRunner - checkOracle: error
		// converting state file.");
		// }
		// if (!this.verifyWindowsSimilarity(act.getOracle(),
		// out.getWindows().get(0))) {
		// writer.println("TEST FAILED.");
		// writer.println("Oracle check failed at step " + cont + ".");
		// writer.close();
		// return false;
		// }
		// }
		// cont++;
		// }
		writer.println("TEST PASSED.");
		writer.close();
		return true;
	}

	private boolean verifyWindowsSimilarity(final Window oracle, final Window to_check) {

		final List<Widget> ws_tocheck = new ArrayList<>();
		ws_tocheck.addAll(to_check.getActionWidgets());
		ws_tocheck.addAll(to_check.getInputWidgets());
		ws_tocheck.addAll(to_check.getSelectableWidgets());

		final List<Widget> ws = new ArrayList<>();
		ws.addAll(oracle.getActionWidgets());
		ws.addAll(oracle.getInputWidgets());
		ws.addAll(oracle.getSelectableWidgets());

		loop: for (final Widget w : ws) {
			for (final Widget w1 : ws_tocheck) {
				if (w.isSame(w1)) {
					ws_tocheck.remove(w1);
					continue loop;
				}
			}
			return false;
		}

		return true;
	}

	private int logPassStatus(final String logfilename) throws Exception {

		BufferedReader br = null;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader(logfilename));

			boolean check = false;
			while ((sCurrentLine = br.readLine()) != null) {
				if ("------- END TESTCASE ------".equals(sCurrentLine.trim())) {
					check = true;
				}

				if (check && sCurrentLine.trim().startsWith("Pass status    :")) {

					final int ind = sCurrentLine.indexOf(":");
					final int passStatus = Integer.valueOf(sCurrentLine.trim().substring(ind + 1).trim());
					br.close();
					return passStatus;
				}

			}
			br.close();
			return -1;
		} catch (

		final Exception e) {
			e.printStackTrace();
			throw new Exception(
					"GUIRippingTestCaseRunner - logPassStatus: problem reading log file, " + e.getMessage());
		}
	}

	private String createTestCaseFile(final GUITestCase tc) throws Exception {

		// the xml testcase is created
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document xml = docBuilder.newDocument();
		final Element root = xml.createElement("TestCase");
		xml.appendChild(root);
		// the actions in the test cases are looped
		for (final GUIAction a : tc.getActions()) {
			// TODO: handle go actions
			if (a instanceof Go) {
				continue;
			}
			final Element step = xml.createElement("Step");
			root.appendChild(step);

			// the event id is the widget id with a "e" instead of the "w"
			// if the widget id does not start with a "w", a "e" is simply added
			// at the beginning
			String event_id = null;
			if (a.getWidget().getId().startsWith("w")) {
				event_id = "e" + a.getWidget().getId().substring(1);
			} else {
				event_id = "e" + a.getWidget().getId();
			}
			final Element eventid = xml.createElement("EventId");
			eventid.setTextContent(event_id);
			step.appendChild(eventid);

			final Element reaching = xml.createElement("ReachingStep");
			reaching.setTextContent("false");
			step.appendChild(reaching);

			if (a instanceof Fill) {
				final Fill f = (Fill) a;
				final Element parameter = xml.createElement("Parameter");
				parameter.setTextContent(f.getInput());
				step.appendChild(parameter);
			}

			if (a instanceof Select) {
				final Select s = (Select) a;
				final Element parameter = xml.createElement("Parameter");
				parameter.setTextContent(String.valueOf(s.getIndex()));
				step.appendChild(parameter);
			}
		}

		// filter to filter all the test case files in the folder
		final FilenameFilter textFilter = new FilenameFilter() {

			@Override
			public boolean accept(final File dir, final String name) {

				final String lowercaseName = name.toLowerCase();
				if (lowercaseName.startsWith("testcase_")) {
					return true;
				} else {
					return false;
				}
			}
		};

		final File dir = new File(this.TESTCASES_FOLDER_PATH);
		final File[] files = dir.listFiles(textFilter);
		int cont = 0;
		// the testcase with the highest number is found
		loop: while (true) {
			for (final File file : files) {
				if (file.getName().startsWith("testcase_" + cont)) {
					cont++;
					continue loop;
				}
			}
			break;
		}
		final String filename = "testcase_" + (cont) + ".xml";

		final String file_path = this.TESTCASES_FOLDER_PATH + File.separator + filename;
		// the xml is saved
		XMLUtil.save(file_path, xml);
		return filename;
	}

}
