package org.sa.rainbow.core.management.ports.eseb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class ESEBReceiverSideGaugeLifecyclePort implements IRainbowGaugeLifecycleBusPort {

    private IRainbowGaugeLifecycleBusPort m_manager;
    private ESEBConnector                 m_connection;

    class MessageGaugeIdentifier implements IGaugeIdentifier {

        private IRainbowMessage m_msg;

        public MessageGaugeIdentifier (IRainbowMessage msg) {
            m_msg = msg;
        }

        @Override
        public String id () {
            return (String )m_msg.getProperty (IGaugeProtocol.ID);
        }

        @Override
        public TypedAttribute gaugeDesc () {
            return new TypedAttribute ((String )m_msg.getProperty (IGaugeProtocol.GAUGE_NAME),
                    (String )m_msg.getProperty (IGaugeProtocol.GAUGE_TYPE));
        }

        @Override
        public TypedAttribute modelDesc () {
            return new TypedAttribute ((String )m_msg.getProperty (IGaugeProtocol.MODEL_NAME),
                    (String )m_msg.getProperty (IGaugeProtocol.MODEL_TYPE));
        }

    }

    public ESEBReceiverSideGaugeLifecyclePort (IRainbowGaugeLifecycleBusPort manager) throws IOException {
        m_manager = manager;
        m_connection = new ESEBConnector (
                ESEBProvider.getESEBClientPort (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT), ChannelT.HEALTH);
        m_connection.addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                MessageGaugeIdentifier mgi;
                switch (type) {
                case IGaugeProtocol.GAUGE_CREATED:
                    mgi = new MessageGaugeIdentifier (msg);
                    reportCreated (mgi);
                    break;
                case IGaugeProtocol.GAUGE_DELETED:
                    mgi = new MessageGaugeIdentifier (msg);
                    reportDeleted (mgi);
                    break;
                case IGaugeProtocol.GAUGE_HEARTBEAT:
                    mgi = new MessageGaugeIdentifier (msg);
                    sendBeacon (mgi);
                    break;
                case IGaugeProtocol.GAUGE_CONFIGURED: {
                    mgi = new MessageGaugeIdentifier (msg);
                    List<TypedAttributeWithValue> params = new LinkedList<> ();

                    int i = 0;
                    do {
                        String name = (String )msg.getProperty (IGaugeProtocol.CONFIG_PARAM_NAME + i);
                        if (name == null) {
                            break;
                        }
                        String t = (String )msg.getProperty (IGaugeProtocol.CONFIG_PARAM_TYPE + i);
                        Object v = msg.getProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i);
                        i++;
                        params.add (new TypedAttributeWithValue (name, t, v));
                    } while (true);
                    reportConfigured (mgi, params);
                }
                break;
                }

            }
        });
    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        m_manager.reportCreated (gauge);
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        m_manager.reportDeleted (gauge);
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        m_manager.reportConfigured (gauge, configParams);
    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {
        m_manager.sendBeacon (gauge);
    }

}
