package org.sa.rainbow.models.ports;

import java.util.List;

import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public interface IRainbowModelUSBusPort {


    public void updateModel (IRainbowModelCommandRepresentation command);
    public void updateModel (List<IRainbowModelCommandRepresentation> commands, boolean transaction);

    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);
}
