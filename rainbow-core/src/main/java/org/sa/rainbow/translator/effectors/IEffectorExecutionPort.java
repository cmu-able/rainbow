package org.sa.rainbow.translator.effectors;

import java.util.List;

import org.sa.rainbow.core.ports.IDisposablePort;

public interface IEffectorExecutionPort extends IDisposablePort {
    public static enum Outcome {
        UNKNOWN, CONFOUNDED, FAILURE, SUCCESS, TIMEOUT
    };

    /**
     * Executes the effect supplied by this effector, applying any arguments.
     * 
     * @param args
     *            array of String arguments
     * @return Outcome the execution outcome as defined in the enum
     *         {@link org.sa.rainbow.translator.effectors.IEffector.Outcome <code>Outcome</code>}
     */
    public Outcome execute (List<String> args);
}
