/**
 * 
 */
package org.sa.rainbow.core.models.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.core.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.management.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.models.ports.IRainbowModelChangeBusPort;

/**
 * @author schmerl
 *
 */
public class ESEBChangeBusAnnouncePort implements IRainbowModelChangeBusPort {

    private ESEBConnector m_role;

    public ESEBChangeBusAnnouncePort () throws IOException {
        // Runs on master
        String delegateHost = ESEBProvider.getESEBClientHost ();
        Short port = ESEBProvider.getESEBClientPort ();
        m_role = new ESEBConnector (delegateHost, port, ChannelT.MODEL_CHANGE);
    }
    /* (non-Javadoc)
     * @see org.sa.rainbow.models.ports.IRainbowModelChangeBusPort#announce(org.sa.rainbow.core.event.IRainbowMessage)
     */
    @Override
    public void announce (IRainbowMessage event) {
        if (event instanceof RainbowESEBMessage && ChannelT.MODEL_CHANGE.name ().equals (event.getProperty (ESEBConstants.MSG_CHANNEL_KEY))) {
            m_role.publish ((RainbowESEBMessage )event);
        }
        else 
            throw new IllegalArgumentException ("Attempt to pass a non ESEB message to an ESEB connector, or on the wrong channel.");
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.models.ports.IRainbowModelChangeBusPort#announce(java.util.List)
     */
    @Override
    public void announce (List<? extends IRainbowMessage> event) {
        for (IRainbowMessage msg : event) {
            announce (msg);
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.models.ports.IRainbowModelChangeBusPort#createMessage()
     */
    @Override
    public IRainbowMessage createMessage () {
        return m_role.createMessage (/*ChannelT.MODEL_CHANGE*/);
    }

}
