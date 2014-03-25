package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

public class ESEBGaugeModelUSBusPort implements IModelUSBusPort, ESEBConstants {

    private ESEBConnector m_role;
    private Identifiable  m_client;

    public ESEBGaugeModelUSBusPort (Identifiable client) throws IOException {
        this (client, ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());

    }

    public ESEBGaugeModelUSBusPort (Identifiable client, String host, short port) throws IOException {
        m_client = client;
        m_role = new ESEBConnector (host, port, ChannelT.MODEL_US);
        // Note, there is no communication from the model US bus to the gauges, so there is no need for a listener    
    }

    @Override
    public void updateModel (IRainbowOperation command) {
        RainbowESEBMessage msg = m_role.createMessage (/*ChannelT.MODEL_US*/);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
        ESEBCommandHelper.command2Message (command, msg);
        m_role.publish (msg);

    }

    @Override
    public void updateModel (List<IRainbowOperation> commands, boolean transaction) {
        RainbowESEBMessage msg = m_role.createMessage ();
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
        m_role.publish (msg);

    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        // if we're on the same host as the master, just return the model instance. 
        // Issue: don't want commands to be executable by the gauge - they can only be passed
        // to the bus. 
        if (Rainbow.isMaster ()) {
            RainbowMaster master = Rainbow.instance ().getRainbowMaster ();
            return master.modelsManager ().getModelInstance (modelType, modelName);
        }
        throw new UnsupportedOperationException (
                "A model instance cannot be retrieved currently if not running in the RainbowMaster.");
    }

}
