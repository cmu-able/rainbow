package org.sa.rainbow.core.management.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class ESEBGaugeSideLifecyclePort implements IRainbowGaugeLifecycleBusPort {

    private ESEBConnector m_connectionRole;

    public ESEBGaugeSideLifecyclePort () throws IOException {

        // All these messages go on the HEALTH channel. Runs on master
        m_connectionRole = new ESEBConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.HEALTH);

    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = m_connectionRole.createMessage (/*ChannelT.HEALTH*/);
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CREATED);
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
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_DELETED);
        m_connectionRole.publish (msg);
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CONFIGURED);
        int i = 0;
        for (TypedAttributeWithValue tav : configParams) {
            msg.setProperty (IGaugeProtocol.CONFIG_PARAM_NAME + i, tav.getName ());
            msg.setProperty (IGaugeProtocol.CONFIG_PARAM_TYPE + i, tav.getType ());
            try {
                msg.setProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i, tav.getValue ());
            }
            catch (RainbowException e) {
                msg.setProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i, "unknown");
            }
            i++;
        }
        m_connectionRole.publish (msg);
    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = m_connectionRole.createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_HEARTBEAT);
        setCommonGaugeProperties (msg, gauge);
        m_connectionRole.publish (msg);
    }

}
