package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBModelsManagerProviderPort extends AbstractESEBDisposableRPCPort implements
IESEBModeslManagerRemoteInterface {

    static Logger          LOGGER = Logger.getLogger (ESEBModelsManagerProviderPort.class);
    private IModelsManager m_modelsManager;

    public ESEBModelsManagerProviderPort (IModelsManager mm) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), DEFAULT_ESEB_RPCNAME);
        m_modelsManager = mm;
        setupModelConverters (MODEL_CONVERTER_CLASS);
        getConnectionRole ().createRegistryWrapper (IESEBModeslManagerRemoteInterface.class, this,
                IESEBModeslManagerRemoteInterface.class.getSimpleName ());

    }

    @Override
    public Collection<? extends String> getRegisteredModelTypes () {
        return m_modelsManager.getRegisteredModelTypes ();
    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        IModelInstance modelInstance = m_modelsManager.getModelInstance (modelType, modelName);
        return modelInstance;
    }

}
