/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.translator.effectors;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;

public abstract class EffectorManager extends AbstractRainbowRunnable implements IEffectorLifecycleBusPort,
IModelDSBusPublisherPort {

    private final Logger LOGGER = Logger.getLogger (this.getClass ());

    @Nullable
    private IEffectorLifecycleBusPort m_effectorLifecyclePort;

    private final Map<String, IEffectorExecutionPort> m_effectorExecutionPorts = new HashMap<> ();

    @Nullable
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

    public Outcome executeEffector (String effName, @NotNull String target, String[] args) {

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
    public void reportCreated (@NotNull IEffectorIdentifier effector) {
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
    public void reportDeleted (@NotNull IEffectorIdentifier effector) {
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


    }

    @Override
    protected void log (String txt) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void runAction () {
        // TODO Auto-generated method stub

    }

    @NotNull
    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.EFFECTOR_MANAGER;
    }

    @NotNull
    protected Set<EffectorAttributes> getEffectorsAtLocation (@NotNull String location) {
        Set<EffectorAttributes> effectors = new LinkedHashSet<> ();
        for (EffectorAttributes candidate : m_effectors.effectors) {
            if (location.equals (candidate.getLocation())) {
                effectors.add (candidate);
            }
        }
        return effectors;
    }

    @Nullable
    @Override
    public IRainbowMessage createMessage () {
        return null;
    }

}
