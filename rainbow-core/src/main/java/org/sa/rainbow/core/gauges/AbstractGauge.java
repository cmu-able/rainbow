package org.sa.rainbow.core.gauges;

import incubator.pval.Ensure;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

/**
 * Abstract definition of a gauge that does not use probes to gather system information This allows for subclasses to
 * implement gauges that get information from other gauges, gauges that get information from probes, or gauges that get
 * information from elsewhere.
 * 
 * @author Bradley Schmerl (schmerl@cs.cmu.edu)
 * 
 */
public abstract class AbstractGauge extends AbstractRainbowRunnable implements IGauge {
    private final String                                      m_id;

    /** The ports through which the gauge interacts with the outside world **/
    protected IModelUSBusPort                          m_announcePort;
    protected IGaugeLifecycleBusPort                   m_gaugeManagementPort;
    protected IGaugeConfigurationPort                    m_configurationPort;
    protected IGaugeQueryPort                            m_queryPort;
    /**
     * Used to determine when to fire off beacon to consumers; the period will be set by the Gauge implementation
     * subclass
     */
    protected Beacon                                          m_gaugeBeacon  = null;
    protected TypedAttribute                                  m_gaugeDesc    = null;
    protected TypedAttribute                                  m_modelDesc    = null;
    protected Map<String, TypedAttributeWithValue>            m_setupParams  = null;
    protected Map<String, TypedAttributeWithValue>            m_configParams = null;
    protected Map<String, IRainbowOperation> m_commands     = null;
    protected Map<String, IRainbowOperation> m_lastCommands = null;

    /**
     * Main Constructor for the Gauge.
     * 
     * @param threadName
     *            the name of the Gauge thread
     * @param id
     *            the unique ID of the Gauge
     * @param beaconPeriod
     *            the liveness beacon period of the Gauge
     * @param gaugeDesc
     *            the type-name description of the Gauge
     * @param modelDesc
     *            the type-name description of the Model the Gauge updates
     * @param setupParams
     *            the list of setup parameters with their values
     * @param mappings
     *            the list of Gauge Value to Model Property mappings
     * @throws RainbowException
     */
    public AbstractGauge (String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (threadName);
        Ensure.is_false (mappings == null || mappings.isEmpty ());
        Ensure.is_false (modelDesc == null);
        Ensure.is_false (modelDesc.getName () == null);
        Ensure.is_false (modelDesc.getType () == null);
        Ensure.is_false (setupParams == null); // can be empty

        this.m_id = id;

        m_gaugeBeacon = new Beacon (beaconPeriod);
        m_gaugeDesc = gaugeDesc;
        m_modelDesc = modelDesc;

        m_setupParams = new HashMap<String, TypedAttributeWithValue> ();
        m_configParams = new HashMap<String, TypedAttributeWithValue> ();
        m_lastCommands = new HashMap<String, IRainbowOperation> ();
        m_commands = new HashMap<String, IRainbowOperation> ();

        // store the setup parameters
        for (TypedAttributeWithValue param : setupParams) {
            m_setupParams.put (param.getName (), param);
        }

        // Need to keep the list of commands, and also perhaps the commands by value (if they exist)
        for (Entry<String, IRainbowOperation> cmd : mappings.entrySet ()) {
            m_commands.put (cmd.getKey (), cmd.getValue ());
            for (String param : cmd.getValue ().getParameters ()) {
                m_commands.put (pullOutParam (param), cmd.getValue ());
            }
        }

        try {
            m_gaugeManagementPort = RainbowPortFactory.createGaugeSideLifecyclePort ();
            m_announcePort = RainbowPortFactory.createModelsManagerClientUSPort (this);
            // register this Gauge with Rainbow, and report created
            Rainbow.registerGauge (this);
            m_configurationPort = RainbowPortFactory.createGaugeConfigurationPort (this);
            m_queryPort = RainbowPortFactory.createGaugeQueryPort (this);
            m_gaugeManagementPort.reportCreated (this);
            m_gaugeBeacon.mark ();
        }
        // TODO: Remove reliance on ParticipantException
        catch (RainbowConnectionException e) {
            m_reportingPort.error (getComponentType (), "Could not interact with the outside world", e);
            throw new RainbowException ("The gauge could not be started because the ports could not be set up.", e);
        }
    }

    private String pullOutParam (String param) {
        // This is a value parameter, so really should store command by this, too
        // pull out the parameter
        if (param.startsWith ("$<")) {
            param = param.substring (2, param.lastIndexOf (">") - 1);
        }
        return param;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#id()
     */
    @Override
    public String id () {
        return m_id;
    }

    @Override
    public void dispose () {
        m_gaugeManagementPort.reportDeleted (this);

//        Rainbow.eventService().unlisten(m_gaugeEventHandler);

        m_setupParams.clear ();
        m_configParams.clear ();
        m_lastCommands.clear ();
        m_commands.clear ();

        // null-out data members
        m_gaugeManagementPort = null;
//		m_id = null;  // keep value for log output
        m_gaugeDesc = null;
        m_modelDesc = null;
        m_setupParams = null;
        m_configParams = null;
        m_lastCommands = null;
        m_commands = null;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#beaconPeriod()
     */
    @Override
    public long beaconPeriod () {
        return m_gaugeBeacon.period ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#gaugeDesc()
     */
    @Override
    public TypedAttribute gaugeDesc () {
        return m_gaugeDesc;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#modelDesc()
     */
    @Override
    public TypedAttribute modelDesc () {
        return m_modelDesc;
    }

    @Override
    public boolean configureGauge (List<TypedAttributeWithValue> configParams) {
        for (TypedAttributeWithValue triple : configParams) {
            m_configParams.put (triple.getName (), triple);
            handleConfigParam (triple);
        }
        m_gaugeManagementPort.reportConfigured (this, configParams);
        return true;
    }

    /**
     * Handles a configuration parameter. Subclasses may override this method to handle additional configuration
     * parameters for different kinds of gauges.
     * 
     * @param triple
     *            a triple of name, type, value.
     */
    protected void handleConfigParam (TypedAttributeWithValue triple) {
        if (triple.getName ().equals (CONFIG_SAMPLING_FREQUENCY)) {
            // set the runner timer directly
            setSleepTime ((Long )triple.getValue ());
        }
    }

    abstract protected void initProperty (String name, Object value);

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#reconfigureGauge()
     */
    @Override
    public boolean reconfigureGauge () {
        return configureGauge (new ArrayList<TypedAttributeWithValue> (m_configParams.values ()));
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#queryGaugeState()
     */
    @Override
    public IGaugeState queryGaugeState () {
        return new GaugeState (m_setupParams.values (), m_configParams.values (),
                new HashSet (m_lastCommands.values ()));
    }

    public void issueCommand (IRainbowOperation cmd, Map<String, String> parameters) {
        OperationRepresentation actualCmd = new OperationRepresentation (cmd);
        Map<String, IRainbowOperation> actualsMap = new HashMap<> ();
        String target = cmd.getTarget ();
        String actualTarget = parameters.get (target);
        if (actualTarget != null) {
            // Need to set the target
            actualCmd = new OperationRepresentation (cmd.getName (), cmd.getModelName (),
                    cmd.getModelType (), actualTarget, cmd.getParameters ());
            actualsMap.put (pullOutParam (target), actualCmd);
        }

        for (int i = 0; i < cmd.getParameters ().length; i++) {
            String p = cmd.getParameters ()[i];
            String actualVal = parameters.get (p);
            if (actualVal != null) {
                actualCmd.getParameters ()[i] = actualVal;
                actualsMap.put (pullOutParam (cmd.getParameters ()[i]), actualCmd);
            }
        }
        m_lastCommands.put (cmd.getName (), actualCmd);
        m_lastCommands.putAll (actualsMap);
        m_announcePort.updateModel (actualCmd);
        m_reportingPort.info (RainbowComponentT.GAUGE,
                MessageFormat.format ("G[{0}]: {1}.{2}({3})", id (), actualCmd.getTarget (),
                        actualCmd.getName (), Arrays.toString (actualCmd.getParameters ())));
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#queryCommand()
     */
    @Override
    public IRainbowOperation queryCommand (String value) {

        IRainbowOperation cmd = m_lastCommands.get (pullOutParam (value));
        if (cmd == null) {
            m_reportingPort.warn (getComponentType (), "Could not find a command associated with '" + value + "'.");
        }
        return cmd;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#queryAllCommands()
     */
    @Override
    public Collection<IRainbowOperation> queryAllCommands () {
        return new HashSet<> (m_lastCommands.values ());
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
     */
    @Override
    protected void log (String txt) {
        String msg = "G[" + id () + "] " + txt;
        m_reportingPort.info (RainbowComponentT.GAUGE, txt);
//        RainbowCloudEventHandler.sendTranslatorLog (msg);
        // avoid duplicate output in the master's process
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
     */
    @Override
    protected void runAction () {
        // report Gauge's beacon
        if (m_gaugeBeacon.periodElapsed ()) {
            // send beacon signal to Rainbow
            m_gaugeManagementPort.sendBeacon (this);
            m_gaugeBeacon.mark ();
        }
    }

    /**
     * Assuming target location is stored as setup parameter IGauge.SETUP_LOCATION, this method returns the value of
     * that location.
     * 
     * @return String the string indicating the deployment location
     */
    protected String deploymentLocation () {
        return (String )m_setupParams.get (SETUP_LOCATION).getValue ();
    }

    public IRainbowReportingPort getReportingPort () {
        if (m_reportingPort == null) {
            m_reportingPort = new DisconnectedRainbowDelegateConnectionPort ();
        }
        return m_reportingPort;
    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.GAUGE;
    }

}
