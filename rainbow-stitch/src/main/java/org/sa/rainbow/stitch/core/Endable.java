/**
 * Created August 28, 2006.
 */
package org.sa.rainbow.stitch.core;

/**
 * Inspired by Nicholas Sherman's AcmeStudio code, this interface defines a
 * Runnable that can also be safely ended.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface Endable extends Runnable {
	public void end ();
}
