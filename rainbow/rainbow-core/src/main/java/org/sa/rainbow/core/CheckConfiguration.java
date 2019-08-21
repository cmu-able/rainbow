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

import java.io.PrintStream;
import java.security.AllPermission;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeManager;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.YamlUtil;

public class CheckConfiguration {
	
	public static interface IReporter {
		public void report (String s);
	}

	public static void main(String[] args) throws Throwable {
		checkConfiguration(System.out);
		Rainbow.instance().signalTerminate();
		System.exit(0);

	}

	public static List<Problem> checkConfiguration(PrintStream out) throws Throwable {
		out.println("Reading configuration files");
		Rainbow.instance();
		out.println(
				"Loading YAMLs for target: " + Rainbow.instance().getProperty(RainbowConstants.PROPKEY_TARGET_NAME));
		out.print("Loading probes...");
		out.flush();
		final ProbeDescription loadProbeDesc = YamlUtil.loadProbeDesc();
		out.println("found " + loadProbeDesc.probes.size() + " probes");
		out.print("Loading effecors...");
		out.flush();
		final EffectorDescription loadEffectorDesc = YamlUtil.loadEffectorDesc();
		out.println("found " + loadEffectorDesc.effectorTypes.size() + " effector types, "
				+ loadEffectorDesc.effectors.size() + " effectors");
		out.print("Loading gauges...");
		out.flush();
		final GaugeDescription loadGaugeSpecs = YamlUtil.loadGaugeSpecs();
		out.println(
				"found " + loadGaugeSpecs.typeSpec.size() + " types, " + loadGaugeSpecs.instSpec.size() + " instances");
//        out.print ("Loading preferences...");
//        out.flush ();
//        final UtilityPreferenceDescription loadUtilityPrefs = YamlUtil.loadUtilityPrefs ();
//        out.println ("found " + loadUtilityPrefs.attributeVectors.size () + " attribute vectors, "
//                + loadUtilityPrefs.utilities.size () + " utilities, " + loadUtilityPrefs.weights.size () + " weights");

		final ModelsManager mm = new ModelsManager();
		final GaugeManager gm = new GaugeManager(loadGaugeSpecs);

		IRainbowMaster master = new IRainbowMaster() {

			@Override
			public ProbeDescription probeDesc() {
				return loadProbeDesc;
			}

//            @Override
//            public UtilityPreferenceDescription preferenceDesc () {
//                return loadUtilityPrefs;
//            }

			@Override
			public GaugeDescription gaugeDesc() {
				return loadGaugeSpecs;
			}

			@Override
			public EffectorDescription effectorDesc() {
				return loadEffectorDesc;
			}

			@Override
			public ModelsManager modelsManager() {
				return mm;
			}

			@Override
			public GaugeManager gaugeManager() {
				return gm;
			}

			@Override
			public Map<String, IAdaptationExecutor<?>> adaptationExecutors() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Map<String, IAdaptationManager<?>> adaptationManagers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<IRainbowAnalysis> analyzers() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public IMasterCommandPort getCommandPort() {
				// TODO Auto-generated method stub
				return null;
			}

		};

		// Make Rainbow.instance().rainbowMaster() return this master
		Rainbow.instance().setMaster(master);
		out.println("Locating configuration checkers in system...");
		Reflections reflections = new Reflections("org.sa", CheckConfiguration.class.getClassLoader());
		List<IRainbowConfigurationChecker> checkers = new LinkedList<>();
		Set<Class<? extends IRainbowConfigurationChecker>> checkerClasses = reflections
				.getSubTypesOf(IRainbowConfigurationChecker.class);
		for (Class<? extends IRainbowConfigurationChecker> cls : checkerClasses) {
			try {

				checkers.add(cls.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				out.println("Could not instantiate " + cls);
			}
		}
		checkers.sort(new Comparator<IRainbowConfigurationChecker>() {

			@Override
			public int compare(IRainbowConfigurationChecker o1, IRainbowConfigurationChecker o2) {
				if (o1.getMustBeExecutedAfter().contains(o2.getClass()))
					return 1;
				else if (o2.getMustBeExecutedAfter().contains(o1.getClass()))
					return -1;
				return 0;
			}
		});
		out.println("Checking configuration consistency...");
		List<Problem> problems = new LinkedList<>();
		boolean hasProblems = false;
		for (IRainbowConfigurationChecker checker : checkers) {
			checker.setRainbowMaster(master);
			if (mm.m_reportingPort instanceof DisconnectedRainbowDelegateConnectionPort
					&& checker instanceof IRainbowReportingPort) {
				mm.m_reportingPort = (IRainbowReportingPort) checker;
				out.print("Loading models...");
				out.flush();
				mm.initializeModels();
				out.println("found " + mm.getRegisteredModelTypes() + " model *types*");
			}
			Throwable ex = null;
			try {
			checker.checkRainbowConfiguration();
			}
			catch (Throwable e) {
				ex = e;
			}
			problems.addAll(checker.getProblems());
			if (checker.getProblems().size() > 0) {
//					out.println("Problems with the configuration were reported:");
				for (Problem p : checker.getProblems()) {
					if (p.getType() == ProblemT.ERROR)
						hasProblems = true;
					out.println(p.problem.name() + ": " + p.msg);
				}
			}
			if (ex!= null) throw ex;

		}

		if (!hasProblems) {
			out.println("No problems were found with the configuration");
		}
		
		
	}

}
