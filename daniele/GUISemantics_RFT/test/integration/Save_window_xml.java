package test.integration;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIWriter;
import usi.gui.GuiStateManager;
import usi.gui.widgets.Window;
import usi.util.MainTestHelper;

import com.rational.test.ft.object.interfaces.RootTestObject;

public class Save_window_xml extends MainTestHelper {

	@Test
	public void test() throws Exception {

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
			assertEquals(2, windows.size());
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
