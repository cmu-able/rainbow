package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.AbstractMasterManagementPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBMasterSideManagementPort extends AbstractMasterManagementPort implements ESEBManagementPortConstants {
    static Logger         LOGGER = Logger.getLogger (ESEBMasterSideManagementPort.class);


    public ESEBMasterSideManagementPort (RainbowMaster master, String delegateID, Properties connectionProperties) throws IOException {
        // Runs on delegate
        super (master, delegateID, connectionProperties.getProperty (
                ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST,
                Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION)), Short.valueOf (connectionProperties
                .getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                        Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT, "1234"))), ChannelT.HEALTH);
        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String msgType = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                switch (msgType) {
                case REQUEST_CONFIG_INFORMATION: {
                    if (msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY).equals (getDelegateId ())) {
                        requestConfigurationInformation ();
                    }

                }
                break;
                case RECEIVE_HEARTBEAT: {
                    if (msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY).equals (getDelegateId ())) {
                        heartbeat ();
                    }
                }
                }
            }
        });
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.fillProperties (configuration);
        // No response is expected from the client, so don't do any waiting, just send
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, SEND_CONFIGURATION_INFORMATION);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        getConnectionRole().publish (msg);
    }


    class BooleanReply implements IESEBListener {
        boolean m_reply = false;

        @Override
        public void receive (RainbowESEBMessage msg) {
            m_reply = (Boolean )msg.getProperty (ESEBConstants.MSG_REPLY_VALUE);

        }
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, START_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        try {
            BooleanReply reply = new BooleanReply ();
            getConnectionRole().blockingSendAndReceive (msg, reply, 10000);
            return reply.m_reply;
        }
        catch (RainbowConnectionException e) {
            LOGGER.error (MessageFormat.format ("startDelegate did not return for delegate {0}", getDelegateId ()));
            return false;
        }
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, PAUSE_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        try {
            BooleanReply reply = new BooleanReply ();
            getConnectionRole().blockingSendAndReceive (msg, reply, 10000);
            return reply.m_reply;
        }
        catch (RainbowConnectionException e) {
            LOGGER.error (MessageFormat.format ("pauseDelegate did not return for delegate {0}", getDelegateId ()));
            return false;
        }
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, TERMINATE_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        BooleanReply reply = new BooleanReply ();
        getConnectionRole().sendAndReceive (msg, reply);
        return reply.m_reply;
    }

    @Override
    public void dispose () {
        try {
            getConnectionRole().close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat.format ("Could not close the deployment port on the master for {0}",
                    getDelegateId ()));
        }
    }

    @Override
    public void startProbes () throws IllegalStateException {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, START_PROBES);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        getConnectionRole().publish (msg);
    }

    @Override
    public void killProbes () throws IllegalStateException {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, KILL_PROBES);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        getConnectionRole().publish (msg);
    }

}
