package org.sa.rainbow.translator.effectors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Util;

public abstract class EffectorManager extends AbstractRainbowRunnable implements IEffectorLifecycleBusPort,
IModelDSBusPublisherPort {

    Logger                            LOGGER = Logger.getLogger (this.getClass ());

    private IEffectorLifecycleBusPort m_effectorLifecyclePort;

    private Map<String, IEffectorExecutionPort> m_effectorExecutionPorts = new HashMap<> ();

    private IModelDSBusSubscriberPort           m_modelDSSubscribePort;
    protected IModelsManagerPort                m_modelsManagerPort;

    protected EffectorDescription               m_effectors;

    public EffectorManager (String id) {
        super (id);
    }

    public void setEffectors (EffectorDescription ed) {
        m_effectors = ed;
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        m_modelDSSubscribePort.subscribeToOperations (this);
    }

    private void initializeConnections () throws RainbowConnectionException {
        m_effectorLifecyclePort = RainbowPortFactory.createClientSideEffectorLifecyclePort (this);
        m_modelDSSubscribePort = RainbowPortFactory.createModelDSSubscribePort (this);
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
    }

    public Outcome executeEffector (String effName, String target, String[] args) {

        String id = Util.genID (effName, target);
        m_reportingPort.info (RainbowComponentT.EFFECTOR_MANAGER, "Attempting E[" + id + "] (" + Arrays.asList (args)
                + ")");
        if (LOGGER.isDebugEnabled ()) {
            LOGGER.debug (MessageFormat.format ("[EffectorManager]: getEffector called, composed ID: {0}", id));
        }
        IEffectorExecutionPort effector = m_effectorExecutionPorts.get (id);
        if (effector == null) {
            // convert target into IP4 number (current Rainbow standard representation)
            if (target.equalsIgnoreCase ("localhost")) {
                String localhost = Rainbow.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION);
                try {
                    id = Util.genID (effName, InetAddress.getByName (localhost).getHostAddress ());
                    if (LOGGER.isDebugEnabled ()) {
                        LOGGER.debug ("[EffectorManager]: getEffector retries ID: " + id);
                    }
                    effector = m_effectorExecutionPorts.get (id);
                }
                catch (UnknownHostException e) {
                }
            }
            else {
                try {
                    id = Util.genID (effName, InetAddress.getByName (target).getHostAddress ());
                    if (LOGGER.isDebugEnabled ()) {
                        LOGGER.debug ("[EffectorManager]: getEffector retries ID: " + id);
                    }
                    effector = m_effectorExecutionPorts.get (id);
                }
                catch (UnknownHostException e) {
                }
            }
        }
        if (LOGGER.isDebugEnabled ()) {
            LOGGER.debug ("[EffectorManager]: Effector retrieved " + (effector == null ? "NULL" : id));
        }
        if (effector == null) return Outcome.UNKNOWN;
        return effector.execute (Arrays.asList (args));
    }

    @Override
    public void reportCreated (IEffectorIdentifier effector) {
        LOGGER.info (MessageFormat.format ("EffectorManager: An effector was created {0}", effector.id ()));
        try {
            IEffectorExecutionPort port = RainbowPortFactory.createEffectorExecutionPortClient (effector);
            synchronized (m_effectorExecutionPorts) {
                m_effectorExecutionPorts.put (effector.id (), port);
            }
        }
        catch (RainbowConnectionException e) {
            LOGGER.error (MessageFormat.format ("Could not create a connection to the effector: {0}", effector.id (), e));
        }
    }

    @Override
    public void reportDeleted (IEffectorIdentifier effector) {
        LOGGER.info (MessageFormat.format ("EffectorManager: An effector was deleted {0}", effector.id ()));
    }

    @Override
    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose () {
        m_modelDSSubscribePort.dispose ();
        m_effectorLifecyclePort.dispose ();
        for (IEffectorExecutionPort p : m_effectorExecutionPorts.values ()) {
            p.dispose ();
        }
        m_effectorExecutionPorts.clear ();
        m_modelDSSubscribePort = null;
        m_effectorLifecyclePort = null;
        m_effectorExecutionPorts = null;

    }

    @Override
    protected void log (String txt) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void runAction () {
        // TODO Auto-generated method stub

    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.EFFECTOR_MANAGER;
    }

    protected Set<EffectorAttributes> getEffectorsAtLocation (String location) {
        Set<EffectorAttributes> effectors = new LinkedHashSet<EffectorAttributes> ();
        for (EffectorAttributes candidate : m_effectors.effectors) {
            if (location.equals (candidate.location)) {
                effectors.add (candidate);
            }
        }
        return effectors;
    }

    @Override
    public IRainbowMessage createMessage () {
        return null;
    }

}
