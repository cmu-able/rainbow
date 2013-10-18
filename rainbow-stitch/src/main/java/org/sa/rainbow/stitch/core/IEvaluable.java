/**
 * Created August 30, 2006.
 */
package org.sa.rainbow.stitch.core;

import java.util.Set;

import org.acmestudio.acme.element.IAcmeElement;



/**
 * An identifier Interface which indicates an IScope that can also be evaluated
 * for some result or outcome.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEvaluable extends IScope {

	/**
	 * Evaluates the AST stored in the scope; return a result if applicable.
	 * 
	 * @param  incoming arguments
	 * @return <code>Object</code> if evaluation has result, null otherwise
	 */
	public Object evaluate (Object[] argsIn);

	/**
	 * Calculates the estimated average time duration required to perform
	 * this evaluable construct.
	 * @return long  estimated upper-bound time cost in milliseconds
	 */
	public long estimateAvgTimeCost ();

	/**
	 * Returns a set of model elements (IAcmeElement) used by this IEvaluable.
	 * An element is considered <em>used</em> by this IEvaluable if it an
	 * operator may have made changes to it, which usually means the element
	 * has been passed as an argument into an operator.
	 * 
	 * @return <code>Set</code> of <code>IAcmeElement</code>, or empty set if none.
	 */
	public Set<? extends IAcmeElement> modelElementsUsed ();
	

}
