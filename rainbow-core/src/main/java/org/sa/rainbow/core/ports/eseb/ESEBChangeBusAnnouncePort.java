/**
 * 
 */
package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

/**
 * @author schmerl
 *
 */
public class ESEBChangeBusAnnouncePort extends AbstractESEBDisposablePort implements IModelChangeBusPort {


    public ESEBChangeBusAnnouncePort () throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.MODEL_CHANGE);
    }
    /* (non-Javadoc)
     * @see org.sa.rainbow.models.ports.IRainbowModelChangeBusPort#announce(org.sa.rainbow.core.event.IRainbowMessage)
     */
    @Override
    public void announce (IRainbowMessage event) {
        if (event instanceof RainbowESEBMessage && ChannelT.MODEL_CHANGE.name ().equals (event.getProperty (ESEBConstants.MSG_CHANNEL_KEY))) {
            getConnectionRole().publish ((RainbowESEBMessage )event);
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
        return getConnectionRole().createMessage (/*ChannelT.MODEL_CHANGE*/);
    }

}
