package org.sa.rainbow.management.ports.local;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.IModelsManager;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;

public class LocalModelsManagerUSPort implements IRainbowModelUSBusPort {

    private IModelsManager m_modelsManager;

    public LocalModelsManagerUSPort (IModelsManager m) {
        m_modelsManager = m;
    }

    @Override
    public void updateModel (IRainbowModelCommandRepresentation command) {
        try {
            m_modelsManager.requestModelUpdate (command);
        }
        catch (IllegalStateException | RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public void updateModel (List<IRainbowModelCommandRepresentation> commands, boolean transaction) {
        try {
            m_modelsManager.requestModelUpdate (commands, transaction);
        }
        catch (IllegalStateException | RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        return m_modelsManager.getModelInstance (modelType, modelName);
    }

}
