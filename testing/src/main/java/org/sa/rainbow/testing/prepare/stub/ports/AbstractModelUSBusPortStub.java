package org.sa.rainbow.testing.prepare.stub.ports;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;

import java.util.List;

public abstract class AbstractModelUSBusPortStub implements IModelUSBusPort {
    /**
     * Is used to update the model. On the Model manager side, it calls the model manager to request an update to the
     * model. On the model client side, it calls this method to request the updated <br>
     * NOTE: This is a pure publish model - clients do not care about whether the update succeeded
     *
     * @param command The command to use to update the model
     */
    @Override
    public void updateModel(IRainbowOperation command) {

    }

    /**
     * Is used to update the model with a list of commands. The commands may be executed as a transaction (i.e., failure
     * of a command results in no change to the model.)
     *
     * @param commands    The list of commands to update the model
     * @param transaction Whether this should be run as a transaction
     */
    @Override
    public void updateModel(List<IRainbowOperation> commands, boolean transaction) {

    }

    /**
     * Queries the model manager to return an instance of the model that can be used for querying. This can be used by
     * clients to work out what commaands to issue, or to see if a command has been executed.
     *
     * @param modelRef The reference to the model in the models manager
     * @return A model instance. NOTE: currently in Rainbow only local models are supported.
     */
    @Override
    public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
        return null;
    }

    /**
     * Should be called when this port is no longer required. Implementors should dispose of all resources.
     */
    @Override
    public void dispose() {

    }
}
