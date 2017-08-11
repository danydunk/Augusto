package src.usi;

import resources.src.usi.RipperMainHelper;
import src.usi.configuration.ConfigurationManager;
import src.usi.configuration.ExperimentManager;
import src.usi.configuration.PathsManager;
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

		final long beginTime = System.currentTimeMillis();
		try {
			switch (args.length) {
			case 1:
				PathsManager.setProjectRoot(args[0].toString());
				break;

			default:
				PathsManager.setProjectRoot(".");
				System.out.println("Error: wrong number of input parameters.");
				return;
			}

			ConfigurationManager.load();
			ExperimentManager.init();
			final Ripper ripper = new Ripper();
			final GUI gui = ripper.ripApplication();
			ExperimentManager.dumpGUI(gui);
			ExperimentManager.cleanUP();
			final long tottime = (System.currentTimeMillis() - beginTime) / 1000;
			System.out.println("RIPPING ELAPSED TIME: " + tottime);
		} catch (final Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		} finally {
			ExperimentManager.cleanUP();
		}
	}
}
