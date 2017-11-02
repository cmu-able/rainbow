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
package org.sa.rainbow.core.ports.eseb;


import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

import java.io.IOException;
import java.util.List;

public class ESEBGaugeModelUSBusPort extends AbstractESEBDisposablePort implements IModelUSBusPort, ESEBConstants {

    private Identifiable  m_client;

    public ESEBGaugeModelUSBusPort (Identifiable client) throws IOException {
        this (client, ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());

    }

    private ESEBGaugeModelUSBusPort (Identifiable client, String host, short port) throws IOException {
        super (host, port, ChannelT.MODEL_US);
        m_client = client;
        // Note, there is no communication from the model US bus to the gauges, so there is no need for a listener    
    }

    @Override
    public void updateModel (IRainbowOperation command) {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.MODEL_US*/);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
        ESEBCommandHelper.command2Message (command, msg);
        getConnectionRole().publish (msg);

    }

    @Override
    public void updateModel (List<IRainbowOperation> commands, boolean transaction) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL + "_multi");
        for (int i = 0; i < commands.size (); i++) {
            ESEBCommandHelper.command2Message (commands.get (i), msg, "_" + i + "_");
        }
        try {
            msg.setProperty (ESEBCommandHelper.MSG_TRANSACTION, Boolean.valueOf (transaction));
        }
        catch (RainbowException e) {
        }
        getConnectionRole().publish (msg);

    }

    @Override
    public IModelInstance getModelInstance (ModelReference modelRef) {
        // if we're on the same host as the master, just return the model instance. 
        // Issue: don't want commands to be executable by the gauge - they can only be passed
        // to the bus. 
        if (Rainbow.instance ().isMaster ()) {
            RainbowMaster master = Rainbow.instance ().getRainbowMaster ();
            return master.modelsManager ().getModelInstance (modelRef);
        }
        throw new UnsupportedOperationException (
                "A model instance cannot be retrieved currently if not running in the RainbowMaster.");
    }

}
