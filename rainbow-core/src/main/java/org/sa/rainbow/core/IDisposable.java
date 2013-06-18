/**
 * Created January 17, 2007.
 */
package org.sa.rainbow.core;

/**
 * An interface declaring the support for, thus (hopefully) the implementation
 * of, a terminate (i.e., dispose) method.  A Class with its own dispose
 * methods may call dispose() from terminate.
 * <p>
 * The contract is that, before terminate() gets executed, isTerminate() should
 * NOT return <code>true</code> (except possibly in the early, uninitialized
 * stage).  After the end of the terminate() method, isTerminated() should
 * return <code>true</code>.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IDisposable {

	/**
	 * Disconnects any I/O connections, disposes of all resources, and
	 * null-out members as good measure.
	 */
	public void dispose ();

	/**
	 * Returns whether this object is considered to have been disposed,
	 * usually by checking some key data member, or via an explicit boolean.
	 * @return boolean  <code>true</code> if object disposed, <code>false</code> otherwise
	 */
	public boolean isDisposed ();

}
