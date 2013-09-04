package org.sa.rainbow.core.ports.local;

import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.ports.IRainbowModelUSBusPort;

public class LocalModelsManagerClientUSPort implements IRainbowModelUSBusPort {

    LocalModelsManagerUSPort m_connectedPort;

    public LocalModelsManagerClientUSPort (Identifiable client) {
    }

    @Override
    public void updateModel (IRainbowModelCommandRepresentation command) {
        if (m_connectedPort != null) {
            m_connectedPort.updateModel (command);
        }
        else
            throw new IllegalStateException ("This port is not connected to anything");

    }

    @Override
    public void updateModel (List<IRainbowModelCommandRepresentation> commands, boolean transaction) {
        if (m_connectedPort != null) {
            m_connectedPort.updateModel (commands, transaction);
        }
        else
            throw new IllegalStateException ("This port is not connected to anything");
    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        if (m_connectedPort != null)
            return m_connectedPort.getModelInstance (modelType, modelName);
        else
            throw new IllegalStateException ("This port is not connected to anything");
    }

    public void connect (LocalModelsManagerUSPort localModelsManagerUSPort) {
        m_connectedPort = localModelsManagerUSPort;
    }

}
