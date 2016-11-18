package test.rft;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import resources.test.rft.Save_window_xmlHelper;
import src.usi.application.ActionManager;
import src.usi.application.ApplicationHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
import src.usi.gui.GUIParser;
import src.usi.gui.GUIWriter;
import src.usi.gui.GuiStateManager;
import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Window;
import src.usi.testcase.structure.Click;
import src.usi.xml.XMLUtil;

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
	 * @throws Exception
	 */
	public void testMain(final Object[] args) throws Exception {

		final String out_file = PathsManager.getProjectRoot() + File.separator + "files"
				+ File.separator + "for_test" + File.separator + "output" + File.separator
				+ "out.xml";
		ApplicationHelper application = null;
		try {

			ConfigurationManager.load(PathsManager.getProjectRoot()
					+ "/files/for_test/config/upm.properties");
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

			g.addWindow(windows.get(0));
			final Document doc = GUIWriter.writeGUI(g);

			src.usi.xml.XMLUtil.save(out_file, doc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		// oracle starting
		final Document d = XMLUtil.read(out_file);
		final NodeList ws = d.getElementsByTagName("window");
		if (ws.getLength() != 1) {
			throw new Exception("");
		}

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

		ExperimentManager.cleanUP();

	}
}
