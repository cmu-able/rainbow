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
package org.sa.rainbow.core.ports;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.IModelsManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Properties;

public class RainbowManagementPortFactory {

    static final Logger LOGGER = Logger.getLogger (RainbowManagementPortFactory.class);
    static final String                DEFAULT_FACTORY = "org.sa.rainbow.ports.local.LocalRainbowDelegatePortFactory";

    static IRainbowConnectionPortFactory m_instance;

    private RainbowManagementPortFactory () {
    }

    /**
     * This method looks for a class specified in 'rainbow.deployment.factory.class' and tries to create the deployment
     * factory. If the property is not specified, then the DEFAULT_FACTORY is used.
     * 
     * @return
     */
    protected static IRainbowConnectionPortFactory getFactory () {
        if (m_instance == null) {
            String factory = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_PORT_FACTORY);
            if (factory == null) {
                LOGGER.warn (MessageFormat.format ("No property defined for ''{0}''. Using default ''{1}''.", RainbowConstants.PROPKEY_PORT_FACTORY,
                        DEFAULT_FACTORY));
                factory = DEFAULT_FACTORY;
            }
            try {
                Class<?> f = Class.forName (factory);
                Method method = f.getMethod ("getFactory");
                m_instance = (IRainbowConnectionPortFactory) method.invoke (null);
            }
            catch (ClassNotFoundException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' could not be found on the classpath. Bailing!", factory);
                LOGGER.error (errMsg);
                LOGGER.error (e);
                throw new NotImplementedException (errMsg, e);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
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

    public static IMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate)
            throws RainbowConnectionException {
        return getFactory ().createDelegateSideConnectionPort (delegate);
    }

    public static IMasterConnectionPort createDelegateConnectionPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException {
        return getFactory ().createMasterSideConnectionPort (rainbowMaster);
    }

    public static IDelegateManagementPort createMasterDeploymentPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) throws RainbowConnectionException {
        return getFactory ().createMasterSideManagementPort (rainbowMaster, delegateID, connectionProperties);
    }

    public static IDelegateManagementPort createDelegateDeploymentPort (RainbowDelegate delegate, String delegateID)
            throws RainbowConnectionException {
        return getFactory ().createDelegateSideManagementPort (delegate, delegateID);
    }

    public static IModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        return getFactory ().createModelsManagerUSPort (m);
    }

    public static IModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException {
        return getFactory ().createModelsManagerClientUSPort (client);
    }

    public static IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        return getFactory ().createGaugeSideLifecyclePort ();

    }

}
