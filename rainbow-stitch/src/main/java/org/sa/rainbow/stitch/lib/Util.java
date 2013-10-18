/**
 * Renamed from znews0.operator.QueryOp, July 22, 2007.
 */
package org.sa.rainbow.stitch.lib;



/**
 * Utility class for Stitch scripts.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class Util {

	/**
	 * Prints the String representation of given object to stdout.
	 * @param o  Object to print to stdout
	 */
	public static void print (Object o) {
		System.out.println(o.toString());
	}

	public static long currentTimeMillis () {
		return System.currentTimeMillis();
	}


	public static double max (double a, double b) {
		return Math.max(a, b);
	}

	public static int max (int a, int b) {
		return Math.max(a, b);
	}

}
