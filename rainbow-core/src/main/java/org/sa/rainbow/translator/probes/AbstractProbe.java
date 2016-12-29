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
package org.sa.rainbow.translator.probes;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.BadLifecycleStepException;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The AbstractProbe provides the probe State member and the transition of that state when a lifecycle method is
 * executed. It also provides the identifier information as well as the data queue.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractProbe implements IProbe {

    protected Logger LOGGER = Logger.getLogger (AbstractProbe.class);

    private Map<String, Object> m_configParams = null;


    private String m_name = null;

    private String m_location = null;

    private String m_type = null;

    private Kind m_kind = null;

    private State m_state = State.NULL;

    private IProbeReportPort        m_reportingPort;
    private IProbeConfigurationPort m_configurationPort;
    private IProbeLifecyclePort     m_probeManagementPort;

    private final IProbeConfigurationPort m_configurationPortCallback = new IProbeConfigurationPort () {

        @Override
        public void
        configure (Map<String, Object> configParams) {
            AbstractProbe.this.configure (configParams);
        }

        @Override
        public void dispose () {

        }
    };
    private IRainbowReportingPort m_loggingPort;

    /**
     * Main Constuctor that initializes the ID of this Probe.
     *
     * @param id
     *            the unique identifier of the Probe
     * @param type
     *            the type name of the Probe
     * @param kind
     *            one of the enumerated {@link IProbe.Kind}s designating how this Probe would be handled by the
     *            ProbeBusRelay
     */
    public AbstractProbe (String id, String type, Kind kind) {
        m_kind = kind;
        m_configParams = Collections.synchronizedMap (new HashMap<String, Object> ());
        setID (id);
        setType (type);
        try {
            m_probeManagementPort = RainbowPortFactory.createProbeManagementPort (this);
        } catch (RainbowConnectionException e) {
            LOGGER.error ("Failed to connect to management port", e);
        }

    }

    @Override
    public void setLoggingPort (IRainbowReportingPort dcp) {
        m_loggingPort = dcp;
    }

    public IRainbowReportingPort getLoggingPort () {
        return m_loggingPort;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#id()
     */

    @Override
    public String id () {
        return m_name + LOCATION_SEP + m_location;
    }

    /**
     * Sets the ID of this Probe only if ID was <code>null</code>. Not accessible to probe users.
     *
     * @param id
     */
    private void setID (String id) {
        if (m_name == null) {
            // find the "@" index
            int atIdx = id.indexOf (LOCATION_SEP);
            if (atIdx == -1) { // no @ symbol, use NULL_LOCATION
                m_name = id;
                m_location = NULL_LOCATION;
            } else {
                m_name = id.substring (0, atIdx);
                m_location = id.substring (atIdx + 1);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#name()
     */

    @Override
    public String name () {
        return m_name;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#location()
     */

    @Override
    public String location () {
        return m_location;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.IProbe#type()
     */

    @Override
    public String type () {
        return m_type;
    }

    private void setType (String type) {
        m_type = type;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.IProbe#kind()
     */

    @Override
    public Kind kind () {
        return m_kind;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#create()
     */
    @Override
    public synchronized void create () {
        if (m_state != State.NULL)
            throw new BadLifecycleStepException ("Cannot " + Lifecycle.CREATE + " when Probe State is " + m_state);

        m_state = State.INACTIVE;
        m_probeManagementPort.reportCreated ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#activate()
     */
    @Override
    public synchronized void activate () {
        if (m_state != State.INACTIVE)
            throw new BadLifecycleStepException ("Cannot " + Lifecycle.ACTIVATE + " when Probe State is " + m_state);

        m_state = State.ACTIVE;
        m_probeManagementPort.reportActivated ();
        try {
            if (m_reportingPort == null) {
                m_reportingPort = RainbowPortFactory.createProbeReportingPortSender (this);
            }
            if (m_configurationPort == null) {
                m_configurationPort = RainbowPortFactory.createProbeConfigurationPort (this, m_configurationPortCallback);
            }
        }
        catch (RainbowConnectionException e) {
            LOGGER.fatal ("Could not connect to Rainbow infrastructure", e);
            throw new RuntimeException (e);
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#deactivate()
     */
    @Override
    public synchronized void deactivate () {
        if (m_state != State.ACTIVE)
            throw new BadLifecycleStepException ("Cannot " + Lifecycle.DEACTIVATE + " when Probe State is " + m_state);

        m_state = State.INACTIVE;
        m_probeManagementPort.reportDeactivated ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#destroy()
     */
    @Override
    public synchronized void destroy () {
        if (m_state != State.INACTIVE)
            throw new BadLifecycleStepException ("Cannot " + Lifecycle.DESTROY + " when Probe State is " + m_state);
        m_probeManagementPort.reportDeleted ();
        m_configParams.clear ();
        m_configParams = null;
        m_state = State.NULL;

        m_reportingPort.dispose ();
        m_configurationPort.dispose ();
        m_probeManagementPort.dispose ();

    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#lcTransition(org.sa.rainbow.translator.probes.relay.IProbe.Lifecycle)
     */
    @Override
    public void lcTransition (Lifecycle lc) {
        if (lc == Lifecycle.CREATE) {
            create ();
        }
        else if (lc == Lifecycle.ACTIVATE) {
            activate ();
        }
        else if (lc == Lifecycle.DEACTIVATE) {
            deactivate ();
        }
        else if (lc == Lifecycle.DESTROY) {
            destroy ();
        }
        else
            throw new BadLifecycleStepException ("Probe lifecycle command '" + lc + "' unrecognized!");
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#lcState()
     */

    @Override
    public State lcState () {
        return m_state;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.relay.IProbe#isActive()
     */
    @Override
    public boolean isActive () {
        return m_state == State.ACTIVE && !Rainbow.instance ().shouldTerminate ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.IProbe#isAlive()
     */
    @Override
    public boolean isAlive () {
        return true;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.IProbe#configure(java.util.Map)
     */
    @Override
    public void configure (Map<String, Object> configParams) {
        m_configParams.putAll (configParams);
        m_probeManagementPort.reportConfigured (configParams);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.IProbe#reportData(java.lang.String)
     */
    @Override
    public void reportData (String data) {
        m_reportingPort.reportData (this, data);
        log (data);
    }

    protected void log (String txt) {
        String msg = "P[" + id () + "] " + txt;
        if (m_loggingPort != null) {
            m_loggingPort.info (RainbowComponentT.PROBE, msg, LOGGER);
        }
        else {
            LOGGER.info (msg);
        }
    }

}
