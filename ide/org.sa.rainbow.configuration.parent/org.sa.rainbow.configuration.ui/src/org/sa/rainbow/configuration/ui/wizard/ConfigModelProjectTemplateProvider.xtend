/*
 * generated by Xtext 2.19.0
 */
package org.sa.rainbow.configuration.ui.wizard

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.HashMap
import java.util.regex.Pattern
import org.eclipse.core.runtime.Status
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.util.JavaProjectFactory
import org.eclipse.xtext.ui.util.ProjectFactory
import org.eclipse.xtext.ui.wizard.template.IProjectGenerator
import org.eclipse.xtext.ui.wizard.template.IProjectTemplateProvider
import org.eclipse.xtext.ui.wizard.template.ProjectTemplate

import static org.eclipse.core.runtime.IStatus.*

/**
 * Create a list with all project templates to be shown in the template new project wizard.
 * 
 * Each template is able to generate one or more projects. Each project can be configured such that any number of files are included.
 */
class ConfigModelProjectTemplateProvider implements IProjectTemplateProvider {
	override getProjectTemplates() {
		#[new RainbowProject]
	}
}

@ProjectTemplate(label="Rainbow Deployment", icon="rainbow.png", description="<p><b>Rainbow Deployment</b></p>
<p>This is a deployment project for Rainbow. You can set a parameters to modify the src diretories and the rainbow directories.</p>")
public class RainbowProject {
	
	
	val advanced = check("File Layout:", false)
	val advancedGroup = group("Properties:")
	val generateJavaCode = check("Generate Java source", true, advancedGroup)
	val srcPackage = text("Source package:", "src/main/java/", "The directory to place Java source files.", advancedGroup)
	val srcGenPackage = text("Generated source:", "src/main/java-gen", "The directory to place generated Java source files", advancedGroup)
	val rbwSrc = text("Target definitions:", "src/main/resources/rbw", "The directory to place Rainbow target definitions", advancedGroup)
	val rbwGen = text("Generated target files:", "src/main/resources/generated", "The directory to place generated target files", advancedGroup)
	val createRCLFiles = check("Generate RCL file templates", true)
	val targetSpecifiction = group("Target Contents:")
	
	val deploymentType = combo("Deployment style:", #["Single Machine", "Multiple Machines", "Custom Deployment"], "The deployment type of Rainbow", targetSpecifiction);
	val customDeployment = text("Port Factory", "", "Specify the port factory to use", targetSpecifiction);
	val useAcmeAndStitch = check('Use Acme and Stitch', true, targetSpecifiction)
	val generateRCLModelFactory = check("Generate RCL model factory", true, targetSpecifiction)
	val modelFactoryName = text("Model Factory:", "DefaultModelFactory", targetSpecifiction)
	override protected updateVariables () {
		generateJavaCode.enabled = advanced.value
		srcPackage.enabled = advanced.value && generateJavaCode.value
		rbwSrc.enabled = advanced.value
		srcGenPackage.enabled = advanced.value
		rbwGen.enabled = advanced.value
		if (!advanced.value) {
			rbwSrc.value = "src/main/resources/rbw"
			if (generateJavaCode.value)
				srcPackage.value="src/main/java/"
			
		}
		
		targetSpecifiction.enabled = createRCLFiles.value
		if (!createRCLFiles.value) {
			deploymentType.value="Single Machine"
			useAcmeAndStitch.value = true
		}
		deploymentType.enabled = createRCLFiles.value
		useAcmeAndStitch.enabled = createRCLFiles.value
		customDeployment.enabled = deploymentType.value == "Custom Deployment"
		if (deploymentType != "Custom Deployment") {
			customDeployment.value = deploymentType.value
		}
		generateRCLModelFactory.enabled = createRCLFiles.value
		modelFactoryName.enabled = createRCLFiles.value && generateRCLModelFactory.value
		
	}
	
	override protected validate() {
		if (generateJavaCode.value && !srcPackage.value.matches('[a-z][a-z0-9_]*(/[a-z][a-z0-9_]*)*'))
			new Status(ERROR, "Wizard", '''Path '«srcPackage»' is not a valid package name''')
		if (rbwSrc.value.matches('[a-z][a-z0-9_]*(/[a-z][a-z0-9_]*)*'))
			null
		else
			new Status(ERROR, "Wizard", '''Path '«rbwSrc»' is not a valid package name''')
	}
	
	var HashMap<String,String> replacements = newLinkedHashMap()
		
	var Pattern pattern = null
	
	def generateReplacements() {
		replacements = newLinkedHashMap(
			'rainbow-target' -> projectInfo.projectName,
			'rainbowVersion' -> '3.0',
			'gauges.rbw' -> "model/gauges.rbw",
			'deploymentFactory' -> (deploymentType.value=="Single Machine"?"org.sa.rainbow.core.ports.guava.GuavaRainbowPortFactory":(deploymentType.value=="Multiple Machines"?"org.sa.rainbow.core.ports.eseb.ESEBRainbowPortFactory":customDeployment.value)),
			'AnalysisName' -> "anAnalysis",
			'AnalysisClass' -> 'org.sa.rainbow.core.analysis.IRainbowAnalysis # Change this to a real class',
			'AdaptationManagerName' -> 'anAdaptationManager',
			'AdaptationClass' -> 'org.sa.rainbow.core.adaptation.org.sa.rainbow.core.adaptation # Change this to a real class',
			'ExecutorName' -> 'anExecutor',
			'ExectorClass' -> 'org.sa.rainbow.core.adaptation.IAdaptationExecutor # Change this to a real class',
			'EffectorManagerName' -> 'anEffectorManager',
			'EffectorManagerClass' -> 'org.sa.rainbow.translator.effectors.EffectorManager # Change this to a real class',
			'GUISpecs' -> '',
			'factoryName' -> modelFactoryName.value,
			'srcGenPackage' -> srcGenPackage.value,
			'rbwGen.value' -> rbwGen.value,
			'rbwSrc.value' -> rbwSrc.value
		)
		if (useAcmeAndStitch.value) {
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
		val patternString = "<<(" + replacements.keySet.join("|") + ")>>"
 		pattern = Pattern.compile(patternString);	
	} 
	
	def addProjectFiles(JavaProjectFactory tgt) {
		
		generateReplacements()
		
		tgt.addFile("pom.xml", readFile("templates/pom.xml_default"))
		if (createRCLFiles.value) {
			val tgtLoc = rbwSrc + "/" + projectInfo.projectName
			tgt.addFile(
				tgtLoc + "/properties.rbw", 
				readFile("templates/properties.rbw_template")
			)
			tgt.addFile(tgtLoc + "/model/gauges.rbw", readFile("templates/gauges.rbw_default"))
			tgt.addFile(tgtLoc + "/system/probes.rbw", readFile("templates/probes.rbw_default"))
			tgt.addFile(tgtLoc + "/system/effectors.rbw", readFile("templates/effectors.rbw_default"))
			
			if (useAcmeAndStitch.value) {
				tgt.addFile(tgtLoc + "/stitch/stitch.s", "// Edit to add StitchsStrategies and tactics")
				tgt.addFile(tgtLoc + "/model/model.acme", "// Edit to add Acme model")
				tgt.addFile(tgtLoc + "/stitch/utilities.rbw", readFile('templates/utilities.rbw_default'))
			}
			if (generateRCLModelFactory.value) {
				tgt.addFile(rbwSrc + "/" + modelFactoryName.value + ".rbw", readFile("templates/modelfactory.rbw_default"))
			}
		}
	}
	
	def readFile(String fileName) {
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
	
	override generateProjects(IProjectGenerator generator) {
		generator.generate(new JavaProjectFactory => [
			projectName = projectInfo.projectName
			location = projectInfo.locationPath
			projectNatures += XtextProjectHelper.NATURE_ID
			builderIds += XtextProjectHelper.BUILDER_ID
			folders += rbwSrc.value
			if (generateJavaCode.value) {
				projectNatures += #["org.eclipse.jdt.core.javanature", "org.eclipse.m2e.core.maven2Nature"]
				builderIds += #["org.eclipse.m2e.core.maven2Builder", "org.eclipse.jdt.core.javabuilder"]
				addSourceFolder(srcPackage.value, "target/classes", false)
				addSourceFolder(srcGenPackage.value, "target/classes", false)
			}
			addProjectFiles(it)
		])
	}
	
}
