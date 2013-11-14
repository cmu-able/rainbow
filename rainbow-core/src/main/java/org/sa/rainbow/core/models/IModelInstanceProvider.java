package org.sa.rainbow.core.models;

public interface IModelInstanceProvider {

    /**
     * Returns a model instance keyed by name and type
     * 
     * @param modelType
     * @param modelName
     * @return
     */
    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);
}
