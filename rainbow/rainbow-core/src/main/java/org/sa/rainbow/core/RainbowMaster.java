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
package org.sa.rainbow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.injection.RainbowRuntimeModule;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.IRainbowGUI;
import org.sa.rainbow.gui.RainbowGUI;
import org.sa.rainbow.translator.effectors.EffectorManager;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.Util;
import org.sa.rainbow.util.YamlUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class RainbowMaster extends AbstractRainbowRunnable implements IMasterCommandPort, IRainbowMaster {


	static final Logger LOGGER = Logger.getLogger(Rainbow.class.getCanonicalName());

	final Map<String, IDelegateManagementPort> m_delegates = new HashMap<>();
	final Map<String, IDelegateConfigurationPort> m_delegateConfigurtationPorts = new HashMap<>();
	final Map<String, Properties> m_delegateInfo = new HashMap<>();

	IMasterConnectionPort m_delegateConnection;
	private final Map<String, Beacon> m_heartbeats = new HashMap<>();

	private ModelsManager m_modelsManager;

	private ProbeDescription m_probeDesc;
	private EffectorDescription m_effectorDesc;
	private GaugeDescription m_gaugeDesc;
//    private UtilityPreferenceDescription     m_prefDesc;

	private GaugeManager m_gaugeManager;

	private Collection<IRainbowAnalysis> m_analyses = Collections.emptySet();

	private Map<String, IAdaptationManager<?>> m_adaptationManagers = new HashMap<>();

	private final Map<String, IAdaptationExecutor<?>> m_adaptationExecutors = new HashMap<>();

	private Collection<EffectorManager> m_effectorManagers = Collections.emptySet();

	private final Map<String, Beacon> m_terminatedDelegates = Collections
			.synchronizedMap(new HashMap<String, Beacon>());

	private final Set<String> m_nonCompliantDelegates = Collections.<String>synchronizedSet(new HashSet<String>());

	private Boolean m_initialized = Boolean.FALSE;

	public boolean m_autoStart = false;

	public void setRainbowEnvironment(IRainbowEnvironment env) {
		m_rainbowEnvironment = env;
	}

	public RainbowMaster() {
		super("Rainbow Master");
		m_rainbowEnvironment.setMaster(this);
	}

	public void initialize() throws RainbowException {
		synchronized (m_initialized) {
			readConfiguration();
			initializeConnections();
			super.initialize(m_reportingPort);
			initializeRainbowComponents();

			m_initialized = true;
		}
	}

	private void readConfiguration() {
		probeDesc();
		effectorDesc();
		gaugeDesc();
//        preferenceDesc (); // This has been moved to a model
	}

	/**
	 * Initializes all of the components of the Rainbow master: adaptation managers,
	 * effector managers, analyses, executors
	 * 
	 * @throws RainbowException
	 */
	private void initializeRainbowComponents() throws RainbowException {
		// Create a models manager
		m_modelsManager = new ModelsManager();
		m_modelsManager.initialize(m_reportingPort);
		m_modelsManager.start();

		// Create the global gauge manager that knows about all gauges
		m_gaugeManager = new GaugeManager(gaugeDesc());
		m_gaugeManager.initialize(m_reportingPort);
		m_gaugeManager.start();

		// Create effector managers from the Rainbow properties file
		int effectorManagerSize = m_rainbowEnvironment
				.getProperty(RainbowConstants.PROPKEY_EFFECTOR_MANAGER_COMPONENT_SIZE, 0);
		m_effectorManagers = new LinkedList<>();
		for (int i = 0; i < effectorManagerSize; i++) {
			String emProp = RainbowConstants.PROPKEY_EFFECTOR_MANAGER_COMPONENT + "_" + i;
			String em = m_rainbowEnvironment.getProperty(emProp);
			if (em != null) {
				em = em.trim();
				try {
					@SuppressWarnings("unchecked")
					Class<? extends EffectorManager> cls = (Class<? extends EffectorManager>) Class.forName(em);
					EffectorManager effMan = cls.newInstance();
					m_effectorManagers.add(effMan);
					effMan.setEffectors(effectorDesc());
					effMan.initialize(m_reportingPort);
					effMan.start();
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| ClassCastException e) {
					m_reportingPort.error(RainbowComponentT.MASTER,
							MessageFormat.format("Could not start effector manager ''{0}''", em), e);
				}
			}
		}

		// Create analyses from the Rainbow properties file
		int analysisSize = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ANALYSIS_COMPONENT_SIZE, 0);
		m_analyses = new LinkedList<>();
		for (int i = 0; i < analysisSize; i++) {
			String analysisProp = RainbowConstants.PROPKEY_ANALYSIS_COMPONENTS + "_" + i;
			String an = m_rainbowEnvironment.getProperty(analysisProp);
			if (an != null) {
				an = an.trim();
				try {
					m_reportingPort.info(RainbowComponentT.MASTER, "Starting " + an);
					@SuppressWarnings("unchecked")
					Class<? extends IRainbowAnalysis> cls = (Class<? extends IRainbowAnalysis>) Class.forName(an);
					IRainbowAnalysis analysis = cls.newInstance();
					m_analyses.add(analysis);
					analysis.initialize(m_reportingPort);
					analysis.start();
					String model = m_rainbowEnvironment
							.getProperty(RainbowConstants.PROPKEY_ANALYSIS_COMPONENTS + ".model_" + i);
					if (model != null) {
						analysis.setProperty("model", model);
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| ClassCastException e) {
					m_reportingPort.error(RainbowComponentT.MASTER,
							MessageFormat.format("Could not start the analysis ''{0}''", an), e);
				}
			}
		}

		// Create adaptation managers from the Rainbow properties file
		int adaptationManagerSize = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ADAPTATION_MANAGER_SIZE,
				0);
		m_adaptationManagers = new HashMap<>();
		for (int i = 0; i < adaptationManagerSize; i++) {

			String adaptationManagerProp = RainbowConstants.PROPKEY_ADAPTATION_MANAGER_CLASS + "_" + i;
			String adaptationManagerClass = m_rainbowEnvironment.getProperty(adaptationManagerProp);
			if (adaptationManagerClass != null) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends IAdaptationManager<?>> cls = (Class<? extends IAdaptationManager<?>>) Class
							.forName(adaptationManagerClass.trim());
					IAdaptationManager<?> adaptationManager = cls.newInstance();
					String amModel = RainbowConstants.PROPKEY_ADAPTATION_MANAGER_MODEL + "_" + i;
					String modelReference = m_rainbowEnvironment.getProperty(amModel);
					if (modelReference != null) {
						ModelReference model = Util.decomposeModelReference(modelReference);
						m_adaptationManagers.put(modelReference, adaptationManager);
						adaptationManager.initialize(m_reportingPort);
						adaptationManager.setModelToManage(model);

						adaptationManager.start();
					} else {
						m_reportingPort.error(RainbowComponentT.MASTER, MessageFormat.format(
								"There is no model reference for adapation manager ''{0}''. Need to set the property ''{1}''.",
								adaptationManagerClass, amModel));
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| ClassCastException e) {
					m_reportingPort.error(RainbowComponentT.MASTER, MessageFormat
							.format("Could not start the adaptation manager ''{0}''.", adaptationManagerClass), e);
				}
			} else {
				m_reportingPort.warn(RainbowComponentT.MASTER,
						MessageFormat.format("Could not find property ''{0}''", adaptationManagerProp));
			}
		}

		// Create adaptation executors from the Rainbow properties file
		int adaptationExecutorSize = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_SIZE,
				0);

		for (int i = 0; i < adaptationExecutorSize; i++) {
			String adaptationExecutorProp = RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_CLASS + "_" + i;
			String adaptationExecutorClass = m_rainbowEnvironment.getProperty(adaptationExecutorProp);
			if (adaptationExecutorClass != null) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends IAdaptationExecutor<?>> cls = (Class<? extends IAdaptationExecutor<?>>) Class
							.forName(adaptationExecutorClass.trim());
					IAdaptationExecutor<?> adaptationExecutor = cls.newInstance();
					adaptationExecutor.initialize(m_reportingPort);
					String amModel = RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_MODEL + "_" + i;
					String modelReference = m_rainbowEnvironment.getProperty(amModel);
					if (modelReference != null) {
						ModelReference model = Util.decomposeModelReference(modelReference);
						adaptationExecutor.setModelToManage(model);
						m_adaptationExecutors.put(modelReference, adaptationExecutor);
						adaptationExecutor.start();
					} else {
						m_reportingPort.error(RainbowComponentT.MASTER, MessageFormat.format(
								"There is no model reference for adapation executor ''{0}''. Need to set the property ''{1}''.",
								adaptationExecutorClass, amModel));
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| ClassCastException e) {
					m_reportingPort.error(RainbowComponentT.MASTER, MessageFormat
							.format("Could not start the adaptation executor ''{0}''.", adaptationExecutorClass), e);
				}

			} else {
				m_reportingPort.warn(RainbowComponentT.MASTER,
						MessageFormat.format("Could not find property ''{0}''", adaptationExecutorProp));
			}
		}

	}

	private void initializeConnections() throws RainbowConnectionException {
		m_delegateConnection = RainbowPortFactory.createDelegateConnectionPort(this);
		m_reportingPort = RainbowPortFactory.createMasterReportingPort();
	}

	@Override
	public ModelsManager modelsManager() {
		return m_modelsManager;
	}

	public GaugeManager gaugeManager() {
		return m_gaugeManager;
	}

	/**
	 * Connects a new delegate and sends the appropriate configuration information
	 * to the delegate
	 * 
	 * @param delegateID
	 * @param connectionProperties
	 */

	public IDelegateManagementPort connectDelegate(String delegateID, Properties connectionProperties) {
		LOGGER.debug(MessageFormat.format("Master received connection request from: {0} at {1}", delegateID,
				connectionProperties.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION, "Unknown Location")));
		try {
			m_delegateInfo.put(delegateID, connectionProperties);
			IDelegateManagementPort delegatePort = RainbowPortFactory.createMasterDeploymentPort(this, delegateID,
					connectionProperties);
			// Check to see if there is already a registered delegate running on the machine
			m_delegates.put(delegateID, delegatePort);
			IDelegateConfigurationPort delegateConfigurationPort = RainbowPortFactory
					.createDelegateConfigurationPortClient(delegateID);
			m_delegateConfigurtationPorts.put(delegateID, delegateConfigurationPort);
			// Add a second to the heartbeat to allow for communication time
			// TODO: Must be a better way to do this...
			Beacon beacon = new Beacon(Long.parseLong(
					m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD, "1000")) + 1000);
			synchronized (m_heartbeats) {
				m_heartbeats.put(delegatePort.getDelegateId(), beacon);
			}
			m_nonCompliantDelegates.add(delegatePort.getDelegateId());
			beacon.mark();
			LOGGER.info(MessageFormat.format("Master created management connection with delegate {0}", delegateID));
			return delegatePort;
		} catch (NumberFormatException | RainbowConnectionException e) {
			LOGGER.error(MessageFormat.format(
					"Rainbow master could not create the management interface to the delegate {0}", delegateID));
			m_delegateConnection.disconnectDelegate(delegateID);
		}
		return null;
	}

	/**
	 * Called by a delegate port to request information be sent to it
	 * 
	 * @param delegateID
	 */
	public void requestDelegateConfiguration(String delegateID) {
		IDelegateConfigurationPort delegate = m_delegateConfigurtationPorts.get(delegateID);
		if (delegate != null) {
			LOGGER.info(MessageFormat.format("Sending configuration information to {0}.", delegateID));
			try {
				delegate.sendConfigurationInformation(filterPropertiesForDelegate(delegateID),
						filterProbesForDelegate(delegateID), filterEffectorsForDelegate(delegateID),
						filterGaugesForDelegate(delegateID));
			} catch (Throwable e) {
				e.printStackTrace();
				LOGGER.error("Failed to send config information", e);
			}
		} else {
			LOGGER.error(MessageFormat.format("Received configuration request from unknown delegate {0}.", delegateID));
		}
	}

	private List<GaugeInstanceDescription> filterGaugesForDelegate(String delegateID) {
		if (gaugeDesc().instSpec == null)
			return Collections.emptyList();

		Properties delegateInfo = m_delegateInfo.get(delegateID);
		String deploymentInfo;
		if (delegateInfo == null
				|| (deploymentInfo = delegateInfo.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
			LOGGER.error("There is no location information associated with " + delegateID);
			return Collections.emptyList();
		}
		return filterGaugesForLocation(deploymentInfo);
	}

	List<GaugeInstanceDescription> filterGaugesForLocation(String deploymentInfo) {
		List<GaugeInstanceDescription> gauges = new LinkedList<>();
		for (GaugeInstanceDescription gid : gaugeDesc().instSpec.values()) {
			TypedAttributeWithValue targetIP = gid.findSetupParam("targetIP");
			if (deploymentInfo.equals(targetIP.getValue())) {
				gauges.add(gid);
			}
		}
		return gauges;
	}

	private List<EffectorAttributes> filterEffectorsForDelegate(String delegateID) {
		if (effectorDesc().effectors == null)
			return Collections.emptyList();
		else {
			Properties delegateInfo = m_delegateInfo.get(delegateID);
			String deploymentInfo;

			if (delegateInfo == null || (deploymentInfo = delegateInfo
					.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
				LOGGER.error("There is no location information associated with " + delegateID);
				return Collections.emptyList();
			}
			return filterEffectorsForLocation(deploymentInfo);
		}
	}

	List<EffectorAttributes> filterEffectorsForLocation(String deploymentInfo) {
		List<EffectorAttributes> effectors = new LinkedList<>();
		for (EffectorAttributes probe : effectorDesc().effectors) {
			if (probe.getLocation().equals(deploymentInfo)) {
				effectors.add(probe);
			}
		}
		return effectors;
	}

	/**
	 * Called when a delegate sends a heartbeat message
	 * 
	 * @param delegateID The IP of the delegate
	 */
	public void processHeartbeat(String delegateID) {
		IDelegateManagementPort delegate = m_delegates.get(delegateID);
		if (delegate != null) {
			Beacon hb;
			synchronized (m_heartbeats) {
				hb = m_heartbeats.get(delegate.getDelegateId());
			}
			if (hb == null) {
				LOGGER.error(MessageFormat.format("Received heartbeat from unknown delegate at {0}.", delegateID));
			} else {
				Properties properties = m_delegateInfo.get(delegateID);
				String loc = "???";
				if (properties != null) {
					loc = properties.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION);
				}

				m_reportingPort.info(RainbowComponentT.MASTER,
						MessageFormat.format("Heartbeat from {0}@{1}", delegateID, loc));
				hb.mark();
				m_nonCompliantDelegates.remove(delegate.getDelegateId());
			}
		} else {
			LOGGER.error(MessageFormat.format("Received heartbeat from unknown delegate at {0}.", delegateID));
		}
	}

	/**
	 * Filters the properties to only report those properties that are relevant to
	 * the delegate
	 * 
	 * @param delegateID
	 * @return
	 */

	private Properties filterPropertiesForDelegate(String delegateID) {
		return m_rainbowEnvironment.allProperties();
	}

	private List<ProbeAttributes> filterProbesForDelegate(String delegateID) {
		if (probeDesc().probes == null)
			return Collections.emptyList();
		else {
			Properties delegateInfo = m_delegateInfo.get(delegateID);
			String deploymentInfo;
			if (delegateInfo == null || (deploymentInfo = delegateInfo
					.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION)) == null) {
				LOGGER.error("There is no location information associated with " + delegateID);
				return Collections.emptyList();
			}
			return filterProbesForLocation(deploymentInfo);
		}
	}

	List<ProbeAttributes> filterProbesForLocation(String deploymentInfo) {
		List<ProbeAttributes> probes = new LinkedList<>();
		for (ProbeAttributes probe : probeDesc().probes) {
			if (probe.getLocation().equals(deploymentInfo)) {
				probes.add(probe);
			}
		}
		return probes;
	}

	@Override
	public void stop() {
		for (IRainbowAnalysis a : m_analyses) {
			a.stop();
		}
		for (EffectorManager e : m_effectorManagers) {
			e.stop();
		}
		for (IAdaptationManager<?> a : m_adaptationManagers.values()) {
			a.stop();
		}
		for (IAdaptationExecutor<?> a : m_adaptationExecutors.values()) {
			a.stop();
		}
		super.stop();
	}

	@Override
	public void restart() {
		for (IRainbowAnalysis a : m_analyses) {
			a.restart();
		}
		for (EffectorManager e : m_effectorManagers) {
			e.restart();
		}
		for (IAdaptationManager<?> a : m_adaptationManagers.values()) {
			a.restart();
		}
		for (IAdaptationExecutor<?> a : m_adaptationExecutors.values()) {
			a.restart();
		}
		super.restart();
	}

	@Override
	public void dispose() {

	}

	@Override
	protected void log(String txt) {
		LOGGER.info(MessageFormat.format("RM: {0}", txt));
	}

	@Override
	protected void runAction() {
		checkTerminations();
		checkHeartbeats();
	}

	private void checkTerminations() {
		synchronized (m_terminatedDelegates) {
			if (!m_terminatedDelegates.isEmpty()) {
				for (Iterator<Entry<String, Beacon>> iterator = m_terminatedDelegates.entrySet().iterator(); iterator
						.hasNext();) {
					Entry<String, Beacon> e = iterator.next();
					if (e.getValue().periodElapsed()) {
						m_reportingPort.warn(getComponentType(),
								"Did not hear back from terminated delegate " + e.getKey() + ". Flushing anyway.");
						flushDelegate(e.getKey());
						iterator.remove();
					}
				}
			}
		}
	}

	private void checkHeartbeats() {
		try {
			synchronized (m_heartbeats) {
				Set<Entry<String, Beacon>> entrySet = m_heartbeats.entrySet();
				for (Iterator<Entry<String, Beacon>> iterator = entrySet.iterator(); iterator.hasNext();) {
					Entry<String, Beacon> entry = iterator.next();
					if (entry.getValue().periodElapsed()) {
						Properties properties = m_delegateInfo.get(entry.getKey());
						String loc = "???";
						if (properties != null) {
							loc = properties.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION);
						}
						if (!m_nonCompliantDelegates.contains(entry.getKey())) {
							m_nonCompliantDelegates.add(entry.getKey());
							m_reportingPort.error(RainbowComponentT.MASTER,
									MessageFormat.format("No Heartbeat from {0}@{1}", entry.getKey(), loc));

							LOGGER.error(MessageFormat.format(
									"Delegate {0} has not given a heartbeat withing the right time", entry.getKey()));
						}
						if (entry.getValue().isExpired()) {
							m_reportingPort.error(RainbowComponentT.MASTER, MessageFormat.format(
									"Delegate {0}@{1} has not sent a heartbeat in a while. " + "Forgetting about it",
									entry.getKey(), loc));
							deregisterDelegate(entry.getKey(), loc);
							iterator.remove();
						}
					}
				}
			}
		} catch (Throwable t) {
			m_reportingPort.error(RainbowComponentT.MASTER, "Failed in checking heartbeats", t);
		}
	}

	@Override
	public boolean allDelegatesOK() {
		return m_nonCompliantDelegates.isEmpty() && !m_heartbeats.isEmpty();
	}

	private void deregisterDelegate(String did, String loc) {
		flushDelegate(did);
	}

	public void disconnectDelegate(String id) {
		LOGGER.info(MessageFormat.format("RM: Disconnecting delegate: {0}", id));
		synchronized (m_terminatedDelegates) {
			m_terminatedDelegates.remove(id);
		}
		flushDelegate(id);
	}

	void flushDelegate(String id) {
		synchronized (m_heartbeats) {
			m_heartbeats.remove(id);
		}
//        IDelegateManagementPort deploymentPort = m_delegates.remove (id);
//        deploymentPort.dispose ();
		m_delegateInfo.remove(id);
//        IDelegateConfigurationPort port = m_delegateConfigurtationPorts.remove (id);
//        port.dispose ();
	}

	@Override
	public void terminate() {
		for (Entry<String, IDelegateManagementPort> entry : m_delegates.entrySet()) {
			disconnectDelegate(entry.getKey());
			entry.getValue().terminateDelegate();
			entry.getValue().dispose();
		}
		m_delegateConnection.dispose();

		m_reportingPort.dispose();
		// TODO: Terminate threads
//        try {
//            Thread.sleep (4000);
//        }
//        catch (InterruptedException e) {
//        }
		super.terminate();
		while (!isTerminated()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

// Methods below this point are used for testing purposes, and so are package protected.

	Map<? extends String, ? extends Beacon> getHeartbeatInfo() {
		return m_heartbeats;
	}

	@Override
	public ProbeDescription probeDesc() {
		synchronized (m_initialized) {
			if (m_probeDesc == null) {
				m_probeDesc = YamlUtil.loadProbeDesc();
			}
			return m_probeDesc;
		}
	}

	@Override
	public EffectorDescription effectorDesc() {
		synchronized (m_initialized) {
			if (m_effectorDesc == null) {
				m_effectorDesc = YamlUtil.loadEffectorDesc();
			}
			return m_effectorDesc;
		}
	}

	@Override
	public GaugeDescription gaugeDesc() {
		synchronized (m_initialized) {
			if (m_gaugeDesc == null) {
				m_gaugeDesc = YamlUtil.loadGaugeSpecs();
			}
			return m_gaugeDesc;
		}
	}

//    @Override
//    public UtilityPreferenceDescription preferenceDesc () {
//        if (m_prefDesc == null) {
//            m_prefDesc = YamlUtil.loadUtilityPrefs ();
//        }
//        return m_prefDesc;
//    }

	public void report(String delegateID, ReportType type, RainbowComponentT compT, String msg) {
		String log = MessageFormat.format("Delegate: {0}[{1}]: {2}", delegateID, compT.name(), msg);
		switch (type) {
		case INFO:
			LOGGER.info(log);
			break;
		case WARNING:
			LOGGER.warn(log);
			break;
		case ERROR:
			LOGGER.error(log);
			break;
		case FATAL:
			LOGGER.fatal(log);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public <S extends IEvaluable> IAdaptationManager<S> adaptationManagerForModel(String modelRef) {
		return (IAdaptationManager<S>) m_adaptationManagers.get(modelRef);
	}

//    public EffectorManager effectorManager () {
//        return m_effectorManager;
//    }

	@SuppressWarnings("unchecked")
	public <S> IAdaptationExecutor<S> strategyExecutor(String modelRef) {
		return (IAdaptationExecutor<S>) m_adaptationExecutors.get(modelRef);
	}

	public static void main(String[] args) throws RainbowException {

		boolean showHelp = false;
		boolean showGui = true;
		boolean autoStart = false;
		String checkFile = null;
		int lastIdx = args.length - 1;
		for (int i = 0; i <= lastIdx; i++) {
			switch (args[i]) {
			case "-h":
				showHelp = true;
				break;
			case "-nogui":
				showGui = false;
				break;
			case "-autostart":
				autoStart = true;
				break;
			case "-check-config":
				checkFile = args[++i];
				break;
			default:
				System.err.println("Unrecognized or incomplete argument " + args[i]);
				showHelp = true;
				break;
			}
		}
		if (showHelp) {
			System.out.println("Usage:\n" + "  system property options {default}:\n"
					+ "    rainbow.target    name of target configuration {default}\n"
					+ "    rainbow.config    top config directory (org.sa.rainbow.config)\n" + "  options: \n"
					+ "    -h          Show this help message\n" + "    -nogui      Don't show the Rainbow GUI\n" + "\n"
					+ "Option defaults are defined in <rainbow.target>/rainbow.properties");
			System.exit(RainbowConstants.EXIT_VALUE_ABORT);
		}
		
		RainbowRuntimeModule module = new RainbowRuntimeModule();
		Injector injector = Guice.createInjector(module);

		RainbowMaster master = new RainbowMaster();
		if (showGui) {
			String guiProp = m_rainbowEnvironment.getProperty(IRainbowEnvironment.PROPKEY_RAINBOW_GUI, null);
			IRainbowGUI gui = null;
			if (guiProp != null) {
				Class<IRainbowGUI> fc = null;
				try {
					fc = (Class<IRainbowGUI>) Class.forName(guiProp);
					Constructor<IRainbowGUI> csnt = fc.getConstructor(RainbowMaster.class);
					gui = csnt.newInstance(master);
				} catch (InvocationTargetException | ClassNotFoundException | SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException e) {
					System.err.println("Could not create class " + guiProp);
				} catch (NoSuchMethodException e) {
					try {
						gui = fc.newInstance();
					} catch (InstantiationException | IllegalAccessException e1) {
						System.err.println("Could not create class " + guiProp);

					}
				}

			}
			if (gui == null) {
				gui = new RainbowGUI();

			}
			gui.setMaster(master);
			gui.display();
		} else {
			m_rainbowEnvironment.setProperty(Rainbow.PROPKEY_SHOW_GUI, false);
		}
		master.m_autoStart = autoStart;		
		
		
		master.initialize();
		
		// This is a bit of a hack because the typechecker seems to be destructive
		// and the technical debt from 2006 of the excessive use of singletons is
		// hampering evolution.
		List<Problem> allProblems = Collections.<Problem>emptyList();
		if (checkFile != null) {
			try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(new File(checkFile)))) {
				allProblems = (List<Problem>) is.readObject();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Problem p : allProblems) {
			if (p.problem == ProblemT.ERROR) {
				master.m_reportingPort.error(master.getComponentType(), p.msg);
			} else if (p.problem == ProblemT.WARNING){
				master.m_reportingPort.warn(master.getComponentType(), p.msg);
			}
			else {
				master.m_reportingPort.info(master.getComponentType(), p.msg);
			}
		}
		RainbowDelegate localDelegate = new RainbowDelegate();
		localDelegate.initialize();
		


		master.start();
		localDelegate.start();

		if (!showGui) {
			RainbowPortFactory.createMasterCommandPort();
		}

	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.MASTER;
	}

	@Override
	public void startProbes() {
		log("Starting probes");
		for (IDelegateManagementPort delegate : m_delegates.values()) {
			delegate.startProbes();
		}
	}

	@Override
	public void killProbes() {
		for (IDelegateManagementPort delegate : m_delegates.values()) {
			delegate.killProbes();
		}
	}

	@Override
	public boolean autoStartProbes() {
		return m_autoStart;
	}

	@Override
	public void enableAdaptation(boolean enabled) {
		for (IAdaptationManager<?> am : m_adaptationManagers.values()) {
			am.setEnabled(enabled);
		}
	}

	public boolean isAdaptationEnabled() {
		boolean enabled = true;
		for (IAdaptationManager<?> am : m_adaptationManagers.values()) {
			enabled &= am.isEnabled();
		}
		return enabled;
	}

	@Override
	public Outcome testEffector(String target, String effName, List<String> args) {
		for (EffectorManager em : m_effectorManagers) {
			Outcome outcome = em.executeEffector(effName, target, args.toArray(new String[0]));
			if (outcome != Outcome.UNKNOWN)
				return outcome;
		}
		return Outcome.UNKNOWN;
	}

	@Override
	public void sleep() {
		m_rainbowEnvironment.signalTerminate();
	}

	@Override
	public void terminate(ExitState exitState) {
		m_rainbowEnvironment.signalTerminate(exitState);
	}

	@Override
	public void restartDelegates() {
		for (IDelegateManagementPort delegate : m_delegates.values()) {
			delegate.startDelegate();
		}
	}

	@Override
	public void sleepDelegates() {
		for (IDelegateManagementPort delegate : m_delegates.values()) {
			delegate.pauseDelegate();
		}
	}

	@Override
	public void destroyDelegates() {
		Set<String> delegatesBeforeClosing = new HashSet<>(m_delegates.keySet());
		for (String key : delegatesBeforeClosing) {
			Beacon b = new Beacon(10000);
			b.mark();
			synchronized (m_terminatedDelegates) {
				m_terminatedDelegates.put(key, b);
				m_delegates.get(key).terminateDelegate();
			}
		}
	}

	@Override
	public void killDelegate(String ipOfDelegate) {
		IDelegateManagementPort port = m_delegates.get(ipOfDelegate);
		String did = ipOfDelegate;
		if (port == null) {
			for (Entry<String, Properties> e : m_delegateInfo.entrySet()) {
				if (ipOfDelegate.equals(e.getValue().getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION))) {
					port = m_delegates.get(e.getKey());
					did = e.getKey();
					break;
				}
			}
			if (port != null) {
				Beacon b = new Beacon(10000);
				b.mark();
				synchronized (m_terminatedDelegates) {
					m_terminatedDelegates.put(did, b);
				}
				port.terminateDelegate();
			}
		}
	}

	@Override
	public List<String> getExpectedDelegateLocations() {
		int tries = 0;
		return getExpectedDelegateLocations(tries);
	}

	private List<String> getExpectedDelegateLocations(int tries) {
		try {
			List<String> ret = new LinkedList<>();
			Properties allProperties = m_rainbowEnvironment.allProperties();
			for (Map.Entry<?, ?> o : allProperties.entrySet()) {
				String k = (String) o.getKey();
				if (k.startsWith("customize.system.")) {
					String location = (String) o.getValue();
					if (!filterEffectorsForLocation(location).isEmpty() || !filterGaugesForLocation(location).isEmpty()
							|| !filterProbesForLocation(location).isEmpty()) {
						ret.add(location);
					}
				}
			}
			Collections.sort(ret);
			return ret;
		} catch (ConcurrentModificationException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			if (tries < 3) return getExpectedDelegateLocations(tries++);
		}
		return Collections.<String>emptyList();
	}

	@Override
	public Collection<IRainbowAnalysis> analyzers() {
		return Collections.unmodifiableCollection(m_analyses);
	}
	
	@Override
	public Map<String,IAdaptationManager<?>> adaptationManagers() {
		return Collections.unmodifiableMap(m_adaptationManagers);
	}
	
	@Override
	public Map<String, IAdaptationExecutor<?>> adaptationExecutors() {
		return Collections.unmodifiableMap(m_adaptationExecutors);
	}
	
	@Override
	public IMasterCommandPort getCommandPort() {
		return this;
	}

}
