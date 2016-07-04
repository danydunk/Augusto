package test.integration;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIWriter;
import usi.gui.GuiStateManager;
import usi.gui.widgets.Window;

import com.rational.test.ft.object.interfaces.RootTestObject;

/**
 * Description : Functional Test Script
 * 
 * @author lta
 */
public class Test_save_window extends Test_save_windowHelper {

	/**
	 * Script Name : <b>Test_save_window</b> Generated : <b>Jul 2, 2016 8:24:45
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 * 
	 * @since 2016/07/02
	 * @author lta
	 */
	public void testMain(Object[] args) {

		try {
			String conf_file = "resources" + System.getProperty("file.separator") + "for_test"
					+ System.getProperty("file.separator") + "config" + System.getProperty("file.separator")
					+ "upm.properties";
			ConfigurationManager.load(conf_file);
			ExperimentManager.init();
			ApplicationHelper application = new ApplicationHelper();
			RootTestObject root = application.startApplication();
			GuiStateManager gui = new GuiStateManager(root);
			List<Window> windows = gui.getCurrentGUI();
			GUIWriter writer = new GUIWriter();
			Element w1 = writer.writeWindow(windows.get(0), true);
			Element w2 = writer.writeWindow(windows.get(1), false);

			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element out = doc.createElement("GUI");
			doc.appendChild(out);
			out.appendChild(w1);
			out.appendChild(w2);
			String out_file = "resources" + System.getProperty("file.separator") + "for_test"
					+ System.getProperty("file.separator") + "output" + System.getProperty("file.separator")
					+ "out.xml";
			usi.xml.XMLUtil.save(out_file, doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
