package org.sa.rainbow.evaluator.znn;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.core.resource.IAcmeLanguageHelper;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.acme.rule.AcmeSet;
import org.acmestudio.acme.rule.node.IExpressionNode;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.model.acme.AcmeModelOperation;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;
import org.sa.rainbow.model.acme.znn.commands.SetSystemPropertiesCmd;
import org.sa.rainbow.translator.znn.gauges.ClientResponseTimeGauge;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by schmerl on 9/10/2015.
 * <p/>
 * Records the overall state of the system as system level properties. Mostly used for tracing.
 */
public class ZNNStateAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis, IModelChangeBusSubscriberPort.IRainbowModelChangeCallback {

    public static final String NAME = "ZNN State Evaluator";
    public static final String OPERATION_NAME = "setSystemProperties";

    // These match some of the statements in the stitch conditions
    public static final String CLIENTS_SELECTION = "/self/components:!ClientT"; // All clients
    public static final String MALICIOUS_CLIENTS_SELECTION = "/self/components:!PotentiallyMaliciousT[maliciousness>self.MALICIOUS_THRESHOLD]"; // All malicious clients
    public static final String ABOVE_MALICIOUSNESS = "aboveMaliciousThreshold (self)"; // #malicious clients exceeds threshold
    public static final String HIGH_RT = "exists c :! ClientT in self.components | (c.experRespTime > self.MAX_RESPTIME)"; // Is a client getting bad response

    private IModelChangeBusSubscriberPort m_modelChangePort;
    private IModelUSBusPort m_modelUSPort;
    private IModelsManagerPort m_modelsManagerPort;

    /**
     * Match if a client's maliciousness or experienced response time changes
     */
    private IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription m_modelChangeSubscriber = new ClientResponseTimeGauge.ModelBasedSubscription ("ZNewsSys", "Acme") {
        @Override
        public boolean matches (IRainbowMessage message) {
            boolean b = super.matches (message);
            String type = (String) message
                    .getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                return b && ((AcmeModelEventType.ADD_PROPERTY.name ().equals (type)
                        && message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP)
                        && ((String) message.getProperty (AcmeModelOperation.PROPERTY_PROP))
                        .endsWith ("maliciousness")) ||
                        (AcmeModelEventType.ADD_PROPERTY.name ().equals (type)
                                && message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP)
                                && ((String) message.getProperty (AcmeModelOperation.PROPERTY_PROP))
                                .endsWith ("experRespTime")) ||
                        (AcmeModelEventType.SET_PROPERTY_VALUE.name ().equals (type) && message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP) && ((String) message.getProperty (AcmeModelOperation.PROPERTY_PROP)).endsWith ("maliciousness"))
                        || (AcmeModelEventType.SET_PROPERTY_VALUE.name ().equals (type) && message.getPropertyNames ().contains (AcmeModelOperation.PROPERTY_PROP) && ((String) message.getProperty (AcmeModelOperation.PROPERTY_PROP)).endsWith ("experRespTime"))
                );
            }
            return false;
        }
    };

    private final LinkedBlockingQueue<ZNNModelUpdateOperatorsImpl> m_modelCheckQ = new LinkedBlockingQueue<> ();
    private IExpressionNode m_clientsSelection;
    private IExpressionNode m_maliciousSelection;
    private Map<String, String> properties = new HashMap<> ();
    private IExpressionNode m_aboveRT;
    private IExpressionNode m_aboveMal;

    public ZNNStateAnalyzer () {
        super (NAME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        } else {
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        initializeSubscriptions ();
        initializeAcmeExpressions ();
    }

    private void initializeAcmeExpressions () {
        IAcmeLanguageHelper lp = StandaloneLanguagePackHelper.defaultLanguageHelper ();
        try {
            m_clientsSelection = lp.designRuleExpressionFromString (CLIENTS_SELECTION, new RegionManager ());
            m_maliciousSelection = lp.designRuleExpressionFromString (MALICIOUS_CLIENTS_SELECTION, new RegionManager ());
            m_aboveRT = lp.designRuleExpressionFromString (HIGH_RT, new RegionManager ());
            m_aboveMal = lp.designRuleExpressionFromString (ABOVE_MALICIOUSNESS, new RegionManager ());
        } catch (Exception e) {
            m_reportingPort.error (RainbowComponentT.ANALYSIS, "Failed to parse selection expressions", e);
        }

    }

    private void initializeSubscriptions () {
        m_modelChangePort.subscribe (m_modelChangeSubscriber, this);
    }

    private void initializeConnections () throws RainbowConnectionException {
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
        m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, txt);
    }

    @Override
    protected void runAction () {
        final ZNNModelUpdateOperatorsImpl model = m_modelCheckQ.poll ();
        if (model != null) {
            Stack<AcmeError> errors = new Stack<> ();
            IAcmeSystem system = model.getModelInstance ();

            if (m_clientsSelection != null && m_maliciousSelection != null) {
                NodeScopeLookup lookup = new NodeScopeLookup ();
                try {
                    AcmeSet clients = RuleTypeChecker.evaluateAsSet (system, null, m_clientsSelection, errors, lookup);

//                    AcmeSet maliciousClients = RuleTypeChecker.evaluateAsSet (system, null, m_maliciousSelection, errors, lookup);
                    boolean aboveMal = RuleTypeChecker.evaluateAsBoolean (system, null, m_aboveMal, errors, lookup);
                    boolean aboveRT = RuleTypeChecker.evaluateAsBoolean (system, null, m_aboveRT, errors, lookup);
                    float avgRT = 0.0f;
                    float perMal = 0.0f;
                    if (clients != null) {
                        float cum = 0.0f;
                        int numMal = 0;
                        for (Object o : clients.getValues ()) {
                            IAcmeProperty prop;
                            float mal = 0;
                            if (o instanceof IAcmeElementInstance) {
                                IAcmeElementInstance client = (IAcmeElementInstance) o;
                                if ((prop = client.getProperty ("experRespTime")) != null) {
                                    cum += ((IAcmeFloatingPointValue) prop.getValue ()).getFloatValue ();
                                }
                                if ((prop = client.getProperty ("maliciousness")) != null) {
                                    mal = ((IAcmeFloatingPointValue) prop.getValue ()).getFloatValue ();
                                    log (MessageFormat.format ("{0}.maliciousness={1}", client, mal));
                                    if (mal > 0.5) {
                                        numMal++;
                                    }
                                }
                            }
                        }
                        avgRT = cum / clients.getValues ().size ();
                        perMal = ((float) numMal) / clients.getValues ().size () * 100f;
                        SetSystemPropertiesCmd cmd = model.getCommandFactory ().setSystemProperties (system, avgRT, perMal, aboveRT, aboveMal);
                        m_modelUSPort.updateModel (cmd);

                    }


                } catch (AcmeException e) {
                    m_reportingPort.error (RainbowComponentT.ANALYSIS, "Failed to evaluate an expression", e);
                }

            }


        }

    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

    @Override
    public void setProperty (String key, String value) {
        properties.put (key, value);
    }

    @Override
    public String getProperty (String key) {
        return properties.get (key);
    }

    @Override
    public void dispose () {
        m_modelChangePort.dispose ();
        m_reportingPort.dispose ();
        m_modelUSPort.dispose ();
    }

    @Override
    public void onEvent (ModelReference reference, IRainbowMessage message) {
        IModelInstance model = m_modelsManagerPort.getModelInstance (reference);
        // Add a model to check, if it doesn't already exist to be processed
        synchronized (m_modelCheckQ) {
            if (model instanceof ZNNModelUpdateOperatorsImpl && !m_modelCheckQ.contains (model)) {
                m_modelCheckQ.offer ((ZNNModelUpdateOperatorsImpl) model);
            }
        }
    }
}
