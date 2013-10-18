package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

public interface IEffectorLifecycleBusPort {
    public void reportCreated (IEffectorIdentifier effector);

    public void reportDeleted (IEffectorIdentifier effector);

    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args);
}
