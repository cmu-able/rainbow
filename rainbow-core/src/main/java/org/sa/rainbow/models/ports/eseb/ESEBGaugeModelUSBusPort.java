package org.sa.rainbow.models.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;

public class ESEBGaugeModelUSBusPort implements IRainbowModelUSBusPort, ESEBConstants {

    private ESEBConnector m_role;
    private Identifiable  m_client;

    public ESEBGaugeModelUSBusPort (Identifiable client) throws IOException {

        m_client = client;
        String delegateHost = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST,
                "localhost");
        String delegatePort = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
        m_role = new ESEBConnector (delegateHost, port);
        // Note, there is no communication from the model US bus to the gauges, so there is no need for a listener
    }

    @Override
    public void updateModel (IRainbowModelCommandRepresentation command) {
        RainbowESEBMessage msg = m_role.createMessage (ChannelT.MODEL_US);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_client.id ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
        ESEBCommandHelper.command2Message (command, msg);
        m_role.publish (msg);

    }

    @Override
    public void updateModel (List<IRainbowModelCommandRepresentation> commands, boolean transaction) {
        // TODO Auto-generated method stub

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
