package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbe;

public class ESEBProbeLifecyclePort implements IProbeLifecyclePort {

    private IProbe m_probe;
    private ESEBConnector m_connectionRole;

    public ESEBProbeLifecyclePort (IProbe probe) throws IOException {
        m_probe = probe;
        // All these messages go on the HEALTH channel. Runs on master
        m_connectionRole = new ESEBConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.HEALTH);
    }

    @Override
    public void reportCreated () {
        RainbowESEBMessage msg = m_connectionRole.createMessage (/*ChannelT.HEALTH*/);
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CREATED);
        m_connectionRole.publish (msg);
    }

    private void setCommonGaugeProperties (RainbowESEBMessage msg) {
        msg.setProperty (IProbeLifecyclePort.PROBE_ID, m_probe.id ());
        msg.setProperty (IProbeLifecyclePort.PROBE_LOCATION, m_probe.location ());
        msg.setProperty (IProbeLifecyclePort.PROBE_NAME, m_probe.name ());
    }

    @Override
    public void reportDeleted () {
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DELETED);
        m_connectionRole.publish (msg);
    }

    @Override
    public void reportConfigured (Map<String, Object> configParams) {
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CONFIGURED);
        int i = 0;
        for (Entry<String, Object> e : configParams.entrySet ()) {
            try {
                msg.setProperty (IProbeLifecyclePort.CONFIG_PARAM_NAME + i, e.getKey ());
                msg.setProperty (IProbeLifecyclePort.CONFIG_PARAM_VALUE + i, e.getValue ());
                i++;
            }
            catch (RainbowException e1) {
                e1.printStackTrace ();
            }
        }
        m_connectionRole.publish (msg);
    }

    @Override
    public void reportDeactivated () {
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DEACTIVATED);
        m_connectionRole.publish (msg);

    }

    @Override
    public void reportActivated () {
        RainbowESEBMessage msg = m_connectionRole.createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_ACTIVATED);
        m_connectionRole.publish (msg);

    }


}
