package org.sa.rainbow.core.ports;

import java.util.Collection;

import org.sa.rainbow.core.models.IModelInstance;

public interface IModelsManagerPort {
    public Collection<? extends String> getRegisteredModelTypes ();

    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);

}
