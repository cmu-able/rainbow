package org.sa.rainbow.core.models;

import java.util.Collection;

import org.sa.rainbow.core.error.RainbowModelException;

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
    public void registerModelType (String typeName);

    /**
     * 
     * @return The collection of model types that the Models Manager knows about
     */
    public Collection<? extends String> getRegisteredModelTypes ();

    /**
     * Gets all the instances of a model of a certain type
     * 
     * @param modelType
     *            The model type
     * @return The collection of instances of the model type
     */
    public Collection<? extends IModelInstance> getModelsOfType (String modelType);

    /**
     * Registers a new model with the name and type with the model manager
     * 
     * @param modelType
     *            The type of the model
     * @param modelName
     *            The name of the model
     * @param model
     *            The model instance
     * @throws RainbowModelException
     */
    public void registerModel (String modelType, String modelName, IModelInstance<?> model)
            throws RainbowModelException;



    /**
     * Copies a model instance, and registers the copy wth the model
     * 
     * @param modelType
     * @param modelName
     * @param copyName
     * @return
     * @throws RainbowModelException
     */
    public <T> IModelInstance<T> copyInstance (String modelType, String modelName, String copyName)
            throws RainbowModelException;

    public abstract void unregisterModel (IModelInstance<?> model) throws RainbowModelException;

    public <T> IModelInstance<T> getModelInstanceByResource (String resource);
}
