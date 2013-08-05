/**
 * 
 */
package org.sa.rainbow.models.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

/**
 * @author schmerl
 *
 */
public class ESEBChangeBusAnnouncePort implements IRainbowModelChangeBusPort {

    private ESEBConnector m_role;

    public ESEBChangeBusAnnouncePort () throws IOException {
        String delegateHost = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST,
                "localhost");
        String delegatePort = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
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
    public void announce (List<IRainbowMessage> event) {
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
