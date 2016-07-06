package test.rft;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import resources.test.rft.Save_window_xmlHelper;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIWriter;
import usi.gui.GuiStateManager;
import usi.gui.structure.GUI;
import usi.gui.structure.GUIParser;
import usi.gui.structure.Window;
import usi.xml.XMLUtil;

import com.rational.test.ft.object.interfaces.RootTestObject;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Save_window_xml extends Save_window_xmlHelper {

	/**
	 * Script Name : <b>Save_window_xml</b> Generated : <b>Jul 5, 2016 7:08:25
	 * AM</b> Description : Functional Test Script Original Host : WinNT Version
	 * 6.1 Build 7601 (S)
	 *
	 * @since 2016/07/05
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			final String out_file = "files" + System.getProperty("file.separator") + "for_test"
					+ System.getProperty("file.separator") + "output"
					+ System.getProperty("file.separator") + "out.xml";
			ApplicationHelper application = null;
			try {
				final String conf_file = "files" + System.getProperty("file.separator")
						+ "for_test" + System.getProperty("file.separator") + "config"
						+ System.getProperty("file.separator") + "upm.properties";
				ConfigurationManager.load(conf_file);
				ExperimentManager.init();
				application = new ApplicationHelper();
				final RootTestObject root = application.startApplication();
				final GuiStateManager gui = new GuiStateManager(root);
				final List<Window> windows = gui.getCurrentWindows();
				final GUIWriter writer = new GUIWriter();
				// final Element w1 = writer.writeWindow(windows.get(0), true);
				final Element w2 = writer.writeWindow(windows.get(0), false);

				final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				final Document doc = docBuilder.newDocument();
				// doc.adoptNode(w1);
				doc.adoptNode(w2);

				final Element out = doc.createElement("GUI");
				doc.appendChild(out);
				// out.appendChild(w1);
				out.appendChild(w2);

				usi.xml.XMLUtil.save(out_file, doc);
			} catch (final Exception e) {
				e.printStackTrace();
			}
			// oracle starting
			final Document d = XMLUtil.read(out_file);
			final NodeList ws = d.getElementsByTagName("window");
			if (ws.getLength() != 1) {
				throw new Exception("");
			}
			// List<Element> list = XMLUtil.searchChildren((Element) ws.item(0),
			// "input_widget");
			// if (list.size() != 1) {
			// throw new Exception("");
			// }
			//
			// String label = XMLUtil.searchChildren(list.get(0),
			// "label").get(0).getTextContent();
			// if
			// (!label.equals("Please enter the master password for this database"))
			// {
			// throw new Exception("");
			// }

			List<Element> list = XMLUtil.searchChildren((Element) ws.item(0), "action_widget");
			if (list.size() != 24) {
				throw new Exception("");
			}

			list = XMLUtil.searchChildren((Element) ws.item(0), "selectable_widget");
			final String label = XMLUtil.searchChildren(list.get(0), "descriptor").get(0)
					.getTextContent();
			if (!label.equals("Universal Password Manager")) {
				throw new Exception("");
			}
			application.closeApplication();

			final GUI g = GUIParser.parse(d);
			if (g.getWindows().size() != 1) {
				throw new Exception("");
			}

			if (g.getWindows().get(0).getActionWidgets().size() != 24) {
				throw new Exception("");
			}
		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
