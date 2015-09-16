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
package org.sa.rainbow.core.models;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

import java.util.List;

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
    void requestModelUpdate (IRainbowOperation command) throws IllegalStateException, RainbowException;

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
    void requestModelUpdate (List<IRainbowOperation> commands, boolean transaction)
            throws IllegalStateException, RainbowException;

    <T> IModelInstance<T> getModelInstance (ModelReference modelRef);

}
