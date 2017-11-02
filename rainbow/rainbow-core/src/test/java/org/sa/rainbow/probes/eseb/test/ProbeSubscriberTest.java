package org.sa.rainbow.probes.eseb.test;

import java.util.UUID;

import org.junit.Test;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.ESEBProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBProbeSubscriberPortHelper;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;

import auxtestlib.DefaultTCase;
import auxtestlib.TestHelper;

public class ProbeSubscriberTest extends DefaultTCase {

    @TestHelper
    ESEBProbeSubscriberPortHelper m_portHelper;

    @Test
    public void testSubscriptionOneLocation () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", "localhost");
        assertTrue (port.subscribedToProbe ("test", "localhost"));
        assertFalse (port.subscribedToProbe ("test2", "localhost"));
        assertFalse (port.subscribedToProbe ("test", "someOtherHost"));
    }

    @Test
    public void testSubscriptionMultipleLocations () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", "host1");
        port.subscribeToProbe ("test", "host2");
        assertTrue (port.subscribedToProbe ("test", "host1"));
        assertTrue (port.subscribedToProbe ("test", "host2"));
        assertFalse (port.subscribedToProbe ("test", "host3"));
    }

    @Test
    public void testSubscribeAllLocations () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", null);
        assertTrue (port.subscribedToProbe ("test", "host1"));
        assertTrue (port.subscribedToProbe ("test", "host2"));
        assertTrue (port.subscribedToProbe ("test", "host3"));
        assertFalse (port.subscribedToProbe ("test1", "host3"));
    }

    @Test
    public void testUnsubscribeOneLocation () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", "localhost");
        assertTrue (port.subscribedToProbe ("test", "localhost"));
        port.unsubscribeToProbe ("test", "localhost");
        assertFalse (port.subscribedToProbe ("test", "localhost"));
    }

    @Test
    public void testUnsubscribeAllLocations () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", null);
        port.unsubscribeToProbe ("test", null);
        assertFalse (port.subscribedToProbe ("test", "host1"));
        assertFalse (port.subscribedToProbe ("test", "host2"));
        assertFalse (port.subscribedToProbe ("test", "host3"));
    }

    public void testUnsubscribeSomeLocations () {
        ESEBProbeReportSubscriberPort port = m_portHelper.disconnectedPort ();
        port.subscribeToProbe ("test", null);
        port.unsubscribeToProbe ("test", "host1");
        assertTrue (port.subscribedToProbe ("test", "host1"));
        assertFalse (port.subscribedToProbe ("test", "host2"));
        assertFalse (port.subscribedToProbe ("test", "host3"));
    }

    IRainbowMessage createProbeReportMessage (String probeType, String location) {
        IRainbowMessage msg = new RainbowESEBMessage ();
        try {
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_PROBE_REPORT);
            msg.setProperty (ESEBConstants.MSG_PROBE_ID_KEY, UUID.randomUUID ().toString ());
            msg.setProperty (ESEBConstants.MSG_PROBE_TYPE_KEY, probeType);
            msg.setProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY, location);
        }
        catch (RainbowException e) {
            fail ();
        }
        return msg;
    }

}
