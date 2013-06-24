package org.sa.rainbow.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.ports.AbstractDelegateDeploymentPort;
import org.sa.rainbow.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBDelegateDeploymentPort extends AbstractDelegateDeploymentPort implements ESEBDeploymentPortConstants {
    static Logger         LOGGER = Logger.getLogger (ESEBDelegateDeploymentPort.class);
    private ESEBConnector m_role;

    public ESEBDelegateDeploymentPort (RainbowDelegate delegate) throws IOException {
        super (delegate);
        String delegatePort = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
        m_role = new ESEBConnector (port);

        m_role.addListener (new IESEBListener () {

            @Override
            public void receive (Map<String, String> msg) {
                String msgType = msg.get (ESEBConstants.MSG_TYPE_KEY);
                String did = msg.get (ESEBConstants.MSG_DELEGATE_ID_KEY);
                if (getDelegateId ().equals (did)) {
                    if (msgType != null) {
                        switch (msgType) {
                        case SEND_CONFIGURATION_INFORMATION:
                            sendConfigurationInformation (m_role.decodeProperties (msg));
                            break;
                        case START_DELEGATE:
                            startDelegate ();
                            break;
                        case TERMINATE_DELEGATE:
                            terminateDelegate ();
                            break;
                        case PAUSE_DELEGATE:
                            pauseDelegate ();
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void receiveHeartbeat () {
        Map<String, String> msg = new HashMap<> ();
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.put (ESEBConstants.MSG_TYPE_KEY, RECEIVE_HEARTBEAT);
        LOGGER.debug (MessageFormat.format ("Delegate {0} sending heartbeat.", getDelegateId ()));
        m_role.publish (msg);
    }

    @Override
    public void requestConfigurationInformation () {
        Map<String, String> msg = new HashMap<> ();
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.put (ESEBConstants.MSG_TYPE_KEY, ESEBDeploymentPortConstants.REQUEST_CONFIG_INFORMATION);
        LOGGER.debug (MessageFormat.format ("Delegate {0} requesting configuration information.", getDelegateId ()));
        m_role.publish (msg);
    }

    @Override
    public void dispose () {
        try {
            m_role.close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat
                    .format ("Could not close the deployment port for delegate {0}", getDelegateId ()));
        }
    }

}
