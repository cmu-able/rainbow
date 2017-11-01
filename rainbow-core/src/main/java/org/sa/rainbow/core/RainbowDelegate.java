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
package org.sa.rainbow.core;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.LocalGaugeManager;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IDelegateMasterConnectionPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.translator.effectors.LocalEffectorManager;
import org.sa.rainbow.translator.probes.LocalProbeManager;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

public class RainbowDelegate extends AbstractRainbowRunnable implements RainbowConstants {


    enum ConnectionState {
        UNKNOWN, CONNECTING, CONNECTED, CONFIGURED
    }

    protected static final String NAME = "Rainbow Delegate";


    private final String m_id;

    private String m_name = null;


    private Beacon m_beacon;

    /** The port through which the management information comes (lifecyle, reporting, ...) **/

    private IDelegateManagementPort        m_masterPort;
    /** The connection port to the master **/
    private IDelegateMasterConnectionPort m_masterConnectionPort;
    /**
     * The configuration port, through which the delegate can be configured. Configuration includes which local probes
     * and effectors to start. It "looks" unused because it calls back
     **/
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private IDelegateConfigurationPort     m_configurationPort;

    private Properties m_configurationInformation;
    /** Manages the connection lifecycle of the delegate. Perhaps this should be moved to the connection port?**/

    private ConnectionState m_delegateState = ConnectionState.UNKNOWN;

    /** The local effectors **/
    private EffectorDescription m_localEffectorDesc;

    private LocalProbeManager    m_probeManager;
    private LocalGaugeManager    m_gaugeManager;
    private LocalEffectorManager m_effectorManager;

    final List<ProbeAttributes>          m_probes    = new LinkedList<> ();
    final List<EffectorAttributes>       m_effectors = new LinkedList<> ();
    final List<GaugeInstanceDescription> m_gauges    = new LinkedList<> ();

    public RainbowDelegate () {
        super (NAME);
        // Generate an ID 
        m_id = UUID.randomUUID ().toString ();
    }

    public void initialize () throws RainbowConnectionException {
        // Create the connection to the master
        m_masterConnectionPort = RainbowPortFactory.createDelegateMasterConnectionPort (this);
        log ("Attempting to connecto to master.");
        m_delegateState = ConnectionState.CONNECTING;
        m_reportingPort = m_masterConnectionPort;
        m_configurationPort = RainbowPortFactory.createDelegateConfigurationPort (this);
        m_masterPort = m_masterConnectionPort.connectDelegate (m_id, getConnectionProperties ());
        m_delegateState = ConnectionState.CONNECTED;
        // Request configuration information

        m_masterPort.requestConfigurationInformation ();
        m_probeManager = new LocalProbeManager (getId ());
        m_probeManager.initialize (m_masterConnectionPort);
        m_probeManager.start ();

        m_gaugeManager = new LocalGaugeManager (getId (), m_masterConnectionPort);

        m_effectorManager = new LocalEffectorManager (getId ());
        m_effectorManager.initialize (m_masterConnectionPort);

    }


    private Properties getConnectionProperties () {
        Properties props = new Properties ();
        props.setProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION, Rainbow.instance ().getProperty
                (PROPKEY_DEPLOYMENT_LOCATION));
        return props;
    }

    /**
     * Called when configuration information is received from the master.
     *
     * @param props
     *            The configuration information, as a set of properties
     */
    public synchronized void receiveConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors,
            List<GaugeInstanceDescription> gauges) {
        synchronized (m_probes) {
            // This might take some time, so record the information lest the method times out
            m_configurationInformation = props;
            m_probes.addAll (probes);
            m_gauges.addAll (gauges);
            m_effectors.addAll (effectors);
        }
        log ("Received configuration information.");

        // Process the period for sending the heartbeat
        long period = Long.parseLong (props.getProperty (PROPKEY_DELEGATE_BEACONPERIOD, "10000"));
        if (m_beacon != null) {
            // Reset beacon period, i it exists
            if (m_beacon.period () != period) {
                m_beacon.setPeriod (period);
            }
        }
        else {
            m_beacon = new Beacon (period);

        }
        m_beacon.mark ();
        // If the Master has a name of this delegate, use it as the name in logging and such
        String id = props.getProperty (PROPKEY_DELEGATE_ID);
        if (id != null) {
            m_name = id;
        }
        m_delegateState = ConnectionState.CONFIGURED;
    }

    void initDelegateComponents (List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors,
            List<GaugeInstanceDescription> gauges) {
        m_probeManager.initProbes (probes);

        m_gaugeManager.initGauges (gauges);
        EffectorDescription ed = new EffectorDescription ();
        ed.effectors = new TreeSet<> (effectors);
        m_effectorManager.initEffectors (ed);
    }

    @Override
    public void dispose () {

        m_probeManager.terminate ();
        m_effectorManager.terminate ();

        m_masterPort.dispose ();
        m_masterConnectionPort.dispose ();
    }

    @Override
    protected void log (String txt) {
        m_masterConnectionPort.info (RainbowComponentT.DELEGATE,
                MessageFormat.format (
                        "{2}[{0}]: {1}",
                        Util.timelog (),
                        txt,
                        m_name == null ? MessageFormat.format ("RD-{0}", m_id) : MessageFormat.format ("{0}-{1}", m_name, m_id)));
    }

    @Override
    protected void runAction () {
        synchronized (m_probes) {
            if (!m_probes.isEmpty () || !m_effectors.isEmpty () || !m_gauges.isEmpty ()) {
                // if anything is left to start
                initDelegateComponents (m_probes, m_effectors, m_gauges);
                // clear so we don't start the next time
                m_probes.clear ();
                m_gauges.clear ();
                m_effectors.clear ();
            }
        }

        manageHeartbeat ();
    }

    private void manageHeartbeat () {
        if (m_beacon != null && m_beacon.periodElapsed ()) {
            log ("Sending heartbeat.");
            m_masterPort.heartbeat ();
            m_beacon.mark ();
        }
    }

    /** 
     * 
     */
    @Override
    protected void doTerminate () {
        log ("Terminating.");
        m_beacon = null;
        m_masterConnectionPort.disconnectDelegate (getId ());
        Rainbow.instance ().signalTerminate ();
        super.doTerminate ();
    }

    @Override
    public void start () {
        log ("Starting.");
        super.start ();
    }

    @Override
    public void stop () {
        log ("Pausing.");
        super.stop ();
    }


    public String getId () {
        return m_id;
    }

    public Properties getConfigurationInformation () {
        return m_configurationInformation;
    }

    public static void main (String[] args) throws RainbowConnectionException {

        RainbowDelegate del = new RainbowDelegate ();
        del.initialize ();
        del.start ();
    }

    public void disconnectFromMaster () {
        terminate ();
    }

    @Override
    public void terminate () {
        super.terminate ();
        while (!isTerminated ()) {
            try {
                Thread.sleep (500);
            }
            catch (InterruptedException e) {
            }
        }
    }



    // Methods after this are used for testing

    synchronized ConnectionState getConnectionState () {
        return m_delegateState;
    }

    synchronized Set<ProbeAttributes> getProbeConfiguration () {
        return m_probeManager.getProbeConfiguration ();
    }


    synchronized Set<EffectorAttributes> getEffectorConfiguration () {
        return m_localEffectorDesc.effectors;
    }

    public void startProbes () {
        m_probeManager.startProbes ();
    }

    public void killProbes () {
        m_probeManager.killProbes ();
    }


    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.DELEGATE;
    }


}
