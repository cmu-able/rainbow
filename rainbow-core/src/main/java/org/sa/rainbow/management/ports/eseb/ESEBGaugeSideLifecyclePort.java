package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gauges.IGaugeIdentifier;
import org.sa.rainbow.gauges.IGaugeProtocol;
import org.sa.rainbow.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.ChannelT;

public class ESEBGaugeSideLifecyclePort implements IRainbowGaugeLifecycleBusPort {

    private ESEBConnector m_connectionRole;

    public ESEBGaugeSideLifecyclePort () throws IOException {

        String port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        m_connectionRole = new ESEBConnector (Rainbow.properties ().getProperty (
                RainbowConstants.PROPKEY_MASTER_LOCATION), p);

    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = m_connectionRole.createMessage (ChannelT.HEALTH);
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "GAUGE_CREATED");
        m_connectionRole.publish (msg);
    }

    private void setCommonGaugeProperties (RainbowESEBMessage msg, IGaugeIdentifier gauge) {
        msg.setProperty (IGaugeProtocol.ID, gauge.id ());
        msg.setProperty (IGaugeProtocol.GAUGE_NAME, gauge.gaugeDesc ().getName ());
        msg.setProperty (IGaugeProtocol.GAUGE_TYPE, gauge.gaugeDesc ().getType ());
        msg.setProperty (IGaugeProtocol.MODEL_TYPE, gauge.modelDesc ().getType ());
        msg.setProperty (IGaugeProtocol.MODEL_NAME, gauge.modelDesc ().getName ());
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        // TODO Auto-generated method stub
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = m_connectionRole.createMessage (ChannelT.HEALTH);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_HEARTBEAT);
        setCommonGaugeProperties (msg, gauge);
        m_connectionRole.publish (msg);
    }

}
