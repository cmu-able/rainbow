/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public interface IModelUSBusPort extends IDisposablePort {

    /**
     * Is used to update the model. On the Model manager side, it calls the model manager to request an update to the
     * model. On the model client side, it calls this method to request the updated <br>
     * NOTE: This is a pure publish model - clients do not care about whether the update succeeded
     * 
     * @param command
     *            The command to use to update the model
     * @throws RainbowException
     * @throws IllegalStateException
     */
    void updateModel (IRainbowOperation command);

    /**
     * Is used to update the model with a list of commands. The commands may be executed as a transaction (i.e., failure
     * of a command results in no change to the model.)
     * 
     * @param commands
     *            The list of commands to update the model
     * @param transaction
     *            Whether this should be run as a transaction
     * @throws RainbowException
     * @throws IllegalStateException
     */
    void updateModel (List<IRainbowOperation> commands, boolean transaction);

    /**
     * Queries the model manager to return an instance of the model that can be used for querying. This can be used by
     * clients to work out what commaands to issue, or to see if a command has been executed.
     * 
     * @param modelRef
     *            The reference to the model in the models manager
     * @return A model instance. NOTE: currently in Rainbow only local models are supported.
     */
    <T> IModelInstance<T> getModelInstance (ModelReference modelRef);

}
