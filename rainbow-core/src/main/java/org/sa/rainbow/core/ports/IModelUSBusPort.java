package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public interface IModelUSBusPort {

    /**
     * Is used to update the model. On the Model manager side, it calls the model manager to request an update to the
     * model. On the model client side, it calls this method to request the updated <br>
     * NOTE: This is a pure publish model - clients do not care about whether the update succeeded
     * 
     * @param command
     *            The command to use to update the model
     */
    public void updateModel (IRainbowOperation command);

    /**
     * Is used to update the model with a list of commands. The commands may be executed as a transaction (i.e., failure
     * of a command results in no change to the model.)
     * 
     * @param commands
     *            The list of commands to update the model
     * @param transaction
     *            Whether this should be run as a transaction
     */
    public void updateModel (List<IRainbowOperation> commands, boolean transaction);

    /**
     * Queries the model manager to return an instance of the model that can be used for querying. This can be used by
     * clients to work out what commaands to issue, or to see if a command has been executed.
     * 
     * @param modelType
     *            The type of the model requested
     * @param modelName
     *            The name of the model in the model manager
     * @return A model instance. NOTE: currently in Rainbow only local models are supported.
     */
    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);
}
