package org.sa.rainbow.testing.prepare.stub.ports;

import org.sa.rainbow.core.models.commands.IRainbowOperation;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is stubbed AbstractModelUSBusPortStub that stores all operations from a gauge.
 */
public class OperationCollectingModelUSBusPortStub extends AbstractModelUSBusPortStub {

    private BlockingQueue<IRainbowOperation> operations = new LinkedBlockingQueue<>();

    /**
     * Wait and retrieve the next action from the gauge.
     *
     * @return the next operation
     */
    public IRainbowOperation takeOperation() {
        try {
            return operations.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is used to update the model. On the Model manager side, it calls the model manager to request an update to the
     * model. On the model client side, it calls this method to request the updated <br>
     * NOTE: This is a pure publish model - clients do not care about whether the update succeeded
     *
     * @param command The command to use to update the model
     */
    @Override
    public void updateModel(IRainbowOperation command) {
        try {
            operations.put(command);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        for (IRainbowOperation command : commands) {
            updateModel(command);
        }
    }
}
