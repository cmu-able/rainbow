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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

public class RainbowPortFactory {

    static final Logger LOGGER = Logger.getLogger (RainbowPortFactory.class);
    static final String                  DEFAULT_FACTORY = "org.sa.rainbow.ports.local.LocalRainbowDelegatePortFactory";

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
            String factory = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_PORT_FACTORY);
            if (factory == null) {
                LOGGER.warn (MessageFormat.format ("No property defined for ''{0}''. Using default ''{1}''.",
                        RainbowConstants.PROPKEY_PORT_FACTORY, DEFAULT_FACTORY));
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
                LOGGER.error (errMsg, e);
                throw new NotImplementedException (errMsg, e);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                String errMsg = MessageFormat.format (
                        "The class ''{0}'' does not implement the method ''{1}''. Bailing!", factory, "getFactory");
                LOGGER.error (errMsg, e);
                throw new NotImplementedException (errMsg, e);
            }

        }
        return m_instance;
    }

    public static IDelegateMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate)
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

    public static IDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException {
        return getFactory ().createDelegateConfigurationPort (rainbowDelegate);
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

    public static IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException {
        return getFactory ().createChangeBusAnnouncePort ();
    }

    public static IGaugeLifecycleBusPort createManagerLifecylePort (IGaugeLifecycleBusPort manager)
            throws RainbowConnectionException {
        return getFactory ().createManagerGaugeLifecyclePort (manager);
    }

    public static IGaugeConfigurationPort createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        return getFactory ().createGaugeConfigurationPortClient (gauge);
    }

    public static IGaugeQueryPort createGaugeQueryPortClient (IGaugeIdentifier gauge) throws RainbowConnectionException {
        return getFactory ().createGaugeQueryPortClient (gauge);
    }

    public static IGaugeConfigurationPort createGaugeConfigurationPort (IGauge gauge) throws RainbowConnectionException {
        return getFactory ().createGaugeConfigurationPort (gauge);
    }

    public static IGaugeQueryPort createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException {
        return getFactory ().createGaugeQueryPort (gauge);
    }

    public static IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException {
        return getFactory ().createProbeReportingPortSender (probe);
    }

    public static IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe,
            IProbeConfigurationPort callback) throws RainbowConnectionException {
        return getFactory ().createProbeConfigurationPort (probe, callback);
    }

    public static IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException {
        return getFactory ().createDelegateConfigurationPortClient (delegateID);
    }

    public static IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException {
        return getFactory ().createProbeManagementPort (probe);
    }

    public static IProbeReportSubscriberPort createProbeReportingPortSubscriber (IProbeReportPort callback)
            throws RainbowConnectionException {
        return getFactory ().createProbeReportingPortSubscriber (callback);
    }

    public static IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException {
        return getFactory ().createEffectorSideLifecyclePort ();
    }

    public static IEffectorLifecycleBusPort
    createClientSideEffectorLifecyclePort (IEffectorLifecycleBusPort subscriber)
            throws RainbowConnectionException {
        return getFactory ().createSubscriberSideEffectorLifecyclePort (subscriber);
    }

    public static IEffectorExecutionPort createEffectorExecutionPort (IEffector effector)
            throws RainbowConnectionException {
        return getFactory ().createEffectorExecutionPort (effector);
    }

    public static IEffectorExecutionPort createEffectorExecutionPortClient (IEffectorIdentifier effector)
            throws RainbowConnectionException {
        return getFactory ().createEffectorExecutionPort (effector);
    }

    public static IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort ()
            throws RainbowConnectionException {
        return getFactory ().createModelChangeBusSubscriptionPort ();
    }

    public static IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException {
        return getFactory ().createMasterReportingPort ();
    }

    public static IRainbowReportingSubscriberPort
    createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportTo)
            throws RainbowConnectionException {
        return getFactory ().createReportingSubscriberPort (reportTo);
    }

    public static IModelDSBusPublisherPort createModelDSPublishPort (Identifiable component)
            throws RainbowConnectionException {
        return getFactory ().createModelDSPublishPort (component);
    }

    public static IModelDSBusSubscriberPort createModelDSSubscribePort (Identifiable component)
            throws RainbowConnectionException {
        return getFactory ().createModelDSubscribePort (component);
    }

    public static IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException {
        return getFactory ().createModelsManagerProviderPort (modelsManager);
    }

    public static IMasterCommandPort createMasterCommandPort () throws RainbowConnectionException {

        if (Rainbow.instance ().isMaster ())
            return getFactory ().createMasterCommandProviderPort (Rainbow.instance ().getRainbowMaster ());

        return getFactory ().createMasterCommandRequirerPort ();
    }

    public static IModelsManagerPort createModelsManagerRequirerPort () throws RainbowConnectionException {
        if (Rainbow.instance ().isMaster ()) {
            final ModelsManager mm = Rainbow.instance ().getRainbowMaster ().modelsManager ();
            return new IModelsManagerPort () {

                @Override
                public Collection<? extends String> getRegisteredModelTypes () {
                    return mm.getRegisteredModelTypes ();
                }

                @Override
                public IModelInstance getModelInstance (ModelReference modelRef) {
                    return mm.getModelInstance (modelRef);
                }
            };
        }
        return getFactory ().createModeslManagerRequirerPort ();
    }

    public static <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S>
    createAdaptationEnqueuePort (ModelReference model) {
        return getFactory ().createAdaptationEnqueuePort (model);
    }

    public static <S extends IEvaluable> IRainbowAdaptationDequeuePort<S>
    createAdaptationDequeuePort (ModelReference model) {
        return getFactory ().createAdaptationDequeuePort (model);
    }
}
