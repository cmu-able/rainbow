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

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.ports.AbstractDelegateManagementPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

import java.io.IOException;
import java.text.MessageFormat;

public class ESEBDelegateManagementPort extends AbstractDelegateManagementPort implements ESEBManagementPortConstants {
    private static final Logger LOGGER = Logger.getLogger (ESEBDelegateManagementPort.class);

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
                        boolean result;
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
        getConnectionRole ().close ();
    }

}
