package org.sa.rainbow.translator.probes;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.util.RainbowLogger;
import org.sa.rainbow.util.Util;

public class LocalProbeManager extends AbstractRainbowRunnable {
    Logger                      LOGGER          = Logger.getLogger (this.getClass ());
    static String NAME = "ProbeManager";
    private ProbeDescription               m_localProbeDesc;
    private Map<String, IProbe>            m_localProbes = new HashMap<> ();
    private Map<String, IProbe>            m_alias2Probe = new HashMap<> ();

    private boolean                        m_probesStarted = false;

    public LocalProbeManager (String delegateId) {
        super (NAME + "[@" + delegateId + "]");
    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

    @Override
    protected void log (String txt) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void runAction () {
        Collection<IProbe> probes;
        synchronized (m_localProbes) {
            probes = new HashSet<> (m_localProbes.values ());
        }
        for (IProbe probe : probes) {
//            if (!probe.isActive () && probe.lcState ().compareTo (IProbe.State.NULL) > 0) {
//                probe.activate ();
//            }
            if (!probe.isAlive ()) {
                log (probe.id () + ": " + probe.id () + " appears to be dead! Deregistering...");
                deregisterProbeAlias (probe.type ());
                deregisterProbe (probe);

            }
        }
    }



    public synchronized void initProbes (List<ProbeAttributes> probes) {
        m_localProbeDesc = new ProbeDescription ();
        m_localProbeDesc.probes = new TreeSet<> (probes);
        // obtain the list of probes to create
        for (ProbeDescription.ProbeAttributes pbAttr : m_localProbeDesc.probes) {
            // see if probe is for my location
            if (!pbAttr.location.equals (Rainbow.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION))) {
                continue;
            }
            try {
                IProbe probe = null;
                // prepare class info
                String probeClazz = null;
                Class<?>[] params = null;
                Object[] args = null;
                // collect argument values
                String refID = Util.genID (pbAttr.name, pbAttr.location);
                switch (pbAttr.kind) {
                case SCRIPT:
                    // get info for a script-based probe
                    String path = pbAttr.info.get ("path");
                    String argument = pbAttr.info.get ("argument");
                    if (!new File (path).exists ()) {
                        RainbowLogger.error (RainbowComponentT.PROBE_MANAGER,
                                MessageFormat.format ("Could not create probe {0}. The script \"{1}\" does not exist.",
                                        pbAttr.location, path), m_reportingPort, LOGGER);
                        continue; // don't create
                    }
                    GenericScriptBasedProbe gProbe = new GenericScriptBasedProbe (refID, pbAttr.alias, path, argument);
                    m_reportingPort.info (getComponentType (), "Script-based IProbe " + pbAttr.name + ": " + path + " "
                            + argument);
                    String mode = pbAttr.info.get ("mode");
                    if ("continual".equals (mode)) {
                        gProbe.setContinual (true);
                    }
                    probe = gProbe;
                    break;
                case JAVA:
                    probeClazz = pbAttr.info.get ("class");
                    // get argument info for Java probe, including ID and possibly sleep period
                    List<Class<?>> paramList = new ArrayList<Class<?>> ();
                    List<Object> argsList = new ArrayList<Object> ();
                    paramList.add (String.class);
                    argsList.add (refID);
                    String periodStr = pbAttr.info.get ("period");
                    if (periodStr != null) { // assume long
                        paramList.add (long.class);
                        argsList.add (Long.parseLong (periodStr));
                    }
                    if (pbAttr.arrays.size () > 0) {
                        // get list of arguments for a pure Java probe
                        for (Object vObj : pbAttr.arrays.values ()) {
                            paramList.add (vObj.getClass ());
                            argsList.add (vObj);
                        }
                    }
                    params = paramList.toArray (new Class<?>[0]);
                    args = argsList.toArray ();
                    break;
                }
                if (probeClazz != null) {
                    try {
                        Class<?> probeClass = Class.forName (probeClazz);
                        Constructor<?> cons = probeClass.getConstructor (params);
                        probe = (IProbe )cons.newInstance (args);
                        probe.setLoggingPort (m_reportingPort);
                    }
                    catch (Throwable e) {
                        String msg = MessageFormat.format ("Could not instantiate probe: {0}", probeClazz);
                        RainbowLogger.error (RainbowComponentT.PROBE_MANAGER, msg, e, m_reportingPort, LOGGER);
                    }
                }

                if (probe != null) {
                    probe.create ();
                    log (probe.id () + " created.");
                    m_localProbes.put (probe.id (), probe);
                    m_alias2Probe.put (probe.type (), probe);
                }
            }
            catch (Exception e) {
                String msg = MessageFormat.format ("Could not instantiate probe: {0}", pbAttr.name);
                RainbowLogger.error (RainbowComponentT.PROBE_MANAGER, msg, e, m_reportingPort, LOGGER);
            }
        }
    }

    protected void deregisterProbeAlias (String alias) {
        IProbe probe = m_alias2Probe.remove (alias);
        deregisterProbe (probe);
    }

    protected void deregisterProbe (IProbe probe) {
        if (probe != null) {
            if (probe.isActive ()) {
                probe.deactivate ();
            }
            if (probe.lcState () != IProbe.State.NULL) {
                probe.destroy ();
            }
            if (m_localProbes.containsKey (probe.id ())) {
                m_localProbes.remove (probe.id ());
            }
        }
    }

    public Set<ProbeAttributes> getProbeConfiguration () {
        return m_localProbeDesc.probes;
    }

    public synchronized void startProbes () {
        if (m_probesStarted) return;
        m_probesStarted = true;
        Collection<IProbe> probes = m_localProbes.values ();
        for (IProbe probe : probes) {
            probe.activate ();
        }
    }

    public synchronized void killProbes () {
        if (!m_probesStarted) return;
        m_probesStarted = false;
        Collection<IProbe> probes = m_localProbes.values ();
        for (IProbe probe : probes) {
            probe.deactivate ();
        }
    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.PROBE_MANAGER;
    }

}
