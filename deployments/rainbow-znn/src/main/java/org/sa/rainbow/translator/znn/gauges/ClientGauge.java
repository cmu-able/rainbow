package org.sa.rainbow.translator.znn.gauges;

import com.google.common.collect.TreeMultiset;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientGauge extends RegularPatternGauge {

    private RequestRater m_requestRater;

    public class NewClientWatcher implements IRainbowModelChangeCallback {

        @Override
        public void onEvent (ModelReference reference, IRainbowMessage message) {
            IModelInstance<?> m = m_modelPort.getModelInstance (reference);
            if (m instanceof AcmeModelInstance) {
                AcmeModelInstance model = (AcmeModelInstance) m;
                String bearer = (String) message.getProperty (AcmeModelOperation.BEARER_PROP);
                try {
                    IAcmeComponent client = model.resolveInModel (bearer, IAcmeComponent.class);
                    if (client.declaresType ("ClientT")) {
                        String ip = (String) message.getProperty (AcmeModelOperation.VALUE_PROP);
                        // Strip out starting and ending quotes
                        ip = ip.replaceAll ("^\"", "");
                        ip = ip.replaceAll ("\"$", "");
                        m_IP2Client.put (ip, client);
                        m_ipsToProcess.remove (ip);
                    }
                } catch (RainbowModelException e) {
                    e.printStackTrace ();
                }
            }
        }

    }

    public static final String NAME = "G - Client Manager";

    private static final String[] commandNames = {"addClient", "removeClient", "setClientRequestRate"};

    private static final String DEFAULT = "CLIENT_REQUEST";

    private static final long RR_INTERVAL = 2000;

    private static SimpleDateFormat SDF = new SimpleDateFormat ("dd/MMM/yyyy:HH:mm:ss Z");

    private IModelsManagerPort m_modelPort;
    private IModelChangeBusSubscriberPort m_modelChanges;

    Map<String, IAcmeComponent> m_IP2Client = new HashMap<> ();
    TreeMap<Long, String> m_lastSeen2IP = new TreeMap<> ();
    Map<String, Long> m_IP2LastSeen = new HashMap<> ();
    Map<String, TreeMultiset<Long>> m_IP2Accesses = new HashMap<> ();

    Set<String> m_ipsToProcess = Collections.synchronizedSet (new HashSet<String> ());

    private long m_clientWindow = 30000; /* 30 seconds */

    private boolean m_someAccess;

    private boolean m_doHousekeeping = true;

    private long m_discardWindow = 1800000; /* 30 minutes */

    private long m_lastRRUpdates = 0;

    private boolean m_housekeeping = false;

    public ClientGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                        List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
            throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (DEFAULT, Pattern.compile ("^([\\d.]+) - - \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}).*$"));

        // Handle setup parameters
        Long window = getSetupValue ("window", Long.class);
        if (window != null) {
            m_clientWindow = window;
        }

        Long discardWindow = getSetupValue ("dsicardAfter", Long.class);
        if (discardWindow != null) {
            m_discardWindow = discardWindow;
        }

        m_modelPort = RainbowPortFactory.createModelsManagerRequirerPort ();
        m_modelChanges = RainbowPortFactory.createModelChangeBusSubscriptionPort ();

        // Register for new clients, so that we can update the maps
        m_modelChanges.subscribe (new IRainbowChangeBusSubscription () {

            @Override
            public boolean matches (IRainbowMessage message) {
                boolean b = message.getPropertyNames ().contains (IModelChangeBusPort.MODEL_NAME_PROP)
                        && message.getProperty (IModelChangeBusPort.MODEL_NAME_PROP).equals (m_modelDesc.getName ());
                if (b) {
                    b = message.getPropertyNames ().contains (IModelChangeBusPort.EVENT_TYPE_PROP)
                            && message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP)
                            .equals (AcmeModelEventType.ADD_PROPERTY.name ());
                    if (b) {
                        b = message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP)
                                && ((String) message.getProperty (AcmeModelOperation.PROPERTY_PROP))
                                .endsWith ("deploymentLocation");
                    }
                }
                return b;
            }
        }, new NewClientWatcher ());
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (DEFAULT.equals (matchName)) {
            String ip = m.group (1);
            String lastSeen = m.group (2);
            try {
                Date lastSeenDate = SDF.parse (lastSeen);
                Date now = new Date ();
                m_someAccess = true;
                if (now.getTime () - m_clientWindow > lastSeenDate.getTime ()) return;
                boolean b = !m_ipsToProcess.contains (ip) && m_IP2Client.get (ip) == null;
                if (b) {
                    // This is from a new client
                    m_ipsToProcess.add (ip);
                    IRainbowOperation cmd = getCommand (commandNames[0]);
                    Map<String, String> pm = new HashMap<> ();
                    pm.put (cmd.getParameters ()[1], ip);
                    issueCommand (cmd, pm);
                }
                // update the last seen data
                m_lastSeen2IP.put (lastSeenDate.getTime (), ip);
                m_IP2LastSeen.put (ip, lastSeenDate.getTime ());
                if (m_requestRater != null)
                    m_requestRater.tick (ip);
//                TreeMultiset<Long> accesses = m_IP2Accesses.get (ip);
//                if (accesses == null) {
//                    accesses = TreeMultiset.create ();
//                    m_IP2Accesses.put (ip, accesses);
//                }
//                accesses.add (lastSeenDate.getTime ());
            } catch (ParseException e) {
                e.printStackTrace ();
            }
        }
    }

    @Override
    protected void runAction () {
        super.runAction ();
//        if (m_someAccess) {
//            m_someAccess = false;
//            doHousekeeping ();
//        }
    }

    void doHousekeeping () {
        synchronized (this) {
            if (m_housekeeping) return;
        }
        m_housekeeping = true;
        cleanOutOldClients ();
//        updateRequestRates ();
        m_housekeeping = false;
    }

    // Request rate calculation has been replaced with RequestRater below
//    private float getRequestRate (TreeMultiset<Long> accesses, long maxInterval, long now) {
//        Iterator<Long> iterator = accesses.iterator ();
//        while (iterator.hasNext ()) {
//            if (now - iterator.next () > maxInterval) {
//                iterator.remove ();
//            }
//        }
//
//        com.google.common.collect.Multiset.Entry<Long> lastEntry = accesses.lastEntry ();
//        if (lastEntry != null) {
//            Long oldest = lastEntry.getElement ();
//
//            long seconds = (maxInterval/*now - maxInterval*/) / 1000;
//
//            float size = accesses.size ();
//            float rr = size / seconds;
//            return rr;
//        } else
//            return 0;
//
//    }
//
//    private void updateRequestRates () {
//        long now = new Date ().getTime ();
//        if (now - m_lastRRUpdates < RR_INTERVAL) return;
//        List<IRainbowOperation> cmds = new LinkedList<> ();
//        List<Map<String, String>> pms = new LinkedList<> ();
//        for (Entry<String, TreeMultiset<Long>> entry : m_IP2Accesses.entrySet ()) {
//            String key = entry.getKey ();
//            IAcmeComponent client = m_IP2Client.get (key);
//            if (client != null) {
//                float reqRate = getRequestRate (entry.getValue (), m_clientWindow, now);
//                String format = MessageFormat.format (
//                        "Client ''{0}'' made {1} requests in the last {2} seconds for a request rate of {3,number,###.######} req/sec",
//                        client.getQualifiedName (), entry.getValue ().size (), m_clientWindow / 1000, reqRate);
//                log (format);
//                System.out.println (format);
//                IRainbowOperation cmd = getCommand (commandNames[2]);
//                Map<String, String> pm = new HashMap<> ();
//                pm.put (cmd.getTarget (), client.getQualifiedName ());
//                pm.put (cmd.getParameters ()[0], Double.toString (reqRate));
//                cmds.add (cmd);
//                pms.add (pm);
//            }
//        }
//        if (!cmds.isEmpty ()) {
//            issueCommands (cmds, pms);
//        }
//        m_lastRRUpdates = new Date ().getTime ();
//    }

    // Removes clients that have not been seen in the last "clientWindow" ms.
    private void cleanOutOldClients () {
        if (m_lastSeen2IP.isEmpty ()) return;
        Date now = new Date ();
        Long oldest = m_lastSeen2IP.firstKey ();
        List<IRainbowOperation> cmds = new LinkedList<> ();
        List<Map<String, String>> pms = new LinkedList<> ();
        while (oldest != null && now.getTime () - oldest > m_discardWindow) {
            String ip = m_lastSeen2IP.get (oldest);
            IAcmeComponent oldClient = m_IP2Client.get (ip);
            if (oldClient != null) {
                IRainbowOperation cmd = getCommand (commandNames[1]);
                cmds.add (cmd);
                Map<String, String> pm = new HashMap<> ();
                pm.put (cmd.getTarget (), oldClient.getQualifiedName ());
                pms.add (pm);

                m_IP2Client.remove (ip);
            }
            m_lastSeen2IP.remove (oldest);
            m_IP2LastSeen.remove (ip);
            oldest = m_lastSeen2IP.firstKey ();
        }
        if (!cmds.isEmpty ()) {
            issueCommands (cmds, pms);
        }
    }

    @Override
    protected void handleConfigParam (TypedAttributeWithValue tav) {
        super.handleConfigParam (tav);

        if (tav.getName ().equals ("windpw")) {
            m_clientWindow = (long) tav.getValue ();
        } else if (tav.getName ().equals ("discardAfter")) {
            m_discardWindow = (long) tav.getValue ();
        }
    }

    @Override
    public void start () {
        super.start ();
        m_doHousekeeping = true;
        Thread housekeep = new Thread (new Runnable () {

            @Override
            public void run () {
                while (m_doHousekeeping) {
                    try {
                        Thread.sleep (m_discardWindow);
                    } catch (InterruptedException e) {
                    }
                    doHousekeeping ();
                }
            }
        });
        housekeep.start ();

        m_requestRater = new RequestRater (m_clientWindow);
        m_requestRater.start ();
    }

    @Override
    public void stop () {
        m_doHousekeeping = false;
        super.stop ();
    }

    @Override
    public void terminate () {
        super.terminate ();
        m_doHousekeeping = false;
    }

    protected class RequestRater extends Thread {


        private long m_sleepTime;

        public RequestRater (long sleepTime) {
            super ();

            m_sleepTime = sleepTime;
        }

        Map<String, Integer> m_requestTracker = new HashMap<> ();
        long m_timestamp = new Date ().getTime ();

        public synchronized void tick (String ip) {
            Integer ticks = m_requestTracker.get (ip);
            if (ticks == null) m_requestTracker.put (ip, 1);
            else m_requestTracker.put (ip, ticks + 1);
        }

        @Override
        public void run () {
            while (!isTerminated () && m_doHousekeeping) {
                try {
                    sleep (m_sleepTime);
                } catch (InterruptedException ignore) {
                }
                Map<String, Double> rrs = markRequestRates ();
                List<IRainbowOperation> ops = new ArrayList<> (rrs.size ());
                List<Map<String, String>> pms = new ArrayList<> (rrs.size ());
                for (Entry<String, Double> e : rrs.entrySet ()) {
                    IAcmeComponent client = m_IP2Client.get (e.getKey ());

                    String format = MessageFormat.format (
                            "Client ''{0}'' has a request rate of {1,number,###.######} req/sec",
                            client.getQualifiedName (), e.getValue ());
                    log (format);
                    IRainbowOperation op = getCommand (commandNames[2]);
                    Map<String, String> pm = new HashMap<> ();
                    pm.put (op.getTarget (), client.getQualifiedName ());
                    pm.put (op.getParameters ()[0], Double.toString (e.getValue ()));
                    ops.add (op);
                    pms.add (pm);
                }
                if (!ops.isEmpty ()) {
                    issueCommands (ops, pms);
                }

            }
        }

        public Map<String, Double> markRequestRates () {
            long from = 0;
            Map<String, Integer> ticks;
            synchronized (this) {
                ticks = m_requestTracker;
                from = m_timestamp;
                m_requestTracker = new HashMap<> ();
                m_timestamp = new Date ().getTime ();
            }
            Map<String, Double> ret = new HashMap<> ();
            String format = "";
            for (Entry<String, Integer> entry : ticks.entrySet ()) {
                double rr = entry.getValue ().doubleValue () / ((m_timestamp - from)) * 1000;

                IAcmeComponent client = m_IP2Client.get (entry.getKey ());
                if (client != null) {
                    format += MessageFormat.format (
                            "Client ''{0}'' made {1} requests in the last {2} seconds for a request rate of {3,number,###.######} req/sec\n",
                            client.getQualifiedName (), entry.getValue (), (m_timestamp - from) / 1000, rr);
                    ret.put (entry.getKey (), rr);
                }
            }
            log (format);
            return ret;
        }


    }

}
