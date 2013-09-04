package org.sa.rainbow.core.ports;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeConfigurationInterface;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeQueryInterface;
import org.sa.rainbow.core.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.translator.probes.IProbe;

public class RainbowPortFactory {

    static Logger                      LOGGER          = Logger.getLogger (RainbowPortFactory.class);
    static final String                DEFAULT_FACTORY = "org.sa.rainbow.ports.local.LocalRainbowDelegatePortFactory";

    static IRainbowConnectionPortFactory m_instance;

    private RainbowPortFactory () {
    }

    /**
     * This method looks for a class specified in 'rainbow.deployment.factory.class' and tries to create the deployment
     * factory. If the property is not specified, then the DEFAULT_FACTORY is used.
     * 
     * @return
     */
    protected static IRainbowConnectionPortFactory getFactory () {
        if (m_instance == null) {
            String factory = Rainbow.getProperty (RainbowConstants.PROPKEY_PORT_FACTORY);
            if (factory == null) {
                LOGGER.warn (MessageFormat.format ("No property defined for ''{0}''. Using default ''{1}''.", RainbowConstants.PROPKEY_PORT_FACTORY,
                        DEFAULT_FACTORY));
                factory = DEFAULT_FACTORY;
            }
            try {
                Class<?> f = Class.forName (factory);
                Method method = f.getMethod ("getFactory", new Class[0]);
                m_instance = (IRainbowConnectionPortFactory )method.invoke (null, new Object[0]);
            }
            catch (ClassNotFoundException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' could not be found on the classpath. Bailing!", factory);
                LOGGER.error (errMsg, e);
                throw new NotImplementedException (errMsg, e);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' does not implement the method ''{1}''. Bailing!", factory, "getFactory");
                LOGGER.error (errMsg, e);
                throw new NotImplementedException (errMsg, e);
            }

        }
        return m_instance;
    }

    public static IRainbowMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate)
            throws RainbowConnectionException {
        return getFactory ().createDelegateSideConnectionPort (delegate);
    }

    public static IRainbowMasterConnectionPort createDelegateConnectionPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException {
        return getFactory ().createMasterSideConnectionPort (rainbowMaster);
    }

    public static IRainbowManagementPort createMasterDeploymentPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) throws RainbowConnectionException {
        return getFactory ().createMasterSideManagementPort (rainbowMaster, delegateID, connectionProperties);
    }

    public static IRainbowManagementPort createDelegateDeploymentPort (RainbowDelegate delegate, String delegateID)
            throws RainbowConnectionException {
        return getFactory ().createDelegateSideManagementPort (delegate, delegateID);
    }

    public static IRainbowDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException {
        return getFactory ().createDelegateConfigurationPort (rainbowDelegate);
    }

    public static IRainbowModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        return getFactory ().createModelsManagerUSPort (m);
    }

    public static IRainbowModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException {
        return getFactory ().createModelsManagerClientUSPort (client);
    }

    public static IRainbowGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        return getFactory ().createGaugeSideLifecyclePort ();

    }


    public static IRainbowModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException {
        return getFactory ().createChangeBusAnnouncePort ();
    }

    public static IRainbowGaugeLifecycleBusPort createManagerLifecylePort (IRainbowGaugeLifecycleBusPort manager)
            throws RainbowConnectionException {
        return getFactory ().createManagerGaugeLifecyclePort (manager);
    }

    public static IGaugeConfigurationInterface createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        return getFactory ().createGaugeConfigurationPortClient (gauge);
    }

    public static IGaugeQueryInterface createGaugeQueryPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        return getFactory ().createGaugeQueryPortClient (gauge);
    }

    public static IGaugeConfigurationInterface createGaugeConfigurationPort (IGauge gauge) throws RainbowConnectionException {
        return getFactory ().createGaugeConfigurationPort (gauge);
    }

    public static IGaugeQueryInterface createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException {
        return getFactory ().createGaugeQueryPort (gauge);
    }

    public static IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException {
        return getFactory ().createProbeReportingPortSender (probe);
    }

    public static IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe,
            IProbeConfigurationPort callback) throws RainbowConnectionException {
        return getFactory ().createProbeConfigurationPort (probe, callback);
    }

    public static IRainbowDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException {
        return getFactory ().createDelegateConfigurationPortClient (delegateID);
    }

}
