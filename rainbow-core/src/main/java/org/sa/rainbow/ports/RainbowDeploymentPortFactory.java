package org.sa.rainbow.ports;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowConstants;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.Rainbow;

public class RainbowDeploymentPortFactory {

    static Logger                      LOGGER          = Logger.getLogger (RainbowDeploymentPortFactory.class);
    static final String                DEFAULT_FACTORY = "org.sa.rainbow.ports.local.LocalRainbowDelegatePortFactory";

    static IRainbowDeploymentPortFactory m_instance;

    private RainbowDeploymentPortFactory () {
    }

    /**
     * This method looks for a class specified in 'rainbow.deployment.factory.class' and tries to create the deployment
     * factory. If the property is not specified, then the DEFAULT_FACTORY is used.
     * 
     * @return
     */
    protected static IRainbowDeploymentPortFactory getFactory () {
        if (m_instance == null) {
            String factory = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_PORT_FACTORY);
            if (factory == null) {
                LOGGER.warn (MessageFormat.format ("No property defined for ''{0}''. Using default ''{1}''.", RainbowConstants.PROPKEY_DEPLOYMENT_PORT_FACTORY,
                        DEFAULT_FACTORY));
                factory = DEFAULT_FACTORY;
            }
            try {
                Class<?> f = RainbowDeploymentPortFactory.class.forName (factory);
                Method method = f.getMethod ("getFactory", new Class[0]);
                m_instance = (IRainbowDeploymentPortFactory )method.invoke (null, new Object[0]);
            }
            catch (ClassNotFoundException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' could not be found on the classpath. Bailing!", factory);
                LOGGER.error (errMsg);
                LOGGER.error (e);
                throw new NotImplementedException (errMsg, e);
            }
            catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' does not implement the method ''{1}''. Bailing!", factory, "getFactory");
                LOGGER.error (errMsg);
                LOGGER.error (e);
                throw new NotImplementedException (errMsg, e);
            }

        }
        return m_instance;
    }

    public static IRainbowMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate) {
        return getFactory ().createDelegateMasterConnectionPort (delegate);
    }

    public static IRainbowMasterConnectionPort createDelegateConnectionPort (RainbowMaster rainbowMaster) {
        return getFactory ().createDelegateConnectionPort (rainbowMaster);
    }

    public static IRainbowDeploymentPort createMasterDeploymentPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {
        return getFactory ().createMasterDeploymentePort (rainbowMaster, delegateID, connectionProperties);
    }

    public static IRainbowDeploymentPort createDelegateDeploymentPort (RainbowDelegate delegate, String delegateID) {
        return getFactory ().createDelegateDeploymentPortPort (delegate, delegateID);
    }

}
