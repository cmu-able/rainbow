package org.sa.rainbow.translator.znn.gauges;

import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeModelOperation;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gauge monitors Apache log information for the %D log entry (response time in microseconds)
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class ClientResponseTimeGauge extends RegularPatternGauge {

    /**
     * Forgets any information about clients that are removed from the system
     *
     */
    public class ForgetDeletedClient implements IRainbowModelChangeCallback {

        @Override
        public void onEvent (ModelReference reference, IRainbowMessage message) {
            synchronized (ClientResponseTimeGauge.this) {
                String client = (String )message.getProperty (IModelChangeBusPort.TARGET_PROP);
                Iterator<Entry<String, String>> iterator = m_IP2ClientName.entrySet ().iterator ();
                boolean found = false;
                while (iterator.hasNext () && !found) {
                    Entry<String, String> next = iterator.next ();
                    if (next.getValue ().equals (client)) {
                        iterator.remove ();
                        found = true;
                    }
                }
            }
        }

    }

    /**
     * 
     * Issues response time reports for new clients. This could happen, for example, when a client is created based on a
     * log entry that has also been read by this gauge.
     */
    public class ReportResponseForNewClient implements IRainbowModelChangeCallback {

        @Override
        public void onEvent (ModelReference reference, IRainbowMessage message) {
            synchronized (ClientResponseTimeGauge.this) {
                String client = (String )message.getProperty (AcmeModelOperation.BEARER_PROP);
                // Ignore events that aren't associated with clients
                if (!client.contains ("Client")) return;
                String ip = (String )message.getProperty (AcmeModelOperation.VALUE_PROP);
                // Remove trailing and leading "
                ip = ip.replaceAll ("^\"", "");
                ip = ip.replaceAll ("\"$", "");
                if (ip != null) {
                    m_IP2ClientName.put (ip, client);
                    issueResponseTimeCommand (client, m_historyMap.get (ip), m_cumulationMap.get (ip), true);
                }
            }
        }

    }

    /** A subscription that matches any event in a given model **/
    public static class ModelBasedSubscription implements IRainbowChangeBusSubscription {

        private String m_modelName;
        private String m_modelType;

        public ModelBasedSubscription (String modelName, String modelType) {
            m_modelName = modelName;
            m_modelType = modelType;
        }

        @Override
        public boolean matches (IRainbowMessage message) {
            boolean b = message.getPropertyNames ().contains (IModelChangeBusPort.MODEL_NAME_PROP)
                    && message.getProperty (IModelChangeBusPort.MODEL_NAME_PROP).equals (m_modelName);
            b &= message.getPropertyNames ().contains (IModelChangeBusPort.MODEL_TYPE_PROP)
                    && message.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP).equals (m_modelType);
            return b;
        }
    }

    public class MatchAddClientSubscription extends ModelBasedSubscription {

        public MatchAddClientSubscription (String modelName, String modelType) {
            super (modelName, modelType);
        }

        /**
         * Match on ADD_PROPERTY of deploymentLocation, meaning that a client has been created
         */
        @Override
        public boolean matches (IRainbowMessage message) {
            boolean b = super.matches (message);
            String type = (String )message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            b &= AcmeModelEventType.ADD_PROPERTY.name ().equals (type)
                    && message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP)
                    && ((String )message.getProperty (AcmeModelOperation.PROPERTY_PROP))
                    .endsWith ("deploymentLocation");
            return b;
        }

    }

    public class MatchDeleteClientSubscription extends ModelBasedSubscription {

        public MatchDeleteClientSubscription (String modelName, String modelType) {
            super (modelName, modelType);
        }

        @Override
        public boolean matches (IRainbowMessage message) {
            boolean b = super.matches (message);
            String type = (String )message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                try {
                    CommandEventT ct = CommandEventT.valueOf (type);
                    b &= ct.isEnd () && message.getProperty (IModelChangeBusPort.COMMAND_PROP).equals ("deleteClient");
                    return b;
                }
                catch (Exception e) {
                }
            }
            return false;

        }

    }

    public static String        NAME              = "G - Client Response Time";
    private static final String DEFAULT           = "CLIENT_REQUEST";
    private static final int    AVG_SAMPLE_WINDOW = 5;
    private static final int    INTERVAL          = 2000;

    private static final String[] commandNames = { "responseTime" };

    private IModelChangeBusSubscriberPort m_modelChanges;

    Map<String, String>        m_IP2ClientName = new HashMap<> ();
    Map<String, Queue<Double>> m_historyMap    = new HashMap<> ();
    Map<String, Double>        m_cumulationMap = new HashMap<> ();
    Map<String, Long>          m_lastReport    = new HashMap<> ();

    final Queue<IRainbowOperation> m_ops = new LinkedList<> ();
    Queue<Map<String, String>> m_params = new LinkedList<> ();

    public ClientResponseTimeGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT,
                Pattern.compile ("^([\\d.]+) - - \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" \\d{3}.*\\s(\\d+)$"));

        m_modelChanges = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChanges.subscribe (new MatchAddClientSubscription (m_modelDesc.getName (), m_modelDesc.getType ()),
                new ReportResponseForNewClient ());
        m_modelChanges.subscribe (new MatchDeleteClientSubscription (m_modelDesc.getName (), m_modelDesc.getType ()),
                new ForgetDeletedClient ());
    }

    @Override
    protected synchronized void doMatch (String matchName, Matcher m) {
        if (DEFAULT.equals (matchName)) {
            String ip = m.group (1);
            Long last = m_lastReport.get (ip);
            if (last == null || new Date ().getTime () - last > INTERVAL) {
                String rtMicro = m.group (4);
                Double rtms = Double.parseDouble (rtMicro) / 1000; // response time in ms
                if (!m_historyMap.containsKey (ip)) {
                    m_historyMap.put (ip, new LinkedList<Double> ());
                    m_cumulationMap.put (ip, 0.0);
                }
                Queue<Double> history = m_historyMap.get (ip);
                double cumulation = m_cumulationMap.get (ip);

                // add value to cumulation and enquue
                cumulation += rtms;
                history.offer (rtms);
                if (history.size () > AVG_SAMPLE_WINDOW) {
                    // if queue size reached window size then dequeue and delete olderst value and report average
                    cumulation -= history.poll ();
                }
                m_cumulationMap.put (ip, cumulation);
                String client = m_IP2ClientName.get (ip);
                if (client != null) {
                    // update client with latency
                    issueResponseTimeCommand (client, history, cumulation, false);
                }
                else {
                    // Client has not yet been registered with the model. Defer this to a component creation end
                }
                m_lastReport.put (ip, new Date ().getTime ());
            }

        }
    }

    @Override
    protected void runAction () {
        IRainbowOperation cmd = null;
        Map<String, String> params = null;
        synchronized (m_ops) {
            cmd = m_ops.poll ();
            if (cmd != null) {
                params = m_params.poll ();
            }
        }
        if (cmd != null) {
            issueCommand (cmd, params);
        }
        super.runAction ();
    }

    void issueResponseTimeCommand (String client, Queue<Double> history, double cumulation, boolean enqueue) {
        double avgRTms = cumulation / history.size ();

        m_reportingPort.trace (getComponentType (),
                MessageFormat.format ("{0}: {1}, hist{2}", id (), cumulation, Arrays.toString (history.toArray ())));
        IRainbowOperation command = getCommand (commandNames[0]);
        Map<String, String> pm = new HashMap<> ();
        pm.put (command.getTarget (), client);
        pm.put (command.getParameters ()[0], Double.toString (avgRTms));
        if (enqueue) {
            synchronized (m_ops) {
                m_ops.offer (command);
                m_params.offer (pm);
            }
        }
        else {
            issueCommand (command, pm);
        }
    }

}
