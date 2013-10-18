/**
 * Created April 14, 2007.
 */
package org.sa.rainbow.stitch.model;

/**
 * An interface provided to the rainbow.core package to allow a ModelOperator
 * implementation to be set on Ohana, which Ohana would use to invoke Effectors.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface ModelOperator {

	public static enum OperatorResult {
		UNKNOWN, SUCCESS, FAILURE;
		public static OperatorResult parseEffectorResult (String result) {
			OperatorResult rv = FAILURE;
			if (result == null) return rv;
			try {
				rv = valueOf(result);
			} catch (IllegalArgumentException e) {  // interpret as failure!
			}
			return rv;
		}
	}

	/**
	 * A No-op instance of this interface, used when a ModelOperator
	 * implementation has not yet been set when required.
	 */
	public static final ModelOperator NO_OP = new ModelOperator() {
		public Object invoke(String name, Object[] args) {
			return null;
		}

		@Override
		public Object lookupOperator(String name) {
			return null;
		};
	};

	/**
	 * If an operator identified by the given name exists, invokes the named
	 * operator, supplying the arguments, where the zero-th argument should be
	 * the object used to determine the target to invoke the arch operator,
	 * and the remainder list serves as arguments.  If no operator exists, this
	 * method returns the String "UNKNOWN".
	 * @param name  the name of arch operator to invoke
	 * @param args  args[0] is the component, connector, or element on which to
	 *     invoke the arch operator
	 * @return Object  a String representation of the return value, which should be
	 *     parsed into an {@link OperatorResult} enum..
	 */
	public Object invoke (String name, Object[] args);
	
	public Object lookupOperator (String name);

}
