package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import resources.test.rft.Save_window_xmlHelper;
import usi.application.ActionManager;
import usi.application.ApplicationHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.GUIParser;
import usi.gui.GUIWriter;
import usi.gui.GuiStateManager;
import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;
import usi.testcase.structure.Click;
import usi.xml.XMLUtil;

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
			final String out_file = "files" + File.separator + "for_test" + File.separator
					+ "output" + File.separator + "out.xml";
			ApplicationHelper application = null;
			try {
				final String conf_file = "files" + File.separator + "for_test" + File.separator
						+ "config" + File.separator + "upm.properties";
				ConfigurationManager.load(conf_file);
				ExperimentManager.init();
				application = ApplicationHelper.getInstance();
				application.startApplication();
				final GuiStateManager gui = GuiStateManager.getInstance();
				final GUI g = new GUI();
				List<Window> windows = gui.readGUI();
				final Action_widget aw = windows.get(0).getActionWidgets().get(0);

				final Click click = new Click(windows.get(0), null, aw);
				ActionManager.executeAction(click);
				windows = gui.readGUI();
				final GUIWriter writer = new GUIWriter();

				g.addWindow(windows.get(0));
				final Document doc = writer.writeGUI(g);

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
			if (XMLUtil.searchChildren(list.get(0), "descriptor").size() > 0) {

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
