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
package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import java.io.IOException;
import java.util.Collection;

public class ESEBModelsManagerRequirerPort extends AbstractESEBDisposableRPCPort implements
IESEBModelsManagerRemoteInterface {

    private IESEBModelsManagerRemoteInterface m_stub;

    public ESEBModelsManagerRequirerPort () throws IOException, ParticipantException {
        this (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());
    }

    private ESEBModelsManagerRequirerPort (String host, short port) throws IOException, ParticipantException {
        super (host, port, DEFAULT_ESEB_RPCNAME);
        setupModelConverters (MODEL_CONVERTER_CLASS);
        m_stub = getConnectionRole ().createRemoteStub (IESEBModelsManagerRemoteInterface.class,
                IESEBModelsManagerRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Collection<? extends String> getRegisteredModelTypes () {
        return m_stub.getRegisteredModelTypes ();
    }

    @Override
    public IModelInstance getModelInstance (ModelReference modelRef) {
        return m_stub.getModelInstance (modelRef);
    }
    
    @Override
    public boolean isModelLocked(ModelReference modelRef) {
    	return m_stub.isModelLocked(modelRef);
    }

}
