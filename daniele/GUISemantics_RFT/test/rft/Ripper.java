package test.rft;

import java.io.File;

import resources.test.rft.RipperHelper;
import usi.RipperMain;

/**
 * Description : Functional Test Script
 *
 * @author usi
 */
public class Ripper extends RipperHelper {

	/**
	 * Script Name : <b>Ripper</b> Generated : <b>Jul 8, 2016 7:05:24 AM</b>
	 * Description : Functional Test Script Original Host : WinNT Version 6.1
	 * Build 7601 (S)
	 *
	 * @since 2016/07/08
	 * @author usi
	 */
	public void testMain(final Object[] args) {

		try {
			final String out_file = "files" + File.separator + "for_test" + File.separator
					+ "output" + File.separator + "outripping.xml";
			final String conf_file = "files" + File.separator + "for_test" + File.separator
					+ "config" + File.separator + "upm.properties";
			final Object[] inputs = new Object[4];
			inputs[0] = "--conf";
			inputs[1] = conf_file;
			inputs[2] = "--outxml";
			inputs[3] = out_file;

			RipperMain.main(inputs);

			// oracle
		} catch (final Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
}
