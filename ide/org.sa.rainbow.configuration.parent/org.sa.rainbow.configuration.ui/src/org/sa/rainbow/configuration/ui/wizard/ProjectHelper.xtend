package org.sa.rainbow.configuration.ui.wizard

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.Map
import java.util.regex.Pattern

class ProjectHelper {
	
		
	
	static def generateReplacements(String projectName, String deploymentType, String customDeployment, String mfName, String sgp, String rbg, String rbs, boolean acme) {
		val replacements = newLinkedHashMap(
			'rainbow-target' -> projectName,
			'rainbowVersion' -> '3.0',
			'gauges.rbw' -> "model/gauges.rbw",
			'deploymentFactory' -> (deploymentType=="Single Machine"?"org.sa.rainbow.core.ports.guava.GuavaRainbowPortFactory":(deploymentType=="Multiple Machines"?"org.sa.rainbow.core.ports.eseb.ESEBRainbowPortFactory":customDeployment)),
			'AnalysisName' -> "anAnalysis",
			'AnalysisClass' -> 'org.sa.rainbow.core.analysis.IRainbowAnalysis # Change this to a real class',
			'AdaptationManagerName' -> 'anAdaptationManager',
			'AdaptationClass' -> 'org.sa.rainbow.core.adaptation.org.sa.rainbow.core.adaptation # Change this to a real class',
			'ExecutorName' -> 'anExecutor',
			'ExectorClass' -> 'org.sa.rainbow.core.adaptation.IAdaptationExecutor # Change this to a real class',
			'EffectorManagerName' -> 'anEffectorManager',
			'EffectorManagerClass' -> 'org.sa.rainbow.translator.effectors.EffectorManager # Change this to a real class',
			'GUISpecs' -> '',
			'factoryName' -> mfName,
			'srcGenPackage' -> sgp,
			'rbwGen.value' -> rbg,
			'rbwSrc.value' -> rbs
		)
		if (acme) {
			val AandSDefaults = newLinkedHashMap(
				'AnalysisName' -> "ArchEvaluator",
				'AnalysisClass' -> 'org.sa.rainbow.evaluator.^acme.ArchEvaluator',
				'AdaptationManagerName' -> 'AdaptationManager',
				'AdaptationManagerClass' -> 'org.sa.rainbow.^stitch.adaptation.AdaptationManager',
				'ExecutorName' -> 'StitchExecutor',
				'ExectorClass' -> 'org.sa.rainbow.^stitch.adaptation.StitchExecutor',
				'EffectorManagerName' -> 'AcmeEffectorManager',
				'EffectorManagerClass' -> 'org.sa.rainbow.effectors.^acme.AcmeEffectorManager',
				'GUISpecs' -> "
		analyzers = {
			analyzer = {
				^for =  ««ArchEvaluator»»
				class = org.sa.rainbow.evaluator.^acme.^gui.ArchAnalyzerGUI
			}
		}
		managers = {
			manager = {
				^for = ««AdaptationManager»»
				class = org.sa.rainbow.^stitch.^gui.manager.ArchStitchAdapationManager 
			}
		}
		executors = {
			^executor = {
				^for = ««StitchExecutor»»
				class = org.sa.rainbow.^stitch.^gui.^executor.EventBasedStitchExecutorPanel 
			} 
		}
		details = {
			managers = org.sa.rainbow.^stitch.^gui.manager.StitchAdaptationManagerTabbedPane
			executors = org.sa.rainbow.^stitch.^gui.^executor.StitchExecutorTabbedPane
		}"
			)
			replacements.putAll(AandSDefaults)

		}
		replacements
	} 
	
	static def generatePattern(Map<String,String> replacements) {
		val patternString = "<<(" + replacements.keySet.join("|") + ")>>"
 		var pattern = Pattern.compile(patternString);	
 		pattern
	}
	
	static def readFile(String fileName, Map<String, String> replacements, Pattern pattern) {
		var sb = new StringBuffer
 		try (val in = new BufferedReader(new InputStreamReader(new URL("platform:/plugin/org.sa.rainbow.configuration.ui/" + fileName).openConnection.inputStream))) {
 			var inputLine = ""
 			while ((inputLine = in.readLine) !== null) {
 				sb.append(inputLine)
 				sb.append("\n")	
 			}
 		}
 		catch (IOException e) {}
 		var ret = sb.toString
 		
 		val matcher = pattern.matcher(ret)
 		sb = new StringBuffer(ret.length << 1)
 		while (matcher.find()) {
 			matcher.appendReplacement(sb, replacements.get(matcher.group(1)));
 		}
 		matcher.appendTail(sb)
 		
 		
 		sb.toString
 		
		
	}
	
	
}