package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.Collection;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IModelsManagerPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBModeslManagerRemoteInterface extends IModelsManagerPort {

    @Override
    @ReturnTypeMapping ("set<string>")
    public Collection<? extends String> getRegisteredModelTypes ();

    @Override
    @ReturnTypeMapping ("rainbow_model")
    @ParametersTypeMapping ({ "string", "string" })
    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName);

}
