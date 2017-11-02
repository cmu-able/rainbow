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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.core.ports.DisconnectedRainbowManagementPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBDelegateConnectionPort extends AbstractDelegateConnectionPort {
    private static final Logger LOGGER = Logger.getLogger (ESEBDelegateConnectionPort.class);

    private IDelegateManagementPort m_deploymentPort;

    public ESEBDelegateConnectionPort (RainbowDelegate delegate) throws IOException {
        super (delegate, ESEBProvider.getESEBClientHost (), ESEBProvider
                .getESEBClientPort (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT),
                ChannelT.HEALTH);
        getConnectionRole().addListener (new IESEBListener() {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE: {
                    if (msg.hasProperty (ESEBConstants.TARGET)
                            && m_delegate.getId ().equals (msg.getProperty (ESEBConstants.TARGET))) {
                        m_delegate.disconnectFromMaster ();
                    }
                }
                }
            }
        });
    }


    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws
    RainbowConnectionException {
        /*
         * connectionProperties should contain the following information: 
         * PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT, PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST: 
         *    Information regarding how to connect to the deployment connector on
         *    the delegate. Note that the delegate needs to create a server that gets passed to the master, 
         *    who becomes a client of the this server. 
         * PROPKEY_ESEB_DELEGATE_CONNECTION_PORT: The port of the delegate connection port
         * server, which will be which will be used by the master to reply to this connection port.
         */
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.fillProperties (connectionProperties);
        short deploymentPortNum = ESEBProvider.getESEBClientPort (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT);

        msg.setProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT, Short.toString (deploymentPortNum));
        String host = connectionProperties.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST, Rainbow
                .instance ().getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION, "localhost"));
        msg.setProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST, host);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateID);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_CONNECT_DELEGATE);

        m_deploymentPort = null;

        getConnectionRole().blockingSendAndReceive (msg, new IESEBListener () {
            @Override
            public void receive (RainbowESEBMessage msgRcvd) {
                String reply = (String )msgRcvd.getProperty (ESEBConstants.MSG_CONNECT_REPLY);
                if (!ESEBConstants.MSG_REPLY_OK.equals (reply)) {
                    LOGGER.error (MessageFormat.format (
                            "Delegate {0}: connectDelegate received the following reply: {1}", m_delegate.getId (),
                            reply));
                }
                else {
                    try {
                        m_deploymentPort = RainbowPortFactory.createDelegateDeploymentPort (m_delegate,
                                m_delegate.getId ());
                    }
                    catch (RainbowConnectionException e) {

                    }
                }
            }
        }, Rainbow.instance ().getProperty (Rainbow.PROPKEY_PORT_TIMEOUT, 10000));
        if (m_deploymentPort == null) {
            LOGGER.error ("The call to connectDelegate timed out without returning a deployment port...");
            // REVIEW: Throw an exception instead
            m_deploymentPort = DisconnectedRainbowManagementPort.instance ();
        }
        return m_deploymentPort;
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
        getConnectionRole().publish (msg);
    }

    @Override
    public void dispose () {
        getConnectionRole ().close ();
    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        RainbowESEBMessage esebMsg = getConnectionRole().createMessage ();
        esebMsg.setProperty (ESEBConstants.MSG_CHANNEL_KEY, ChannelT.UIREPORT.name ());
        esebMsg.setProperty (ESEBConstants.COMPONENT_TYPE_KEY, compT.name ());
        esebMsg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
        esebMsg.setProperty (ESEBConstants.REPORT_TYPE_KEY, type.name ());
        esebMsg.setProperty (ESEBConstants.REPORT_MSG_KEY, msg);
        esebMsg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateID);
        getConnectionRole().publish (esebMsg);

    }

    @Override
    public void trace (RainbowComponentT type, String msg) {
        if (LOGGER.isTraceEnabled ()) {
            LOGGER.trace (msg);
        }
    }

}
