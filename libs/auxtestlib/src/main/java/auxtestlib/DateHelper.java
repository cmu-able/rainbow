package auxtestlib;

import java.util.Calendar;
import java.util.Date;

/**
 * Class that provides simple methods that are usually considered when handling
 * dates.
 */
public final class DateHelper {
	/**
	 * Utility class: no constructor.
	 */
	private DateHelper() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Add a number of days to a data.
	 * 
	 * @param originDate origin date to add days to
	 * @param numberOfDays the number of days to add. If the numberOfDays is
	 * positive then the day is added, otherwise it is removed.
	 * 
	 * @return the date after the adding operation.
	 */
	public static Date dateAddDays(Date originDate, int numberOfDays) {
		int operator = 1;
		if (numberOfDays < 0) {
			operator = -1;
		}

		Calendar c = Calendar.getInstance();
		c.setTime(originDate);
		for (int i = 0; i < Math.abs(numberOfDays); i++) {
			c.add(Calendar.DAY_OF_MONTH, operator);
		}
		return c.getTime();
	}

	/**
	 * Returns the difference in days of two dates. The method ignores the order
	 * of the parameters. Note that the method ignores the daylight savings.
	 * 
	 * @param date1 the first date
	 * @param date2 the second date
	 * 
	 * @return the difference in days.
	 */
	public static int getSimpleDayDifference(Date date1, Date date2) {
		long res;

		res = (date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24);

		return (int) Math.abs(res);

	}
}
