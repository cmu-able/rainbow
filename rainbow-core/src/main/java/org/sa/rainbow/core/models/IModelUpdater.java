package org.sa.rainbow.core.models;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

public interface IModelUpdater {

    /**
     * Requests that an update to a model be done via the command.
     * 
     * @param command
     *            The details of the command to execute
     * @throws IllegalStateException
     *             thrown if the command turns out to be unexecutable
     * @throws RainbowException
     */
    void requestModelUpdate (IRainbowModelCommandRepresentation command) throws IllegalStateException, RainbowException;

    /**
     * Requests that a list of commands be executed to update a model. If done in a transaction, the all the commands
     * are effected on the model or none. A prerequisite of issuing a transaction is that all of the commands must be
     * updating the same model.
     * 
     * @param commands
     *            The commands to execute
     * @param transaction
     *            Whether to execute in a transaction
     * @throws IllegalStateException
     * @throws RainbowException
     */
    void requestModelUpdate (List<IRainbowModelCommandRepresentation> commands, boolean transaction)
            throws IllegalStateException, RainbowException;

    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);

}
