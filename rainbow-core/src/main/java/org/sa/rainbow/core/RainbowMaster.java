package org.sa.rainbow.core;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow.ExitState;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.RainbowGUI;
import org.sa.rainbow.translator.effectors.EffectorManager;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.RainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.Util;
import org.sa.rainbow.util.YamlUtil;

public class RainbowMaster extends AbstractRainbowRunnable implements IMasterCommandPort, IRainbowMaster {
    static Logger                            LOGGER                        = Logger.getLogger (Rainbow.class
            .getCanonicalName ());

    Map<String, IDelegateManagementPort>     m_delegates                   = new HashMap<> ();
    Map<String, IDelegateConfigurationPort>  m_delegateConfigurtationPorts = new HashMap<> ();
    Map<String, Properties>                  m_delegateInfo                = new HashMap<> ();

    IMasterConnectionPort                    m_delegateConnection;
    private Map<String, Beacon>              m_heartbeats                  = new HashMap<> ();

    private ModelsManager                    m_modelsManager;

    private ProbeDescription                 m_probeDesc;
    private EffectorDescription              m_effectorDesc;
    private GaugeDescription                 m_gaugeDesc;
    private UtilityPreferenceDescription     m_prefDesc;

    private GaugeManager                     m_gaugeManager;

    private Collection<IRainbowAnalysis>     m_analyses                    = Collections.<IRainbowAnalysis> emptySet ();

    private Map<String, IAdaptationManager>  m_adaptationManagers          = new HashMap<> ();

    private Map<String, IAdaptationExecutor> m_adaptationExecutors         = new HashMap<> ();

    private Collection<EffectorManager>      m_effectorManagers            = Collections.<EffectorManager> emptySet ();

    private Map<String, Beacon>              m_terminatedDelegates         = Collections
            .<String, Beacon> synchronizedMap (new HashMap<String, Beacon> ());

    public RainbowMaster () throws RainbowConnectionException {
        super ("Rainbow Master");
        Rainbow.instance ().setIsMaster (true);
        Rainbow.instance ().setMaster (this);
    }

    public void initialize () throws RainbowException {
        readConfiguration ();
        initializeConnections ();
        super.initialize (m_reportingPort);
        initializeRainbowComponents ();
        RainbowConfigurationChecker checker = new RainbowConfigurationChecker (this);
        checker.checkRainbowConfiguration ();
        for (Problem p : checker.getProblems ()) {
            if (p.problem == ProblemT.ERROR) {
                m_reportingPort.error (getComponentType(), p.msg);
            }
            else {
                m_reportingPort.warn (getComponentType(), p.msg);
            }
        }
    }

    private void readConfiguration () {
        probeDesc ();
        effectorDesc ();
        gaugeDesc ();
        preferenceDesc ();
    }

    private void initializeRainbowComponents () throws RainbowException {
        m_modelsManager = new ModelsManager ();
        m_modelsManager.initialize (m_reportingPort);
        m_modelsManager.start ();

        m_gaugeManager = new GaugeManager (gaugeDesc ());
        m_gaugeManager.initialize (m_reportingPort);
        m_gaugeManager.start ();

        int effectorManagerSize = Rainbow.getProperty (RainbowConstants.PROPKEY_EFFECTOR_MANAGER_COMPONENT_SIZE, 0);
        m_effectorManagers = new LinkedList<> ();
        for (int i = 0; i < effectorManagerSize; i++) {
            String emProp = RainbowConstants.PROPKEY_EFFECTOR_MANAGER_COMPONENT + "_" + i;
            String em = Rainbow.getProperty (emProp);
            if (em != null) {
                em = em.trim ();
                try {
                    Class<? extends EffectorManager> cls = (Class<? extends EffectorManager> )Class.forName (em);
                    EffectorManager effMan = cls.newInstance ();
                    m_effectorManagers.add (effMan);
                    effMan.setEffectors (effectorDesc ());
                    effMan.initialize (m_reportingPort);
                    effMan.start ();
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    m_reportingPort.error (RainbowComponentT.MASTER,
                            MessageFormat.format ("Could not start effector manager ''{0}''", em), e);
                }
            }
        }


        int analysisSize = Rainbow.getProperty (RainbowConstants.PROPKEY_ANALYSIS_COMPONENT_SIZE, 0);
        m_analyses = new LinkedList<> ();
        for (int i = 0; i < analysisSize; i++) {
            String analysisProp = RainbowConstants.PROPKEY_ANALYSIS_COMPONENTS + "_" + i;
            String an = Rainbow.getProperty (analysisProp);
            if (an != null) {
                an = an.trim ();
                try {
                    Class<? extends IRainbowAnalysis> cls = (Class<? extends IRainbowAnalysis> )Class.forName (an);
                    IRainbowAnalysis analysis = cls.newInstance ();
                    m_analyses.add (analysis);
                    analysis.initialize (m_reportingPort);
                    analysis.start ();
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    m_reportingPort.error (RainbowComponentT.MASTER,
                            MessageFormat.format ("Could not start the analysis ''{0}''", an), e);
                }
            }
        }

        int adaptationManagerSize = Rainbow.getProperty (RainbowConstants.PROPKEY_ADAPTATION_MANAGER_SIZE, 0);
        m_adaptationManagers = new HashMap<> ();
        for (int i = 0; i < adaptationManagerSize; i++) {

            String adaptationManagerProp = RainbowConstants.PROPKEY_ADAPTATION_MANAGER_CLASS + "_" + i;
            String adaptationManagerClass = Rainbow.getProperty (adaptationManagerProp);
            if (adaptationManagerClass != null) {
                try {
                    Class<? extends IAdaptationManager> cls = (Class<? extends IAdaptationManager> )Class
                            .forName (adaptationManagerClass.trim ());
                    IAdaptationManager adaptationManager = cls.newInstance ();
                    String amModel = RainbowConstants.PROPKEY_ADAPTATION_MANAGER_MODEL + "_" + i;
                    String modelReference = Rainbow.getProperty (amModel);
                    if (modelReference != null) {
                        TypedAttribute model = Util.decomposeModelReference (modelReference);
                        adaptationManager.setModelToManage (model.getName (), model.getType ());
                        m_adaptationManagers.put (modelReference, adaptationManager);
                        adaptationManager.initialize (m_reportingPort);

                        adaptationManager.start ();
                    }
                    else {
                        m_reportingPort
                        .error (RainbowComponentT.MASTER,
                                MessageFormat
                                .format (
                                        "There is no model reference for adapation manager ''{0}''. Need to set the property ''{1}''.",
                                        adaptationManagerClass, amModel));
                    }
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    m_reportingPort.error (RainbowComponentT.MASTER, MessageFormat.format (
                            "Could not start the adaptation manager ''{0}''.", adaptationManagerClass), e);
                }
            }
            else {
                m_reportingPort.warn (RainbowComponentT.MASTER,
                        MessageFormat.format ("Could not find property ''{0}''", adaptationManagerProp));
            }
        }
        int adaptationExecutorSize = Rainbow.getProperty (RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_SIZE, 0);

        for (int i = 0; i < adaptationExecutorSize; i++) {
            String adaptationExecutorProp = RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_CLASS + "_" + i;
            String adaptationExecutorClass = Rainbow.getProperty (adaptationExecutorProp);
            if (adaptationExecutorClass != null) {
                try {
                    Class<? extends IAdaptationExecutor> cls = (Class<? extends IAdaptationExecutor> )Class
                            .forName (adaptationExecutorClass.trim ());
                    IAdaptationExecutor adaptationExecutor = cls.newInstance ();
                    adaptationExecutor.initialize (m_reportingPort);
                    String amModel = RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_MODEL + "_" + i;
                    String modelReference = Rainbow.getProperty (amModel);
                    if (modelReference != null) {
                        TypedAttribute model = Util.decomposeModelReference (modelReference);
                        adaptationExecutor.setModelToManage (model.getName (), model.getType ());
                        m_adaptationExecutors.put (modelReference, adaptationExecutor);
                        adaptationExecutor.start ();
                    }
                    else {
                        m_reportingPort
                        .error (RainbowComponentT.MASTER,
                                MessageFormat
                                .format (
                                        "There is no model reference for adapation manager ''{0}''. Need to set the property ''{1}''.",
                                        adaptationExecutorClass, amModel));
                    }
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    m_reportingPort.error (RainbowComponentT.MASTER, MessageFormat.format (
                            "Could not start the adaptation manager ''{0}''.", adaptationExecutorClass), e);
                }

            }
            else {
                m_reportingPort.warn (RainbowComponentT.MASTER,
                        MessageFormat.format ("Could not find property ''{0}''", adaptationExecutorProp));
            }
        }

    }

    private void initializeConnections () throws RainbowConnectionException {
        m_delegateConnection = RainbowPortFactory.createDelegateConnectionPort (this);
        m_reportingPort = RainbowPortFactory.createMasterReportingPort ();
    }

    @Override
    public ModelsManager modelsManager () {
        return m_modelsManager;
    }

    /**
     * Connects a new delegate and sends the appropriate configuration information to the delegate
     * 
     * @param delegateID
     * @param connectionProperties
     * @param delegateIP
     *            Drop the ip address
     */
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) {
        LOGGER.debug (MessageFormat.format ("Master received connection request from: {0} at {1}", delegateID,
                connectionProperties.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION, "Unknown Location")));
        try {
            m_delegateInfo.put (delegateID, connectionProperties);
            IDelegateManagementPort delegatePort = RainbowPortFactory.createMasterDeploymentPort (this, delegateID,
                    connectionProperties);
            // Check to see if there is already a registered delegate running on the machine
            m_delegates.put (delegateID, delegatePort);
            IDelegateConfigurationPort delegateConfigurationPort = RainbowPortFactory
                    .createDelegateConfigurationPortClient (delegateID);
            m_delegateConfigurtationPorts.put (delegateID, delegateConfigurationPort);
            // Add a second to the heartbeat to allow for communication time
            // TODO: Must be a better way to do this...
            Beacon beacon = new Beacon (Long.parseLong (Rainbow.getProperty (
                    RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD, "1000")) + 1000);
            m_heartbeats.put (delegatePort.getDelegateId (), beacon);
            beacon.mark ();
            LOGGER.info (MessageFormat.format ("Master created management connection with delegate {0}", delegateID));
            return delegatePort;
        }
        catch (NumberFormatException | RainbowConnectionException e) {
            LOGGER.error (MessageFormat.format (
                    "Rainbow master could not create the management interface to the delegate {0}", delegateID));
            m_delegateConnection.disconnectDelegate (delegateID);
        }
        return null;
    }

    /**
     * Called by a delegate port to request information be sent to it
     * 
     * @param delegateID
     */
    public void requestDelegateConfiguration (String delegateID) {
        IDelegateConfigurationPort delegate = m_delegateConfigurtationPorts.get (delegateID);
        if (delegate != null) {
            LOGGER.info (MessageFormat.format ("Sending configuration information to {0}.", delegateID));
            delegate.sendConfigurationInformation (filterPropertiesForDelegate (delegateID),
                    filterProbesForDelegate (delegateID), filterEffectorsForDelegate (delegateID),
                    filterGaugesForDelegate (delegateID));
        }
        else {
            LOGGER.error (MessageFormat
                    .format ("Received configuration request from unknown delegate {0}.", delegateID));
        }
    }

    private List<GaugeInstanceDescription> filterGaugesForDelegate (String delegateID) {
        if (gaugeDesc ().instSpec == null) return Collections.<GaugeInstanceDescription> emptyList ();

        Properties delegateInfo = m_delegateInfo.get (delegateID);
        String deploymentInfo = null;
        if (delegateInfo == null
                || (deploymentInfo = delegateInfo.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
            LOGGER.error ("There is no location information associated with " + delegateID);
            return Collections.<GaugeInstanceDescription> emptyList ();
        }
        return filterGaugesForLocation (deploymentInfo);
    }

    List<GaugeInstanceDescription> filterGaugesForLocation (String deploymentInfo) {
        List<GaugeInstanceDescription> gauges = new LinkedList<GaugeInstanceDescription> ();
        for (GaugeInstanceDescription gid : gaugeDesc ().instSpec.values ()) {
            TypedAttributeWithValue targetIP = gid.findSetupParam ("targetIP");
            if (deploymentInfo.equals (targetIP.getValue ())) {
                gauges.add (gid);
            }
        }
        return gauges;
    }

    private List<EffectorAttributes> filterEffectorsForDelegate (String delegateID) {
        if (effectorDesc ().effectors == null)
            return Collections.<EffectorAttributes> emptyList ();
        else {
            Properties delegateInfo = m_delegateInfo.get (delegateID);
            String deploymentInfo = null;

            if (delegateInfo == null
                    || (deploymentInfo = delegateInfo.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
                LOGGER.error ("There is no location information associated with " + delegateID);
                return Collections.<EffectorAttributes> emptyList ();
            }
            return filterEffectorsForLocation (deploymentInfo);
        }
    }

    List<EffectorAttributes> filterEffectorsForLocation (String deploymentInfo) {
        List<EffectorAttributes> effectors = new LinkedList<EffectorAttributes> ();
        for (EffectorAttributes probe : effectorDesc ().effectors) {
            if (probe.location.equals (deploymentInfo)) {
                effectors.add (probe);
            }
        }
        return effectors;
    }

    /**
     * Called when a delegate sends a heartbeat message
     * 
     * @param delegateID
     *            The IP of the delegate
     */
    public void processHeartbeat (String delegateID) {
        IDelegateManagementPort delegate = m_delegates.get (delegateID);
        if (delegate != null) {
            Beacon hb = m_heartbeats.get (delegate.getDelegateId ());
            if (hb == null) {
                LOGGER.error (MessageFormat.format ("Received heartbeat from unknown delegate at {0}.", delegateID));
            }
            else {
                Properties properties = m_delegateInfo.get (delegateID);
                String loc = "???";
                if (properties != null) {
                    loc = properties.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION);
                }

                m_reportingPort.info (RainbowComponentT.MASTER,
                        MessageFormat.format ("Heartbeat from {0}@{1}", delegateID, loc));
                hb.mark ();

            }
        }
        else {
            LOGGER.error (MessageFormat.format ("Received heartbeat from unknown delegate at {0}.", delegateID));
        }
    }

    /**
     * Filters the properties to only report those properties that are relevant to the delegate
     * 
     * @param delegateID
     * @return
     */
    private Properties filterPropertiesForDelegate (String delegateID) {
        return Rainbow.allProperties ();
    }

    private List<ProbeAttributes> filterProbesForDelegate (String delegateID) {
        if (probeDesc ().probes == null)
            return Collections.<ProbeAttributes> emptyList ();
        else {
            Properties delegateInfo = m_delegateInfo.get (delegateID);
            String deploymentInfo = null;
            ;
            if (delegateInfo == null
                    || (deploymentInfo = delegateInfo.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
                LOGGER.error ("There is no location information associated with " + delegateID);
                return Collections.<ProbeAttributes> emptyList ();
            }
            return filterProbesForLocation (deploymentInfo);
        }
    }

    List<ProbeAttributes> filterProbesForLocation (String deploymentInfo) {
        List<ProbeAttributes> probes = new LinkedList<ProbeAttributes> ();
        for (ProbeAttributes probe : probeDesc ().probes) {
            if (probe.location.equals (deploymentInfo)) {
                probes.add (probe);
            }
        }
        return probes;
    }

    @Override
    public void stop () {
        for (IRainbowAnalysis a : m_analyses) {
            a.stop ();
        }
        for (EffectorManager e : m_effectorManagers) {
            e.stop ();
        }
        for (IAdaptationManager a : m_adaptationManagers.values ()) {
            a.stop ();
        }
        for (IAdaptationExecutor a : m_adaptationExecutors.values ()) {
            a.stop ();
        }
        super.stop ();
    }

    @Override
    public void restart () {
        for (IRainbowAnalysis a : m_analyses) {
            a.restart ();
        }
        for (EffectorManager e : m_effectorManagers) {
            e.restart ();
        }
        for (IAdaptationManager a : m_adaptationManagers.values ()) {
            a.restart ();
        }
        for (IAdaptationExecutor a : m_adaptationExecutors.values ()) {
            a.restart ();
        }
        super.restart ();
    }

    @Override
    public void dispose () {

    }

    @Override
    protected void log (String txt) {
        LOGGER.info (MessageFormat.format ("RM: {0}", txt));
    }

    @Override
    protected void runAction () {
        checkTerminations ();
        checkHeartbeats ();
    }

    private void checkTerminations () {
        synchronized (m_terminatedDelegates) {
            if (!m_terminatedDelegates.isEmpty ()) {
                for (Entry<String, Beacon> e : m_terminatedDelegates.entrySet ()) {
                    if (e.getValue ().periodElapsed ()) {
                        m_reportingPort.warn (getComponentType (),
                                "Did not hear back from terminated delegate " + e.getKey () + ". Flushing anyway.");
                        flushDelegate (e.getKey ());
                    }
                }
            }
        }
    }

    private void checkHeartbeats () {
        Set<Entry<String, Beacon>> entrySet = m_heartbeats.entrySet ();
        for (Entry<String, Beacon> entry : entrySet) {
            if (entry.getValue ().periodElapsed ()) {
                Properties properties = m_delegateInfo.get (entry.getKey ());
                String loc = "???";
                if (properties != null) {
                    loc = properties.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION);
                }
                m_reportingPort.error (RainbowComponentT.MASTER,
                        MessageFormat.format ("No Heartbeat from {0}@{1}", entry.getKey (), loc));
                LOGGER.error (MessageFormat.format ("Delegate {0} has not given a heartbeat withing the right time",
                        entry.getKey ()));
                entry.getValue ().mark ();
            }
        }
    }

    public void disconnectDelegate (String id) {
        LOGGER.info (MessageFormat.format ("RM: Disconnecting delegate: {0}", id));
        flushDelegate (id);
    }

    void flushDelegate (String id) {
        synchronized (m_terminatedDelegates) {
            m_terminatedDelegates.remove (id);
        }
        m_heartbeats.remove (id);
        IDelegateManagementPort deploymentPort = m_delegates.remove (id);
        deploymentPort.dispose ();
        m_delegateInfo.remove (id);
        IDelegateConfigurationPort port = m_delegateConfigurtationPorts.remove (id);
    }

    @Override
    public void terminate () {
        for (Entry<String, IDelegateManagementPort> entry : m_delegates.entrySet ()) {
            disconnectDelegate (entry.getKey ());
            entry.getValue ().terminateDelegate ();
            entry.getValue ().dispose ();
        }
        m_delegateConnection.dispose ();

        m_reportingPort.dispose ();
        // TODO: Terminate threads
//        try {
//            Thread.sleep (4000);
//        }
//        catch (InterruptedException e) {
//        }
        super.terminate ();
        while (!isTerminated ()) {
            try {
                Thread.sleep (500);
            }
            catch (InterruptedException e) {
            }
        }
    }

// Methods below this point are used for testing purposes, and so are package protected.
    Map<? extends String, ? extends Beacon> getHeartbeatInfo () {
        return m_heartbeats;
    }

    @Override
    public ProbeDescription probeDesc () {
        if (m_probeDesc == null) {
            m_probeDesc = YamlUtil.loadProbeDesc ();
        }
        return m_probeDesc;
    }

    @Override
    public EffectorDescription effectorDesc () {
        if (m_effectorDesc == null) {
            m_effectorDesc = YamlUtil.loadEffectorDesc ();
        }
        return m_effectorDesc;
    }

    @Override
    public GaugeDescription gaugeDesc () {
        if (m_gaugeDesc == null) {
            m_gaugeDesc = YamlUtil.loadGaugeSpecs ();
        }
        return m_gaugeDesc;
    }

    @Override
    public UtilityPreferenceDescription preferenceDesc () {
        if (m_prefDesc == null) {
            m_prefDesc = YamlUtil.loadUtilityPrefs ();
        }
        return m_prefDesc;
    }

    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        // TODO: Hook up to master interface?
        String log = MessageFormat.format ("Delegate: {0}[{1}]: {2}", delegateID, compT.name (), msg);
        switch (type) {
        case INFO:
            LOGGER.info (log);
            break;
        case WARNING:
            LOGGER.warn (log);
            break;
        case ERROR:
            LOGGER.error (log);
            break;
        case FATAL:
            LOGGER.fatal (log);
            break;
        default:
            break;
        }
    }

    public <S> IAdaptationManager<S> adaptationManagerForModel (String modelRef) {
        return m_adaptationManagers.get (modelRef);
    }

//    public EffectorManager effectorManager () {
//        return m_effectorManager;
//    }

    public <S> IAdaptationExecutor<S> strategyExecutor (String modelRef) {
        return m_adaptationExecutors.get (modelRef);
    }

    public static void main (String[] args) throws RainbowException {

        boolean showHelp = false;
        boolean showGui = true;
        int lastIdx = args.length - 1;
        for (int i = 0; i <= lastIdx; i++) {
            if (args[i].equals ("-h")) {
                showHelp = true;
            }
            else if (args[i].equals ("-nogui")) {
                showGui = false;
            }
            else {
                System.err.println ("Unrecognized or incomplete argument " + args[i]);
                showHelp = true;
            }
        }
        if (showHelp) {
            System.out.println ("Usage:\n" + "  system property options {default}:\n"
                    + "    rainbow.target    name of target configuration {default}\n"
                    + "    rainbow.config    top config directory (org.sa.rainbow.config)\n" + "  options: \n"
                    + "    -h          Show this help message\n" + "    -nogui      Don't show the Rainbow GUI\n"
                    + "\n" + "Option defaults are defined in <rainbow.target>/rainbow.properties");
            System.exit (RainbowConstants.EXIT_VALUE_ABORT);
        }

        RainbowMaster master = new RainbowMaster ();
        if (showGui) {
            RainbowGUI gui = new RainbowGUI (master);
            gui.display ();
        }
        master.initialize ();


        RainbowDelegate localDelegate = new RainbowDelegate ();
        localDelegate.initialize ();


        master.start ();
        localDelegate.start ();

    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.MASTER;
    }

    @Override
    public void startProbes () {
        for (IDelegateManagementPort delegate : m_delegates.values ()) {
            delegate.startProbes ();
        }
    }

    @Override
    public void killProbes () {
        for (IDelegateManagementPort delegate : m_delegates.values ()) {
            delegate.killProbes ();
        }
    }

    @Override
    public void enableAdaptation (boolean enabled) {
        for (IAdaptationManager am : m_adaptationManagers.values ()) {
            am.setEnabled (enabled);
        }
    }

    public boolean isAdaptationEnabled () {
        boolean enabled = true;
        for (IAdaptationManager am : m_adaptationManagers.values ()) {
            enabled &= am.isEnabled ();
        }
        return enabled;
    }

    @Override
    public Outcome testEffector (String target, String effName, String[] args) {
        for (EffectorManager em : m_effectorManagers) {
            Outcome outcome = em.executeEffector (effName, target, args);
            if (outcome != Outcome.UNKNOWN) return outcome;
        }
        return Outcome.UNKNOWN;
    }

    @Override
    public void sleep () {
        Rainbow.signalTerminate ();
    }

    @Override
    public void terminate (ExitState exitState) {
        Rainbow.signalTerminate (exitState);
    }

    @Override
    public void restartDelegates () {
        for (IDelegateManagementPort delegate : m_delegates.values ()) {
            delegate.startDelegate ();
        }
    }

    @Override
    public void sleepDelegates () {
        for (IDelegateManagementPort delegate : m_delegates.values ()) {
            delegate.pauseDelegate ();
        }
    }

    @Override
    public void destroyDelegates () {
        for (Entry<String, IDelegateManagementPort> e : m_delegates.entrySet ()) {
            Beacon b = new Beacon (10000);
            b.mark ();
            synchronized (m_terminatedDelegates) {
                m_terminatedDelegates.put (e.getKey (), b);
            }
            e.getValue ().terminateDelegate ();
        }
    }

    @Override
    public void killDelegate (String ipOfDelegate) {
        IDelegateManagementPort port = m_delegates.get (ipOfDelegate);
        String did = ipOfDelegate;
        if (port == null) {
            for (Entry<String,Properties> e : m_delegateInfo.entrySet ()) {
                if (ipOfDelegate.equals (e.getValue ().getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION))) {
                    port = m_delegates.get (e.getKey ());
                    did = e.getKey ();
                    break;
                }
            }
            if (port != null) {
                Beacon b = new Beacon (10000);
                b.mark ();
                synchronized (m_terminatedDelegates) {
                    m_terminatedDelegates.put (did, b);
                }
                port.terminateDelegate ();
            }
        }
    }

    public List<String> getExpectedDelegateLocations () {
        List<String> ret = new LinkedList<String> ();
        Properties allProperties = Rainbow.allProperties ();
        for (Map.Entry o : allProperties.entrySet ()) {
            String k = (String )o.getKey ();
            if (k.startsWith ("customize.system.")) {
                String location = (String )o.getValue ();
                if (!filterEffectorsForLocation (location).isEmpty () || !filterGaugesForLocation (location).isEmpty ()
                        || !filterProbesForLocation (location).isEmpty ()) {
                    ret.add (location);
                }
            }
        }
        Collections.sort (ret);
        return ret;
    }

}
