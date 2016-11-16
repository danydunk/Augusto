package src.usi.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 */
public class DateUtility {

	private static final String FORMAT = "yyyyMMdd_HHmm";

	/**
	 * Get today date and current time in format: yyyyMMdd_HHmm.
	 *
	 *
	 * @return current date and time in format yyyyMMdd_HHmm
	 */
	public static String now() {

		final Calendar calendar = Calendar.getInstance();
		final SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);

		return formatter.format(calendar.getTime());
	}
}