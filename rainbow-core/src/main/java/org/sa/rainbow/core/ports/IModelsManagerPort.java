package org.sa.rainbow.core.ports;

import java.util.Collection;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelInstanceProvider;

public interface IModelsManagerPort extends IModelInstanceProvider {
    public Collection<? extends String> getRegisteredModelTypes ();

    @Override
    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);

}
