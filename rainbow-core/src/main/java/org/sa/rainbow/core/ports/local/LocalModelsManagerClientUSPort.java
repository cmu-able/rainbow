package org.sa.rainbow.core.ports.local;

import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;

public class LocalModelsManagerClientUSPort implements IModelUSBusPort {

    LocalModelsManagerUSPort m_connectedPort;

    public LocalModelsManagerClientUSPort (Identifiable client) {
    }

    @Override
    public void updateModel (IRainbowOperation command) {
        if (m_connectedPort != null) {
            m_connectedPort.updateModel (command);
        }
        else
            throw new IllegalStateException ("This port is not connected to anything");

    }

    @Override
    public void updateModel (List<IRainbowOperation> commands, boolean transaction) {
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

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }


}
