/**
 * Created November 4, 2006.
 */
package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.core.Identifiable;


/**
 * General interface for the system Effector.
 * The Identifiable.id() returns the reference ID of this effector, which is
 * the same ID used by Rainbow to obtain access to particular effectors.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEffector extends Identifiable {

	public static enum Outcome {
		UNKNOWN, CONFOUNDED, FAILURE, SUCCESS, TIMEOUT
	};

	public static enum Kind {
		/** An effector based on shell or Perl script */
		SCRIPT,
		/** An effector implemented purely in Java */
		JAVA,
		/** Null type, returned by the NULL_EFFECTOR */
		NULL
	}

	public static IEffector NULL_EFFECTOR = new IEffector() {
		public String id() {
			return "NullEffector@0.0.0.0";
		}
		public String service() {
			return "ANull";
		}
		public Kind kind() {
			return Kind.NULL;
		}
		public Outcome execute(String[] args) {
			return Outcome.UNKNOWN;
		}
	};

	/**
	 * Returns the name of effector service provided by this IEffector.
	 * @return String  Service name
	 */
	public String service ();

	/**
	 * Returns the implementation {@link IEffector.Kind type} of this effector
	 * @return Type  the implementation type
	 */
	public Kind kind ();

	/**
	 * Executes the effect supplied by this effector, applying any arguments.
	 * @param args  array of String arguments
	 * @return Outcome  the execution outcome as defined in the enum
	 *     {@link org.sa.rainbow.translator.effectors.IEffector.Outcome <code>Outcome</code>}
	 */
	public Outcome execute (String[] args);

}
