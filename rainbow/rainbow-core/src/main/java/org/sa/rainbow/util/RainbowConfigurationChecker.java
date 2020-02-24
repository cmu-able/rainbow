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
package org.sa.rainbow.util;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeTypeDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe.Kind;

import com.google.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

public class RainbowConfigurationChecker implements IRainbowReportingPort, IRainbowConfigurationChecker {

	public static enum ProblemT {
		WARNING, ERROR, INFO
	}

	public static class Problem implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4323498028203837123L;

		public Problem(ProblemT p, String msg) {
			problem = p;
			this.msg = msg;
		}

		public Problem() {
		}

		public ProblemT problem;
		public String msg;

		public void setMessage(String string) {
			msg = string;
		}

		public ProblemT getType() {
			return problem;
		}
	}

	final List<Problem> m_problems = new LinkedList<>();
	private IRainbowMaster m_master;
	final Set<String> m_referredToProbes = new HashSet<>();
	
	protected IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();

	public RainbowConfigurationChecker() {

	}

	@Override
	public void setRainbowMaster(IRainbowMaster master) {
		m_master = master;
	}

	@Override
	public void checkRainbowConfiguration() {
		checkModelConfigurations();
		checkAnalyzerConfigurations();
		checkManagerConfigurations();
		checkExecutorConfigurations();
		checkGaugeConfiguration();
		checkProbeConfiguration();
		checkEffectorConfiguration();
	}

	private void checkExecutorConfigurations() {
		String message = "Checking executors...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		String noAs = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_SIZE, "0");
		int numberOfAnalyzers = Integer.parseInt(noAs);
		if (numberOfAnalyzers == 0) {
			m_problems.add(new Problem(ProblemT.WARNING, "There seem to be no executors specified."));
		} else {
			for (int anum = 0; anum < numberOfAnalyzers; anum++) {
				String analyzerClass = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_ADAPTATION_MANAGER_CLASS + "_" + anum);
				if (analyzerClass == null) {
					m_problems.add(
							new Problem(ProblemT.ERROR, MessageFormat.format("Executor {0} is not specified.", anum)));
				} else {
					try {
						Class<?> analyzer = Class.forName(analyzerClass);
					} catch (ClassNotFoundException e) {
						m_problems.add(new Problem(ProblemT.ERROR,
								MessageFormat.format("Executor {0}: Could not load class {1}.", anum, analyzerClass)));
					}
					String modelRef = m_rainbowEnvironment
							.getProperty(RainbowConstants.PROPKEY_ADAPTATION_EXECUTOR_MODEL + "_" + anum);
					IModelInstance<Object> modelInstance = m_master.modelsManager()
							.getModelInstance(ModelReference.fromString(modelRef));
					if (modelInstance == null) {
						p = new Problem();
						p.problem = ProblemT.ERROR;
						p.msg = MessageFormat.format("Executor {0}: The model ''{1}'' is unknown.", anum, modelRef);
						m_problems.add(p);
					}
				}

			}
		}

		if (m_problems.size() == num)
			p.setMessage(message + "ok");
	}

	private void checkManagerConfigurations() {
		String message = "Checking managers...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		String noAs = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ADAPTATION_MANAGER_SIZE, "0");
		int numberOfManagers = Integer.parseInt(noAs);
		if (numberOfManagers == 0) {
			m_problems.add(new Problem(ProblemT.WARNING, "There seem to be no analyzers specified."));
		} else {
			for (int anum = 0; anum < numberOfManagers; anum++) {
				String managerClass = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_ADAPTATION_MANAGER_CLASS + "_" + anum);
				if (managerClass == null) {
					m_problems.add(
							new Problem(ProblemT.ERROR, MessageFormat.format("Manager {0} is not specified.", anum)));
				} else {
					try {
						Class<?> analyzer = Class.forName(managerClass);
					} catch (ClassNotFoundException e) {
						m_problems.add(new Problem(ProblemT.ERROR,
								MessageFormat.format("Manager {0}: Could not load class {1}.", anum, managerClass)));
					}
					String modelRef = m_rainbowEnvironment
							.getProperty(RainbowConstants.PROPKEY_ADAPTATION_MANAGER_MODEL + "_" + anum);
					IModelInstance<Object> modelInstance = m_master.modelsManager()
							.getModelInstance(ModelReference.fromString(modelRef));
					if (modelInstance == null) {
						p = new Problem();
						p.problem = ProblemT.ERROR;
						p.msg = MessageFormat.format("Manager {0}: The model ''{1}'' is unknown.", anum, modelRef);
						m_problems.add(p);
					}
				}
			}
		}

		if (m_problems.size() == num)
			p.setMessage(message + "ok");
	}

	private void checkAnalyzerConfigurations() {
		String message = "Checking analyzers...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		String noAs = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_ANALYSIS_COMPONENT_SIZE, "0");
		int numberOfAnalyzers = Integer.parseInt(noAs);
		if (numberOfAnalyzers == 0) {
			m_problems.add(new Problem(ProblemT.WARNING, "There seem to be no analyzers specified."));
		} else {
			for (int anum = 0; anum < numberOfAnalyzers; anum++) {
				String analyzerClass = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_ANALYSIS_COMPONENTS + "_" + anum);
				if (analyzerClass == null) {
					m_problems.add(
							new Problem(ProblemT.ERROR, MessageFormat.format("Analyzer {0} is not specified.", anum)));
				} else {
					try {
						Class<?> analyzer = Class.forName(analyzerClass);
					} catch (ClassNotFoundException e) {
						m_problems.add(new Problem(ProblemT.ERROR,
								MessageFormat.format("Analyzer {0}: Could not load class {1}.", anum, analyzerClass)));
					}
				}
			}
		}

		if (m_problems.size() == num)
			p.setMessage(message + "ok");

	}

	private void checkModelConfigurations() {
		String message = "Checking models...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		String numberOfModelsStr = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_MODEL_NUMBER, "0");
		int numberOfModels = Integer.parseInt(numberOfModelsStr);
		if (numberOfModels == 0) {
			m_problems.add(new Problem(ProblemT.ERROR, "There are no models specified"));
		} else {
			for (int modelNum = 0; modelNum < numberOfModels; modelNum++) {
				String factoryClassName = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum);
				if (factoryClassName == null || "".equals(factoryClassName)) {
					m_problems.add(new Problem(ProblemT.WARNING,
							MessageFormat.format(
									"Model number {0} is not specified. Looking for property. Looking for ''{1}''.",
									modelNum, (RainbowConstants.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum))));
				}
				String modelName = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_MODEL_NAME_PREFIX + modelNum);
				if (modelName == null) {
					m_problems.add(new Problem(ProblemT.ERROR,
							MessageFormat.format("Model {0} does not have a name. ''{1}'' is unspecified.", modelNum,
									(RainbowConstants.PROPKEY_MODEL_NAME_PREFIX + modelNum))));
				}
				String path = m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_MODEL_PATH_PREFIX + modelNum);
				String saveOnClose = m_rainbowEnvironment
						.getProperty(RainbowConstants.PROPKEY_MODEL_SAVE_PREFIX + modelNum);
				// It is possible for a model not to be sourced from a file, in which case
				// the load command may just create and register the model in the manager
				File modelPath = null;
				if (path != null) {
					modelPath = new File(path);
					if (!modelPath.isAbsolute()) {
						modelPath = Util.getRelativeToPath(m_rainbowEnvironment.getTargetPath(), path);
					}

				}
				if (modelPath != null && !modelPath.exists()) {
					m_problems.add(new Problem(ProblemT.ERROR,
							"The specified file for the model does not exist: " + modelPath.getAbsolutePath()));
				}
				if (factoryClassName != null)
					try {
						Class loadClass = Class.forName(factoryClassName);
						Method method = loadClass.getMethod("loadCommand", ModelsManager.class, String.class,
								InputStream.class, String.class);
						if (!Modifier.isStatic(method.getModifiers()) || method.getDeclaringClass() != loadClass)
							m_problems
									.add(new Problem(ProblemT.ERROR,
											MessageFormat.format("The class {0} does not implement a "
													+ "static method loadCommand, used" + " to generate modelInstances",
													method.getDeclaringClass().getCanonicalName())));
					} catch (ClassNotFoundException | SecurityException e) {
						m_problems.add(new Problem(ProblemT.ERROR, MessageFormat.format(
								"Model {0}: Could not find factory class ''{1}''", modelNum, factoryClassName)));
					} catch (NoSuchMethodException e) {
						m_problems.add(new Problem(ProblemT.ERROR, MessageFormat
								.format("Could not access static method loadCommand in ''{0}''.", factoryClassName)));
					}
			}
		}
		if (m_problems.size() == num)
			p.setMessage(message + "ok");
	}

	private void checkEffectorConfiguration() {
		String message = "Checking effectors...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		EffectorDescription effectorDesc = m_master.effectorDesc();
		for (EffectorAttributes effector : effectorDesc.effectors) {
			checkEffector(effector);
		}
		if (m_problems.size() == num)
			p.setMessage(message + "ok");
	}

	private void checkEffector(EffectorAttributes effector) {

		if (effector.effectorType != null) {
			if (m_master.effectorDesc().effectorTypes.get(effector.effectorType.name) == null) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("{0}: Refers to an effector type that does not exist: {1}", effector.name,
								effector.effectorType.name)));
			}
		}

		if (effector.getLocation() == null || "".equals(effector.getLocation())) {
			m_problems.add(
					new Problem(ProblemT.ERROR, MessageFormat.format("{0}: Does not have a location", effector.name)));
		}

		if (effector.getCommandPattern() == null) {
			m_problems.add(new Problem(ProblemT.ERROR,
					MessageFormat.format("{0}: does not have a command and so cannot be called.", effector.name)));
		}

		if (effector.getKind() == IEffectorIdentifier.Kind.JAVA) {
			String effClass = effector.getInfo().get("class");
			if (effClass == null) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("{0}: Is a JAVA effector without a 'class' attribute", effector.name)));
			} else {
				try {
					Class.forName(effClass);
				} catch (ClassNotFoundException e) {
					m_problems.add(new Problem(ProblemT.WARNING, MessageFormat
							.format("{0}: Cannot find the class ''{1}'' for the effector", effector.name, effClass)));
				}
			}
		} else if (effector.getKind() == IEffectorIdentifier.Kind.SCRIPT) {
			String path = effector.getInfo().get("path");
			if (path == null) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("{0}: Is a SCRIPT effector without a 'path' attribute", effector.name)));
			}

		}
	}

	private void checkProbeConfiguration() {
		String message = "Checking probes...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		ProbeDescription probeDesc = m_master.probeDesc();
		for (ProbeDescription.ProbeAttributes probe : probeDesc.probes) {
			checkProbe(probe);
		}
		if (m_problems.size() == num)
			p.setMessage(message + "ok");

	}

	private void checkProbe(ProbeAttributes probe) {
		if (probe.alias == null || "".equals(probe.alias)) {
			m_problems
					.add(new Problem(ProblemT.ERROR, MessageFormat.format("{0}: Does not have an alias", probe.name)));
		} else {
			if (!m_referredToProbes.contains(probe.alias)) {
				m_problems.add(new Problem(ProblemT.WARNING, MessageFormat
						.format("{0}: The alias ''{1}'' is not referred to by any gauges.", probe.name, probe.alias)));
			}
		}

		if (probe.getLocation() == null || "".equals(probe.getLocation())) {
			m_problems.add(
					new Problem(ProblemT.ERROR, MessageFormat.format("{0}: Does not have a location", probe.name)));
		}

		if (probe.getLocation() != null && probe.getLocation().startsWith("$")) {
			m_problems.add(new Problem(ProblemT.ERROR,
					MessageFormat.format("{0}: Has an unexpanded location ''{1}''", probe.name, probe.getLocation())));
		}

		if (probe.kind == Kind.JAVA) {
			String probeClazz = probe.getInfo().get("class");
			if (probeClazz == null) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("{0}: Is a JAVA probe without a 'class' attribute", probe.name)));
			} else {
				try {
					this.getClass().forName(probeClazz);
				} catch (ClassNotFoundException e) {
					m_problems.add(new Problem(ProblemT.WARNING, MessageFormat
							.format("{0}: Cannot find the class ''{1}'' for the probe", probe.name, probeClazz)));
				}
			}
		} else if (probe.kind == Kind.SCRIPT) {
			String path = probe.getInfo().get("path");
			if (path == null) {
				m_problems.add(new Problem(ProblemT.ERROR,
						MessageFormat.format("{0}: Is a SCRIPT probe without a 'path' attribute", probe.name)));
			}

		}

	}

	protected void checkGaugeConfiguration() {
		String message = "Checking gauges...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		GaugeDescription gaugeDesc = m_master.gaugeDesc();
		Collection<GaugeInstanceDescription> instSpecs = gaugeDesc.instSpec.values();

		for (GaugeTypeDescription gtd : m_master.gaugeDesc().typeSpec.values()) {
			checkGaugeType(gtd);
		}

		for (GaugeInstanceDescription gid : instSpecs) {
			checkGaugeConsistent(gid);
		}
		if (m_problems.size() == num)
			p.setMessage(message + "ok");

	}

	private void checkGaugeType(GaugeTypeDescription gtd) {
		checkSetupParam(gtd, "targetIP");
		checkSetupParam(gtd, "beaconPeriod");
	}

	void checkSetupParam(GaugeTypeDescription gtd, String param) {
		if (gtd.findSetupParam(param) == null) {
			Problem p = new Problem();
			p.problem = ProblemT.ERROR;
			p.msg = MessageFormat.format("{0}: does not specify a setup param ''{1}''.", gtd.gaugeType(), param);
			m_problems.add(p);
		}
	}

	private void checkGaugeConsistent(GaugeInstanceDescription gid) {
		// Errors
		// Check if gauge type exsits in gauge spec
		GaugeTypeDescription type = m_master.gaugeDesc().typeSpec.get(gid.gaugeType());
		if (type == null) {
			Problem p = new Problem();
			p.problem = ProblemT.ERROR;
			p.msg = MessageFormat.format("{0}: The gauge type ''{1}'' is unknown", gid.gaugeName(), gid.gaugeType());
			m_problems.add(p);
		}
		// Check if model exists in Rainbow
		if (gid.modelDesc() == null || gid.modelDesc().getName() == null || gid.modelDesc().getType() == null) {
			Problem p = new Problem();
			p.problem = ProblemT.ERROR;
			if (gid.modelDesc() == null) {
				p.msg = MessageFormat.format("{0}: There is no model that the gauge is associated with",
						gid.gaugeName());
			} else {
				p.msg = MessageFormat.format(
						"{0}: Neither the model name nor model type can be null: name=''{1}'', type=''{2}''",
						gid.gaugeName(), gid.modelDesc().getName(), gid.modelDesc().getType());
			}
			m_problems.add(p);
		} else {
			IModelInstance<Object> modelInstance = m_master.modelsManager()
					.getModelInstance(new ModelReference(gid.modelDesc().getName(), gid.modelDesc().getType()));
			if (modelInstance == null) {
				Problem p = new Problem();
				p.problem = ProblemT.ERROR;
				p.msg = MessageFormat.format("{0}: The model ''{1}:{2}'' is unknown.", gid.gaugeName(),
						gid.modelDesc().getName(), gid.modelDesc().getType());
				m_problems.add(p);
				return;
			}
			// Check if command exists in model

			List<Pair<String, OperationRepresentation>> commandSignatures = gid.commandSignatures();
			Set<String> commandsFromType = new HashSet<>();
			try {
				ModelCommandFactory<Object> cf;
				cf = modelInstance.getCommandFactory();
				for (Pair<String, OperationRepresentation> pair : commandSignatures) {
					String commandName = pair.secondValue().getName();
					try {
						commandsFromType.add(commandName);
						if (!findCommand(cf, commandName)) {
							Problem p = new Problem();
							p.problem = ProblemT.ERROR;
							p.msg = MessageFormat.format(
									"{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
									gid.gaugeName(), gid.modelDesc().getName(), gid.modelDesc().getType(), commandName);
							m_problems.add(p);
						}
					} catch (RainbowModelException e) {
						if (e.getCause() instanceof NoSuchMethodException
								|| e.getCause() instanceof SecurityException) {
							Problem p = new Problem();
							p.problem = ProblemT.ERROR;
							p.msg = MessageFormat.format(
									"{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
									gid.gaugeName(), gid.modelDesc().getName(), gid.modelDesc().getType(), commandName);
							m_problems.add(p);
						}
					}
				}
				Collection<OperationRepresentation> mappings = gid.mappings().values();
				for (OperationRepresentation command : mappings) {
					boolean remove = commandsFromType.remove(command.getName());
					if (!remove) {
						Problem p = new Problem();
						p.problem = ProblemT.WARNING;
						p.msg = MessageFormat.format(
								"{0}: Specifiies the command ''{1}'' that is not referenced in the type ''{2}",
								gid.gaugeName(), command.getName(), gid.gaugeType());
						m_problems.add(p);
					}
					try {
						if (!findCommand(cf, command.getName())) {
							Problem p = new Problem();
							p.problem = ProblemT.ERROR;
							p.msg = MessageFormat.format(
									"{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
									gid.gaugeName(), gid.modelDesc().getName(), gid.modelDesc().getType(),
									command.getName());
							m_problems.add(p);
						}

					} catch (RainbowModelException e) {
						if (e.getCause() instanceof NoSuchMethodException
								|| e.getCause() instanceof SecurityException) {
							Problem p = new Problem();
							p.problem = ProblemT.ERROR;
							p.msg = MessageFormat.format(
									"{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
									gid.gaugeName(), gid.modelDesc().getName(), gid.modelDesc().getType(),
									command.getName());
							m_problems.add(p);
						}
					}
				}
			} catch (RainbowException e1) {
				m_problems.add(new Problem(ProblemT.ERROR, "Cannot create command factory for "
						+ modelInstance.getModelName() + ":" + modelInstance.getModelType()));
			}
			if (!commandsFromType.isEmpty()) {
				Problem p = new Problem();
				p.problem = ProblemT.WARNING;
				StringBuilder cmd = new StringBuilder();
				for (String c : commandsFromType) {
					cmd.append(c);
					cmd.append(", ");
				}
				cmd.delete(cmd.length() - 1, cmd.length());
				p.msg = MessageFormat.format("{0}: Does not refer to the following commands defined in the type: {1}",
						gid.gaugeName(), cmd.toString());
				m_problems.add(p);
			}

		}

		// Check if probe exists in probe desc
		TypedAttributeWithValue probe;
		probe = gid.findConfigParam("targetProbeType");
//        if (probe == null) probe = gid.findConfigParam("targetProbeList");
		if (probe != null) {
			String[] probes = null;
			if (probe.getValue() instanceof String) {
				probes = ((String) probe.getValue()).split(",");
			} else if (probe.getValue() instanceof String[]) {
				probes = (String[]) probe.getValue();
			} else if (probe.getValue() instanceof ArrayList) {
				ArrayList value = (ArrayList) probe.getValue();
				probes = (String[]) value.toArray(new String[0]);
				String csv = String.join(",", value);
				probe.setValue(csv);

			}
//            String[] probes = ((String )probe.getValue ()).split (",");
			for (String probe2 : probes) {
				probe2 = Util.decomposeID(probe2).firstValue();
				m_referredToProbes.add(probe2);
				SortedSet<ProbeAttributes> probeDescs = m_master.probeDesc().probes;
				boolean found = false;
				for (Iterator it = probeDescs.iterator(); it.hasNext() && !found;) {
					ProbeAttributes pa = (ProbeAttributes) it.next();
					found = probe2.equals(pa.alias);
				}
				if (!found) {
					Problem p = new Problem();
					p.problem = ProblemT.ERROR;
					p.msg = MessageFormat.format("{0}: Refers to a probe ''{1}'' that is not found.", gid.gaugeName(),
							probe2);
					m_problems.add(p);
				}
			}
		}

		// Warnings
		// Check if class can be found and has a constructor
		TypedAttributeWithValue cls = gid.findSetupParam("javaClass");
		if (cls == null) {
			Problem p = new Problem();
			p.problem = ProblemT.WARNING;
			p.msg = MessageFormat.format("{0}: does not have a ''javaClass'' setup parameter.", gid.gaugeName());
			m_problems.add(p);

		} else {
			String className = (String) cls.getValue();
			try {
				Class clazz = getClass().forName(className);
				Class<?>[] paramTypes = new Class[6];
				paramTypes[0] = String.class;
				paramTypes[1] = long.class;
				paramTypes[2] = TypedAttribute.class;
				paramTypes[3] = TypedAttribute.class;
				paramTypes[4] = List.class;
				paramTypes[5] = Map.class;
				Constructor constructor = clazz.getConstructor(paramTypes);

			} catch (ClassNotFoundException e) {
				Problem p = new Problem();
				p.problem = ProblemT.WARNING;
				p.msg = MessageFormat.format("{0}: refers to a class ''{1}'' that cannot be found on the class path.",
						gid.gaugeName(), className);
				m_problems.add(p);
			} catch (NoSuchMethodException | SecurityException e) {
				Problem p = new Problem();
				p.problem = ProblemT.ERROR;
				p.msg = MessageFormat.format("{0}: The class ''{1}'' does not seem to have a valid constructor.",
						gid.gaugeName(), className);
				m_problems.add(p);
			}
		}

		// Check if all setupParams are found in type, and all configParams are found in
		// type
		for (TypedAttributeWithValue s : gid.setupParams()) {
			if (m_master.gaugeDesc().typeSpec.get(gid.gaugeType()).findSetupParam(s.getName()) == null) {
				Problem p = new Problem();
				p.problem = ProblemT.WARNING;
				p.msg = MessageFormat.format("{0}: has a setup parameter ''{1}'' that is not declared in the type",
						gid.gaugeName(), s.getName());
				m_problems.add(p);
			}
		}
		for (TypedAttributeWithValue s : gid.configParams()) {
			if (m_master.gaugeDesc().typeSpec.get(gid.gaugeType()).findConfigParam(s.getName()) == null) {
				Problem p = new Problem();
				p.problem = ProblemT.WARNING;
				p.msg = MessageFormat.format("{0}: has a config parameter ''{1}'' that is not declared in the type",
						gid.gaugeName(), s.getName());
				m_problems.add(p);
			}
		}

	}

	protected boolean findCommand(ModelCommandFactory<?> cf, String commandName) throws RainbowModelException {
		Method[] methods = cf.getClass().getMethods();
		Method method = null;
		boolean found = false;
		for (int i = 0; i < methods.length && !found; i++) {
			if (methods[i].getName().toLowerCase().startsWith(commandName.toLowerCase())) {
				method = methods[i];
				found = true;
			}
		}
		return found;
	}

	@Override
	public Collection<Problem> getProblems() {
		return m_problems;
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		fatal(type, MessageFormat.format("{0}.\nException: {1}\n{2}", msg, e.getMessage(), baos.toString()));

	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Logger logger) {
		fatal(type, msg);
	}

	@Override
	public void fatal(RainbowComponentT type, String msg, Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		fatal(type, MessageFormat.format("{0}.\nException: {1}\n{2}", msg, e.getMessage(), baos.toString()));
	}

	@Override
	public void fatal(RainbowComponentT type, String msg) {
		Problem p = new Problem(ProblemT.ERROR, msg);
		m_problems.add(p);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		error(type, msg, e);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Logger logger) {
		error(type, msg);
	}

	@Override
	public void error(RainbowComponentT type, String msg, Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		error(type, MessageFormat.format("{0}.\nException: {1}\n{2}", msg, e.getMessage(), baos.toString()));
	}

	@Override
	public void error(RainbowComponentT type, String msg) {
		Problem p = new Problem(ProblemT.ERROR, msg);
		m_problems.add(p);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e, Logger logger) {
		warn(type, msg, e);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Logger logger) {
		warn(type, msg);
	}

	@Override
	public void warn(RainbowComponentT type, String msg, Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		warn(type, MessageFormat.format("{0}.\nException: {1}\n{2}", msg, e.getMessage(), baos.toString()));

	}

	@Override
	public void warn(RainbowComponentT type, String msg) {
		Problem p = new Problem(ProblemT.WARNING, msg);
		m_problems.add(p);
	}

	@Override
	public void info(RainbowComponentT type, String msg, Logger logger) {

	}

	@Override
	public void info(RainbowComponentT type, String msg) {

	}

	@Override
	public void trace(RainbowComponentT type, String msg) {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Class> getMustBeExecutedAfter() {
		return Collections.<Class>emptySet();
	}

}
