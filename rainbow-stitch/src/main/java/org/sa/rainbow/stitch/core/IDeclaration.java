/**
 * Created March 16, 2006
 */
package org.sa.rainbow.stitch.core;

/**
 * Interface for a variable or external item declared within a scope, allowing
 * query of its declaration scope, acess to the item itself 
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IDeclaration {

	/**
	 * Returns the Scope within which this declaration was created
	 * @return  the declaring Scope
	 */
	public IScope scope ();

	/**
	 * Returns the type signature of this declaration to be used as key
	 * @return  the type signature string
	 */
	public String signature ();
}
