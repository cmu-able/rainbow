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
package org.sa.rainbow.core.ports.guava;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IDelegateMasterConnectionPort;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBMasterCommandProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBMasterCommandRequirerPort;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class GuavaRainbowPortFactory implements IRainbowConnectionPortFactory {

	private static GuavaRainbowPortFactory m_instance;

	private GuavaRainbowPortFactory() {

	}

	@Override
	public IDelegateMasterConnectionPort createDelegateSideConnectionPort(RainbowDelegate delegate) {
		return new GuavaDelegateConnectionPort(delegate);

	}

	@Override
	public IMasterConnectionPort createMasterSideConnectionPort(RainbowMaster rainbowMaster) {

		return new GuavaMasterConnectionPort(rainbowMaster);
	}

	@Override
	public IDelegateManagementPort createDelegateSideManagementPort(RainbowDelegate delegate, String delegateID) {
		return new GuavaDelegateManagementPort(delegate);
	}

	@Override
	public IDelegateManagementPort createMasterSideManagementPort(RainbowMaster rainbowMaster, String delegateID,
			Properties connectionProperties) {
		return new GuavaMasterSideManagementPort(rainbowMaster, delegateID, connectionProperties);

	}

	public static IRainbowConnectionPortFactory getFactory() {
		if (m_instance == null) {
			m_instance = new GuavaRainbowPortFactory();
		}
		return m_instance;
	}

	@Override
	public IModelUSBusPort createModelsManagerUSPort(IModelUpdater m) throws RainbowConnectionException {
		return new GuavaModelManagerModelUpdatePort(m);
	}

	@Override
	public IModelUSBusPort createModelsManagerClientUSPort(Identifiable client) throws RainbowConnectionException {
		return new GuavaGaugeModelUSBusPort(client);
	}

	@Override
	public IGaugeLifecycleBusPort createGaugeSideLifecyclePort() throws RainbowConnectionException {
		return new GuavaGaugesideLifecyclePort();
	}

	
	@Override
	public IGaugeLifecycleBusPort createManagerGaugeLifecyclePort(IGaugeLifecycleBusPort manager)
			throws RainbowConnectionException {
		return new GuavaReceiverSideGaugLifecyclePort(manager);
	}
	

	Map<String,GuavaGaugePort> m_gaugePorts = new HashMap<>();
	
	protected GuavaGaugePort createGaugePort(IGaugeIdentifier gauge) {
		GuavaGaugePort p = m_gaugePorts.get(gauge.id());
		if (p == null) {
			p = new GuavaGaugePort();
			m_gaugePorts.put(gauge.id(), p);
		}
		
		return p;
	}
	
	protected GuavaGaugePort createGaugePort(IGauge gauge) {
		GuavaGaugePort p = m_gaugePorts.get(gauge.id());
		if (p == null) {
			p = new GuavaGaugePort();
			m_gaugePorts.put(gauge.id(), p);
		}
		p.setGauge(gauge);
		return p;
	}
	
	@Override
	public IGaugeConfigurationPort createGaugeConfigurationPortClient(IGaugeIdentifier gauge)
			throws RainbowConnectionException {
		
		return createGaugePort(gauge);
		
	}
	
	@Override
	public IGaugeConfigurationPort createGaugeConfigurationPort(IGauge gauge) throws RainbowConnectionException {
		return createGaugePort(gauge);
	}

	@Override
	public IGaugeQueryPort createGaugeQueryPortClient(IGaugeIdentifier gauge) throws RainbowConnectionException {
		return createGaugePort(gauge);
	}



	@Override
	public IGaugeQueryPort createGaugeQueryPort(IGauge gauge) throws RainbowConnectionException {
		return createGaugePort(gauge);
	}

	@Override
	public IProbeReportPort createProbeReportingPortSender(IProbe probe) throws RainbowConnectionException {
		return new GuavaProbeReportingPortSender(probe);
	}
	
	@Override
	public IProbeReportSubscriberPort createProbeReportingPortSubscriber(IProbeReportPort callback)
			throws RainbowConnectionException {
		return new GuavaProbeReportingPortingPortSubscriber(callback);
	}

	
	Map<String, LocalDelegateConfigurationProviderPort> delegatePorts = new HashMap<>();

	@Override
	public IDelegateConfigurationPort createDelegateConfigurationPort(RainbowDelegate rainbowDelegate)
			throws RainbowConnectionException {
		LocalDelegateConfigurationProviderPort p = delegatePorts.get(rainbowDelegate.getId());
		if (p == null) {
			p = new LocalDelegateConfigurationProviderPort();
			delegatePorts.put(rainbowDelegate.getId(), p);
		}
		p.setDelegate(rainbowDelegate);
		return p;
	}

	@Override
	public IDelegateConfigurationPort createDelegateConfigurationPortClient(String delegateID)
			throws RainbowConnectionException {
		LocalDelegateConfigurationProviderPort p = delegatePorts.get(delegateID);
		if (p == null) {
			p = new LocalDelegateConfigurationProviderPort();
			delegatePorts.put(delegateID, p);
		}
		return p;
	}

	@Override
	public IProbeLifecyclePort createProbeManagementPort(IProbe probe) throws RainbowConnectionException {
		return new GuavaProbeLifecyclePort(probe);
	}

	
	@Override
	public IProbeConfigurationPort createProbeConfigurationPort(Identifiable probe, IProbeConfigurationPort callback)
			throws RainbowConnectionException {
		return new GuavaProbeConfigurationPort(probe, callback);
		
	}

	

	@Override
	public IEffectorLifecycleBusPort createEffectorSideLifecyclePort() throws RainbowConnectionException {
		return new GuavaEffectorSideLifecyclePort();
		
	}

	@Override
	public IEffectorLifecycleBusPort createSubscriberSideEffectorLifecyclePort(IEffectorLifecycleBusPort delegate)
			throws RainbowConnectionException {
		return new GuavaSubscriberSideEffectorLifecyclePort(delegate);
	}

	
	Map<String,LocalThreadedEffectorExecutionPort> m_effectorExecutionPorts = new HashMap<>();
	
	@Override
	public IEffectorExecutionPort createEffectorExecutionPort(IEffector effector) throws RainbowConnectionException {
		LocalThreadedEffectorExecutionPort p = m_effectorExecutionPorts.get(effector.id());
		if (p == null) {
			p = new LocalThreadedEffectorExecutionPort();
			m_effectorExecutionPorts.put(effector.id(), p);
		}
		p.setEffector(effector);
		return p;
	}

	@Override
	public IEffectorExecutionPort createEffectorExecutionPort(IEffectorIdentifier effector)
			throws RainbowConnectionException {
		LocalThreadedEffectorExecutionPort p = m_effectorExecutionPorts.get(effector.id());
		if (p == null) {
			p = new LocalThreadedEffectorExecutionPort();
			m_effectorExecutionPorts.put(effector.id(), p);
		}
		return p;
	}

	@Override
	public IRainbowReportingPort createMasterReportingPort() throws RainbowConnectionException {
		return new GuavaMasterReportingPort();
	}
	
	@Override
	public IModelChangeBusPort createChangeBusAnnouncePort() throws RainbowConnectionException {
		return new GuagaChangeBusAnnouncePort();
	}

	@Override
	public IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort() throws RainbowConnectionException {
		return new GuavaModelChangeBusSubscriptionPort();
	}

	@Override
	public IRainbowReportingSubscriberPort createReportingSubscriberPort(IRainbowReportingSubscriberCallback reportTo)
			throws RainbowConnectionException {
		return new GuavaRainbowReportingSubscriberPort(reportTo);
	}

	@Override
	public IModelDSBusPublisherPort createModelDSPublishPort(Identifiable client) throws RainbowConnectionException {
		return new GuavaModelDSPublishPort(client);
	}

	@Override
	public IModelDSBusSubscriberPort createModelDSubscribePort(Identifiable client) throws RainbowConnectionException {
		return new GuavaModelDSPublishPort(client);
	}

	GuavaModelsManagerProviderPort mm_port = null;
	
	@Override
	public IModelsManagerPort createModelsManagerProviderPort(IModelsManager modelsManager)
			throws RainbowConnectionException {
		if (mm_port == null) {
			mm_port = new GuavaModelsManagerProviderPort();
		}
		mm_port.setModelsManager(modelsManager);
		return mm_port;
	}

	@Override
	public IModelsManagerPort createModeslManagerRequirerPort() throws RainbowConnectionException {
		if (mm_port == null) {
			mm_port = new GuavaModelsManagerProviderPort();
		}
		return mm_port;
	}

	private final Map<String, LocalAdaptationQConnector> m_adaptationConnectors = new HashMap<>();

	@Override
	public <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S> createAdaptationEnqueuePort(ModelReference model) {
		return getAdaptationConnectorForModel(model);
	}

	private <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S> getAdaptationConnectorForModel(
			ModelReference model) {
		synchronized (m_adaptationConnectors) {
			LocalAdaptationQConnector<S> conn = m_adaptationConnectors.get(model.toString());
			if (conn == null) {
				conn = new LocalAdaptationQConnector<>();
				m_adaptationConnectors.put(model.toString(), conn);
			}
			return conn;
		}
	}

	@Override
	public <S extends IEvaluable> IRainbowAdaptationDequeuePort<S> createAdaptationDequeuePort(ModelReference model) {
		synchronized (m_adaptationConnectors) {

			LocalAdaptationQConnector<S> conn = m_adaptationConnectors.get(model.toString());
			if (conn == null) {
				conn = new LocalAdaptationQConnector<>();
				m_adaptationConnectors.put(model.toString(), conn);
			}
			return conn;
		}
	}

	
	// This needs to be thought about because by necessity, these commands can come from
	// a separate process (e.g., the shell).
	@Override
	public IMasterCommandPort createMasterCommandProviderPort(IRainbowMaster rainbowMaster)
			throws RainbowConnectionException {
		try {
//			return new JSONRPCMasterCommandProvirerPort(rainbowMaster);
			return new ESEBMasterCommandProviderPort(rainbowMaster);
		} catch (IOException | ParticipantException e) {
			throw new RainbowConnectionException("Failed to connect", e);
		}
	}

	@Override
	public IMasterCommandPort createMasterCommandRequirerPort() throws RainbowConnectionException {
		try {
			return new ESEBMasterCommandRequirerPort();
		} catch (IOException |

				ParticipantException e)

		{
			throw new RainbowConnectionException("Failed to connect", e);
		}
	}

}
