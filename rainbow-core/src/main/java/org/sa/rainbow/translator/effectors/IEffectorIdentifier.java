package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.core.Identifiable;

public interface IEffectorIdentifier extends Identifiable {

    public static enum Kind {
        /** An effector based on shell or Perl script */
        SCRIPT,
        /** An effector implemented purely in Java */
        JAVA,
        /** Null type, returned by the NULL_EFFECTOR */
        NULL
    }

    /**
     * Returns the name of effector service provided by this IEffector.
     * 
     * @return String Service name
     */
    public String service ();

    /**
     * Returns the implementation {@link IEffector.Kind type} of this effector
     * 
     * @return Type the implementation type
     */
    public Kind kind ();
}
