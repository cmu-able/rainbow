package org.sa.rainbow.core.ports.guava;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class GuavaProbeReportingPortingPortSubscriber implements IProbeReportSubscriberPort {

	private IProbeReportPort m_callback;
	private GuavaEventConnector m_eventBus;
	private final Map<String,Set<String>> m_subscriptions = new HashMap();

	public GuavaProbeReportingPortingPortSubscriber(IProbeReportPort callback) {
		m_callback = callback;
		m_eventBus = new GuavaEventConnector(ChannelT.SYSTEM_US);
		m_eventBus.addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage m) {
				processMessage (m);
			}
		});
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeToProbe(String probeType, String location) {
        synchronized (m_subscriptions) {
            Set<String> locations = m_subscriptions .get (probeType);
            if (locations == null) {
                locations = new HashSet<> ();
                locations.add (location == null ? "*" : location);
                m_subscriptions.put (probeType, locations);
            }
            else {
                if (locations.contains ("*") && location != null) {
                    locations.remove (location);
                }
                else if (!locations.contains ("*") && location == null) {
                    locations.clear ();
                    locations.add ("*");
                }
                else {
                    locations.add (location == null ? "*" : location);
                }
            }
        }
	}

	@Override
	public void unsubscribeToProbe(String probeType, String location) {
        synchronized (m_subscriptions) {
            if (location == null) {
                m_subscriptions.remove (probeType);
            }
            else {
                Set<String> locations = m_subscriptions.get (probeType);
                if (locations.contains ("*")) {
                    locations.add (location);
                }
                else {
                    locations.remove (location);
                }
            }
        }
	}
	
	  private void processMessage (final GuavaRainbowMessage msg) {
	        String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
	        if (ESEBConstants.MSG_TYPE_PROBE_REPORT.equals (type)) {
	            final String probeType = (String )msg.getProperty (ESEBConstants.MSG_PROBE_TYPE_KEY);
	            final String probeLocation = (String )msg.getProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY);
	            boolean subscribedToMessage = subscribedToProbe (probeType, probeLocation);
	            if (subscribedToMessage) {
	                m_callback.reportData (new IProbeIdentifier () {


	                    @Override
	                    public String id () {
	                        return (String )msg.getProperty (ESEBConstants.MSG_PROBE_ID_KEY);
	                    }


	                    @Override
	                    public String type () {
	                        return probeType;
	                    }


	                    @Override
	                    public String name () {
	                        return id ();
	                    }


	                    @Override
	                    public String location () {
	                        return probeLocation;
	                    }

	                }, (String )msg.getProperty (ESEBConstants.MSG_DATA_KEY));
	            }
	        }
	    }

	    public boolean subscribedToProbe (String probeType, String probeLocation) {
	        Set<String> locationsInterestedIn;
	        synchronized (m_subscriptions) {
	            locationsInterestedIn = m_subscriptions.get (probeType);
	        }
	        if (locationsInterestedIn == null) return false;
	        boolean subscribed = false;
	        if (locationsInterestedIn.contains ("*") && !locationsInterestedIn.contains (probeLocation)) {
	            subscribed = true;
	        }
	        else if (locationsInterestedIn.contains (probeLocation)) {
	            subscribed = true;
	        }
	        return subscribed;
	    }

}
