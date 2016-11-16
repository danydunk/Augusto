package src.usi;

import resources.src.usi.RipperMainHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.gui.Ripper;
import src.usi.gui.structure.GUI;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class RipperMain extends RipperMainHelper {

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
			final Ripper ripper = new Ripper();
			final GUI gui = ripper.ripApplication();
			ExperimentManager.dumpGUI(gui, out_file);

		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
