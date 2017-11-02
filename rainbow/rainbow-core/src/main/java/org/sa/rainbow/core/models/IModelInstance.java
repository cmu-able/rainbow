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

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Captures an abstract Rainbow model, for example a model of the architecture. A model in Rainbow should provide the
 * following things:
 * 
 * 1. A way to get a model instance keyed by some value (e.g., the filename) <br>
 * 2. A command factory that can be used to make changes to the model -- not sure what the abstract representation of a
 * command factory should be <br>
 * 3. Lock seeking methods for a model instance? <br>
 * 4. Get a snapshot of a model instance (a snapshot will not be updated by any changes through the command interface)
 * (p.s., this is needed at least by the tactic executor, which requires no changes to the model)
 * 
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IModelInstance<T> {

    /**
     * 
     * @return the Model instance being managed (e.g., an Acme system, and impact model, ...)
     */
    T getModelInstance ();

    /**
     * Sets the model to be managed by this instance
     * 
     * @param model
     */
    void setModelInstance (T model);

    /**
     * Creates a copy of this model instance, giving it the new name if appropriate
     * 
     * @param newName
     * @return
     * @throws RainbowCopyException
     */
    IModelInstance<T> copyModelInstance (String newName) throws RainbowCopyException;

    /**
     * 
     * @return The type of this model (e.g., Acme, computation, ...)
     */
    String getModelType ();

    String getModelName ();

    ModelCommandFactory<T> getCommandFactory ();

    void setOriginalSource (String source);

    String getOriginalSource ();

    void dispose () throws RainbowException;
}
