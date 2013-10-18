package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

public interface IModelDSBusPublisherPort {
    public enum Result {
        SUCCESS, FAILURE, UNKNOWN
    };

    public class OperationResult {
        public Result result;
        public String reply;
    }

    public abstract OperationResult publishOperation (IRainbowModelCommandRepresentation cmd);
}
