package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.Collection;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBModelsManagerRequirerPort extends AbstractESEBDisposableRPCPort implements
IESEBModeslManagerRemoteInterface {

    private IESEBModeslManagerRemoteInterface m_stub;

    public ESEBModelsManagerRequirerPort () throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), "rainbow_models_manager");
        setupModelConverters ("org.sa.rainbow.model.converter.eseb.class");
        m_stub = getConnectionRole ().createRemoteStub (IESEBModeslManagerRemoteInterface.class,
                IESEBModeslManagerRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Collection<? extends String> getRegisteredModelTypes () {
        return m_stub.getRegisteredModelTypes ();
    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        return m_stub.getModelInstance (modelType, modelName);
    }

}
