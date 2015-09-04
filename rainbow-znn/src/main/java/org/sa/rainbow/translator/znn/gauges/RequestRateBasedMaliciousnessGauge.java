package org.sa.rainbow.translator.znn.gauges;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.translator.znn.gauges.ClientResponseTimeGauge.ModelBasedSubscription;

public class RequestRateBasedMaliciousnessGauge extends AbstractGauge {

    /** Matches a setClientRequestRate operation ending on the model manager **/
    public class RequestRatePropertySubscription extends ModelBasedSubscription {

        public RequestRatePropertySubscription (String modelName, String modelType) {
            super (modelName, modelType);
        }

        @Override
        public boolean matches (IRainbowMessage message) {
            boolean b = super.matches (message);
            String type = (String )message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                try {
                    CommandEventT ct = CommandEventT.valueOf (type);
                    b &= ct.isEnd ()
                            && message.getProperty (IModelChangeBusPort.COMMAND_PROP).equals ("setClientRequestRate");
                    return b;
                }
                catch (Exception e) {
                }
            }
            return false;
        }

    }

    private static final String NAME = "G - RR Based Maliciousness";

    private Map<String, Float> m_maliciousnessMap     = new HashMap<> ();
    private float              m_maliciousRequestRate = 10.0f;
    private String             m_reqRateProperty      = "reqRate";

    private IModelChangeBusSubscriberPort m_modelChanges;

    static final String[] commandNames = { "setMaliciousness" };

    /* Both these queues should be synchronized */
    Queue<IRainbowOperation>   m_ops        = new LinkedList<> ();
    Queue<Map<String, String>> m_parameters = new LinkedList<> ();

    public RequestRateBasedMaliciousnessGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings) throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_modelChanges = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChanges.subscribe (new RequestRatePropertySubscription (modelDesc.getName (), modelDesc.getType ()),
                new ProcessRequestRate ());
    }

    @Override
    protected void handleConfigParam (TypedAttributeWithValue cfp) {
        super.handleConfigParam (cfp);
        if ("maliciousRequestRate".equals (cfp.getName ())) {
            m_maliciousRequestRate = (Float )cfp.getValue ();
        }
        if ("requestRateProperty".equals (cfp.getName ())) {
            m_reqRateProperty = (String )cfp.getValue ();
        }
    }

    @Override
    protected void runAction () {
        super.runAction ();
        IRainbowOperation cmd = null;
        Map<String, String> params = null;
        synchronized (m_ops) {
            cmd = m_ops.poll ();
            if (cmd != null) {
                params = m_parameters.poll ();
            }
        }
        if (cmd != null) {
            issueCommand (cmd, params);
        }
    }

    // This gauge listens to changes to the property request rate on clients
    public class ProcessRequestRate implements IRainbowModelChangeCallback {

        @Override
        public void onEvent (ModelReference reference, IRainbowMessage message) {
            if (!isTerminated ()) {
                String client = (String )message.getProperty (IModelChangeBusPort.TARGET_PROP);
                String reqRateString = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
                Float reqRate = Float.valueOf (reqRateString);

                Float currentMaliciousness = m_maliciousnessMap.get (client);
                if (currentMaliciousness == null) {
                    // set the current value to the opposite of what it should be to force a command
                    currentMaliciousness = isMaliciousRate (reqRate) ? 0f : 1f;
                    m_maliciousnessMap.put (client, currentMaliciousness);
                }

                IRainbowOperation cmd = getCommand (commandNames[0]);
                Map<String, String> pm = new HashMap<> ();
                pm.put (cmd.getTarget (), client);

                if (isMaliciousRate (reqRate) && currentMaliciousness != 1) {
                    // setMaliciousness (1.0)
                    currentMaliciousness = 1.0f;
                }
                else {
                    // setMaliciousness (0)
                    currentMaliciousness = 0f;
                }
                if (!m_maliciousnessMap.get (client).equals (currentMaliciousness)) {
                    pm.put (cmd.getParameters ()[0], currentMaliciousness.toString ());
                    m_maliciousnessMap.put (client, currentMaliciousness);
                    synchronized (m_ops) {
                        m_ops.offer (cmd);
                        m_parameters.offer (pm);
                    }
                }
            }
        }

    }

    boolean isMaliciousRate (Float reqRate) {
        return reqRate >= m_maliciousRequestRate;
    }
}
