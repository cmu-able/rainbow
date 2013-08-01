package org.sa.rainbow.gauges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;
import org.sa.rainbow.util.Beacon;
/**
 * Abstract definition of a gauge that does not use probes to gather system information
 * This allows for subclasses to implement gauges that get information from other gauges,
 * gauges that get information from probes, or gauges that get information from elsewhere.
 * 
 * @author Bradley Schmerl (schmerl@cs.cmu.edu)
 *
 */
public abstract class AbstractGauge extends AbstractRainbowRunnable implements
IGauge {

    private final String m_id;

    protected IRainbowModelUSBusPort                          m_announcePort;
    protected

    /** Used to determine when to fire off beacon to consumers;
     * the period will be set by the Gauge implementation subclase */
    protected Beacon m_gaugeBeacon = null;
    protected TypedAttribute m_gaugeDesc = null;
    protected TypedAttribute m_modelDesc = null;
    protected Map<String,TypedAttributeWithValue> m_setupParams = null;
    protected Map<String,TypedAttributeWithValue> m_configParams = null;
    protected Map<String,String> m_mappings = null;
    protected Map<String, IRainbowModelCommandRepresentation> m_lastCommands = null;

    /**
     * Main Constructor for the Gauge.
     * @param threadName  the name of the Gauge thread
     * @param id  the unique ID of the Gauge
     * @param beaconPeriod  the liveness beacon period of the Gauge
     * @param gaugeDesc  the type-name description of the Gauge
     * @param modelDesc  the type-name description of the Model the Gauge updates
     * @param setupParams  the list of setup parameters with their values
     * @param mappings  the list of Gauge Value to Model Property mappings
     */
    public AbstractGauge(String threadName, String id, long beaconPeriod,
            TypedAttribute gaugeDesc, TypedAttribute modelDesc, 
            List<TypedAttributeWithValue> setupParams,
            List<IRainbowModelCommandRepresentation> mappings) {
        super(threadName);
        this.m_id = id;

        m_gaugeBeacon = new Beacon(beaconPeriod);
        m_gaugeDesc = gaugeDesc;
        m_modelDesc = modelDesc;

        m_setupParams = new HashMap<String,TypedAttributeWithValue>();
        m_configParams = new HashMap<String,TypedAttributeWithValue>();
        m_mappings = new HashMap<String,String>();
        m_lastCommands = new HashMap<String, IRainbowModelCommandRepresentation> ();

        // store the setup parameters
        for (TypedAttributeWithValue param : setupParams) {
            m_setupParams.put(param.getName(), param);
        }
//        // store the mapping info
//        for (ValuePropertyMappingPair mapping : mappings) {
//            m_mappings.put(mapping.valueName(), mapping.propertyName());
//        }


        // register this Gauge with Rainbow, and report created
        Rainbow.registerGauge(this);
        m_gaugeEventHandler.reportCreated();
        m_gaugeBeacon.mark();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#id()
     */
    @Override
    public String id () {
        return m_id;
    }

    @Override
    public void dispose() {
        m_gaugeEventHandler.reportDeleted();

        Rainbow.eventService().unlisten(m_gaugeEventHandler);

        m_setupParams.clear();
        m_configParams.clear();
        m_mappings.clear();
        m_lastCommands.clear ();

        // null-out data members
        m_gaugeEventHandler = null;
//		m_id = null;  // keep value for log output
        m_gaugeDesc = null;
        m_modelDesc = null;
        m_setupParams = null;
        m_configParams = null;
        m_mappings = null;
        m_lastCommands = null;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#beaconPeriod()
     */
    @Override
    public long beaconPeriod () {
        return m_gaugeBeacon.period();
    }


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#gaugeDesc()
     */
    @Override
    public TypeNamePair gaugeDesc () {
        return m_gaugeDesc;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#modelDesc()
     */
    @Override
    public TypeNamePair modelDesc () {
        return m_modelDesc;
    }

    @Override
    public boolean configureGauge(List<TypedAttributeWithValue> configParams) {
        for (TypedAttributeWithValue triple : configParams) {
            m_configParams.put(triple.getName (), triple);
            handleConfigParam(triple);
        }
        m_gaugeEventHandler.reportConfigured(configParams);
        return true;
    }

    /**
     * Handles a configuration parameter. Subclasses may override this method to 
     * handle additional configuration parameters for different kinds of gauges.
     * @param triple a triple of name, type, value.
     */
    protected void handleConfigParam(TypedAttributeWithValue triple) {
        if (triple.getName ().equals (CONFIG_SAMPLING_FREQUENCY)) {
            // set the runner timer directly
            setSleepTime((Long )triple.getValue());
        }
        if (m_mappings.keySet().contains (triple.getName ())) {
            initProperty (triple.getName (), triple.getValue ());
        }
    }

    abstract protected void initProperty (String name, Object value);


    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#reconfigureGauge()
     */
    @Override
    public boolean reconfigureGauge() {
        return configureGauge(new ArrayList<TypedAttributeWithValue>(m_configParams.values()));
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#queryGaugeState(java.util.List, java.util.List, java.util.List)
     */
    @Override
    public IGaugeState queryGaugeState () {
        return new GaugeState (m_setupParams.values (), m_configParams.values (), m_commands.values ());
    }



    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#querySingleValue(org.sa.rainbow.util.AttributeValueTriple)
     */
    public TypedAttributeWithValue querySingleValue () {

        if (m_lastCommands.containsKey (value.attribute ())) {
            value.setSecondValue (m_lastCommands.get (value.attribute ()));
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.gauges.IGauge#queryAllValues(java.util.List)
     */
    public boolean queryAllValues (List<AttributeValueTriple> values) {
        for (AttributeValueTriple value : m_lastCommands.values ()) {
            values.add(value);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
     */
    @Override
    protected void log (String txt) {
        String msg = "G[" + id() + "] " + txt;
        RainbowCloudEventHandler.sendTranslatorLog(msg);
        // avoid duplicate output in the master's process
        if (! Rainbow.isMaster()) {
            m_logger.info(msg);
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
     */
    @Override
    protected void runAction () {
        // report Gauge's beacon
        if (m_gaugeBeacon.periodElapsed()) {
            // send beacon signal to Rainbow
            m_gaugeEventHandler.sendBeacon();
            m_gaugeBeacon.mark();
        }
    }


    /**
     * Assuming target location is stored as setup parameter IGauge.SETUP_LOCATION,
     * this method returns the value of that location.
     * @return String  the string indicating the deployment location
     */
    protected String deploymentLocation () {
        return m_setupParams.get(SETUP_LOCATION).getValue();
    }

}
