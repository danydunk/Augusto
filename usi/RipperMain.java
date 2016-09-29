package usi;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import resources.usi.RipperMainHelper;
import usi.configuration.ConfigurationManager;
import usi.configuration.ExperimentManager;
import usi.gui.pattern.GUIPatternParser;
import usi.gui.pattern.Pattern_action_widget;
import usi.gui.ripping.Ripper;
import usi.gui.structure.GUI;
import usi.xml.XMLUtil;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class RipperMain extends RipperMainHelper {

	private static String AW_TO_FILTER_PATH = "config" + File.separator + "awfilter.xml";

	/**
	 * Script Name : <b>RipperMain</b> Generated : <b>Jul 8, 2016 6:18:02 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/07/08
	 * @author usi
	 */

	public static void main(final Object[] args) {

		final RipperMain r = new RipperMain();
		r.testMain(args);
	}

	public void testMain(final Object[] args) {

		try {
			String out_file = null;
			switch (args.length) {
			case 0:
				ConfigurationManager.load();
				break;

			case 2:
				if (args[0].toString().equals("--conf")) {
					ConfigurationManager.load(args[1].toString());
				} else if (args[0].toString().equals("--outxml")) {
					out_file = args[1].toString();
				} else {
					System.out.println("Error: unknown input parameter.");
					return;
				}
				break;
			case 4:
				if (!args[0].toString().equals("--conf") || !args[2].toString().equals("--outxml")) {
					System.out.println("Error: unknown input parameter.");
					return;
				}
				ConfigurationManager.load(args[1].toString());
				out_file = args[3].toString();
				break;

			default:
				System.out.println("Error: wrong number of input parameters.");
				return;
			}

			ExperimentManager.init();
			final Ripper ripper = new Ripper(ConfigurationManager.getSleepTime(),
					this.loadAWtoFilter(AW_TO_FILTER_PATH));
			final GUI gui = ripper.ripApplication();
			ExperimentManager.dumpGUI(gui, out_file);

		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}

	private List<Pattern_action_widget> loadAWtoFilter(final String path) {

		Document doc = null;
		try {
			doc = XMLUtil.read(path);
		} catch (final Exception e) {
			System.out.println("Filter file not found.");
			return null;
		}
		final Node root = doc.getDocumentElement();
		final List<Pattern_action_widget> out = GUIPatternParser.createActionsWidgets(root);
		if (out != null && out.size() > 0) {
			return out;
		}
		return null;
	}
}
