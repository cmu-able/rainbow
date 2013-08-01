package org.sa.rainbow.models;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

public interface IModelUpdater {

    void requestModelUpdate (IRainbowModelCommandRepresentation command) throws IllegalStateException, RainbowException;

    void requestModelUpdate (List<IRainbowModelCommandRepresentation> commands, boolean transaction)
            throws IllegalStateException, RainbowException;

    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);

}
