package org.sa.rainbow.gauges;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.management.ports.RainbowPortFactory;

public class GaugeManager extends AbstractRainbowRunnable implements IRainbowGaugeLifecycleBusPort {

    public static final String ID = "Gauge Manager";
    static Logger              LOGGER = Logger.getLogger (GaugeManager.class);

    private IRainbowGaugeLifecycleBusPort m_gaugeLifecyclePort;

    private Map<Long, IGaugeConfigurationInterface> configurationPorts = new HashMap<> ();
    private Map<Long, IGaugeQueryInterface>         queryPorts         = new HashMap<> ();

    public GaugeManager () {
        super (ID);
    }

    public void initialize () throws RainbowConnectionException {
        initializeConnections ();
        // Read all the gauge information, including the deployment stuff      
    }

    protected void initializeConnections () throws RainbowConnectionException {
        m_gaugeLifecyclePort = RainbowPortFactory.createManagerLifecylePort (this);
    }

    @Override
    public void dispose () {

    }

    @Override
    protected void log (String txt) {
        LOGGER.info (txt);
    }

    @Override
    protected void runAction () {

    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        // Log creation
        LOGGER.info (MessageFormat.format ("Gauge Manager: A gauge was created {0}", gauge.id ()));
        // Set configuration ports
        try {
            IGaugeConfigurationInterface req = RainbowPortFactory.createGaugeConfigurationPortClient (gauge);
            configurationPorts.put (gauge.id_long (), req);
            IGaugeQueryInterface query = RainbowPortFactory.createGaugeQueryPortClient (gauge);
            queryPorts.put (gauge.id_long (), query);
        }
        catch (RainbowConnectionException e) {
            LOGGER.error (
                    MessageFormat.format ("Could not create a connection to configure the gauge: {0}", gauge.id ()), e);
        }
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        LOGGER.info (MessageFormat.format ("Gauge Manager: A gauge was deleted {0}", gauge.id ()));
        IGaugeConfigurationInterface p = configurationPorts.get (gauge.id_long ());
        configurationPorts.remove (gauge.id_long ());
        queryPorts.remove (gauge.id_long ());
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {

    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {

    }

}
