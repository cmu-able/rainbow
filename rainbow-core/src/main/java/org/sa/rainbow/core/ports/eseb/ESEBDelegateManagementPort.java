package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.ports.AbstractDelegateManagementPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBDelegateManagementPort extends AbstractDelegateManagementPort implements ESEBManagementPortConstants {
    static Logger         LOGGER = Logger.getLogger (ESEBDelegateManagementPort.class);

    public ESEBDelegateManagementPort (RainbowDelegate delegate) throws IOException {
        // Runs on delegate
        super (delegate, ESEBProvider.getESEBClientHost (), ESEBProvider
                .getESEBClientPort (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT), ChannelT.HEALTH);

        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String msgType = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                String did = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                if (getDelegateId ().equals (did)) {
                    if (msgType != null) {
                        boolean result = false;
                        switch (msgType) {
/*                        case SEND_CONFIGURATION_INFORMATION:
                            sendConfigurationInformation (msg.pulloutProperties ());
                            break;*/
                        case START_DELEGATE:
                            result = startDelegate ();
                            getConnectionRole().replyToMessage (msg, result);
                            break;
                        case TERMINATE_DELEGATE:
                            result = terminateDelegate ();
                            getConnectionRole().replyToMessage (msg, result);
                            break;
                        case PAUSE_DELEGATE:
                            result = pauseDelegate ();
                            getConnectionRole().replyToMessage (msg, result);
                        case START_PROBES:
                            startProbes ();
                            break;
                        case KILL_PROBES:
                            killProbes ();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void heartbeat () {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, RECEIVE_HEARTBEAT);
        LOGGER.debug (MessageFormat.format ("Delegate {0} sending heartbeat.", getDelegateId ()));
        getConnectionRole().publish (msg);
    }

    @Override
    public void requestConfigurationInformation () {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBManagementPortConstants.REQUEST_CONFIG_INFORMATION);
        LOGGER.debug (MessageFormat.format ("Delegate {0} requesting configuration information.", getDelegateId ()));
        getConnectionRole().publish (msg);
    }

    @Override
    public void dispose () {
        try {
            getConnectionRole().close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat
                    .format ("Could not close the deployment port for delegate {0}", getDelegateId ()));
        }
    }

}
