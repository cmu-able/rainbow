package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public interface IModelDSBusPublisherPort extends IRainbowMessageFactory, IDisposablePort {
    public enum Result {
        SUCCESS, FAILURE, UNKNOWN
    };

    public class OperationResult {
        public Result result;
        public String reply;
    }

    public static final String            TACTIC_NAME         = "TACTIC_NAME";
    public static final String            STRATEGY_NAME       = "STRATEGY_NAME";
    public static final String            STRATEGY_OUTCOME    = "STRATEGY_OUTCOME";
    public static final String            TACTIC_SUCCESS      = "TACTIC_SUCCESS";
    public static final String            TACTIC_DURATION     = "TACTIC_DURATION";
    public static final String            TACTIC_PARAM        = "TACTIC_PARAM_";

    public abstract OperationResult publishOperation (IRainbowOperation cmd);

    public abstract void publishMessage (IRainbowMessage msg);

}
