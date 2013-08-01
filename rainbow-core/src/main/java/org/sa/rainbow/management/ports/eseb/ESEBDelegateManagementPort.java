package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.management.ports.AbstractDelegateManagementPort;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBDelegateManagementPort extends AbstractDelegateManagementPort implements ESEBManagementPortConstants {
    static Logger         LOGGER = Logger.getLogger (ESEBDelegateManagementPort.class);
    private ESEBConnector m_role;

    public ESEBDelegateManagementPort (RainbowDelegate delegate) throws IOException {
        super (delegate);
        String delegatePort = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
        m_role = new ESEBConnector (port);

        m_role.addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String msgType = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                String did = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                if (getDelegateId ().equals (did)) {
                    if (msgType != null) {
                        boolean result = false;
                        switch (msgType) {
                        case SEND_CONFIGURATION_INFORMATION:
                            sendConfigurationInformation (msg.pulloutProperties ());
                            break;
                        case START_DELEGATE:
                            result = startDelegate ();
                            m_role.replyToMessage (msg, result);
                            break;
                        case TERMINATE_DELEGATE:
                            result = terminateDelegate ();
                            m_role.replyToMessage (msg, result);
                            break;
                        case PAUSE_DELEGATE:
                            result = pauseDelegate ();
                            m_role.replyToMessage (msg, result);

                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void heartbeat () {
        RainbowESEBMessage msg = m_role.createMessage (ChannelT.HEALTH);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, RECEIVE_HEARTBEAT);
        LOGGER.debug (MessageFormat.format ("Delegate {0} sending heartbeat.", getDelegateId ()));
        m_role.publish (msg);
    }

    @Override
    public void requestConfigurationInformation () {
        RainbowESEBMessage msg = m_role.createMessage (ChannelT.HEALTH);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBManagementPortConstants.REQUEST_CONFIG_INFORMATION);
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
