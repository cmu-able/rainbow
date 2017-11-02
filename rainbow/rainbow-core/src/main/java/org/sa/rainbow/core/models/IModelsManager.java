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

import org.sa.rainbow.core.error.RainbowModelException;

import java.util.Collection;

/**
 * The models manager is a repository for all models. Features should include - registering different kinds of models -
 * getting the model keyed by types - getting the list of registered types - connecting to various busses? -
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IModelsManager extends IModelUpdater, IModelInstanceProvider {
    /**
     * Registers a model type that will be keyed by the type name
     * 
     * @param typeName
     *            The name to register the model type under
     */
    void registerModelType (String typeName);

    /**
     * 
     * @return The collection of model types that the Models Manager knows about
     */

    Collection<? extends String> getRegisteredModelTypes ();

    /**
     * Gets all the instances of a model of a certain type
     * 
     * @param modelType
     *            The model type
     * @return The collection of instances of the model type
     */

    Collection<? extends IModelInstance<?>> getModelsOfType (String modelType);

    /**
     * Registers a new model with the name and type with the model manager
     *
     * @param modelRef
     *            The model reference
     * @param model
     *            The model instance
     * @throws RainbowModelException
     */
    void registerModel (ModelReference modelRef, IModelInstance<?> model)
            throws RainbowModelException;



    /**
     * Copies a model instance, and registers the copy wth the model
     *
     * @param modelRef
     * @param copyName
     * @return
     * @throws RainbowModelException
     */
    <T> IModelInstance<T> copyInstance (ModelReference modelRef, String copyName)
            throws RainbowModelException;

    void unregisterModel (IModelInstance<?> model) throws RainbowModelException;

    <T> IModelInstance<T> getModelInstanceByResource (String resource);
}
