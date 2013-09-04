package org.sa.rainbow.core.models.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.core.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.management.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ports.IRainbowModelChangeBusPort;

public class ESEBModelChangeBusAnnouncePort implements IRainbowModelChangeBusPort {

    private ESEBConnector m_role;

    public ESEBModelChangeBusAnnouncePort (IModelsManager modelsManager) throws IOException {
        // Runs on master
        String delegateHost = ESEBProvider.getESEBClientHost ();
        String delegatePort = Rainbow.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = ESEBProvider.getESEBClientPort ();
        m_role = new ESEBConnector (delegateHost, port, ChannelT.MODEL_CHANGE);
    }

    @Override
    public void announce (IRainbowMessage event) {
        if (event instanceof RainbowESEBMessage) {
            m_role.publish ((RainbowESEBMessage )event);
        }
        else
            throw new IllegalArgumentException ("Cannot pass a non ESEB Rainbow message to an ESEB port");
    }

    @Override
    public void announce (List<? extends IRainbowMessage> event) {
        for (IRainbowMessage msg : event) {
            announce (msg);
        }
    }

    @Override
    public IRainbowMessage createMessage () {
        return new RainbowESEBMessage ();
    }

}
