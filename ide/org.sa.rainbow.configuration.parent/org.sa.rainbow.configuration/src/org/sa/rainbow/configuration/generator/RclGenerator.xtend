/*
 * generated by Xtext 2.19.0
 */
package org.sa.rainbow.configuration.generator

import java.util.HashMap
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.common.types.JvmType
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.sa.rainbow.configuration.RainbowOutputConfigurationProvider
import org.sa.rainbow.configuration.Utils
import org.sa.rainbow.configuration.XtendUtils
import org.sa.rainbow.configuration.rcl.Actual
import org.sa.rainbow.configuration.rcl.Array
import org.sa.rainbow.configuration.rcl.Assignment
import org.sa.rainbow.configuration.rcl.BooleanLiteral
import org.sa.rainbow.configuration.rcl.CommandCall
import org.sa.rainbow.configuration.rcl.Component
import org.sa.rainbow.configuration.rcl.ComponentType
import org.sa.rainbow.configuration.rcl.DeclaredProperty
import org.sa.rainbow.configuration.rcl.DoubleLiteral
import org.sa.rainbow.configuration.rcl.Effector
import org.sa.rainbow.configuration.rcl.Factory
import org.sa.rainbow.configuration.rcl.Gauge
import org.sa.rainbow.configuration.rcl.GaugeType
import org.sa.rainbow.configuration.rcl.IPLiteral
import org.sa.rainbow.configuration.rcl.ImpactVector
import org.sa.rainbow.configuration.rcl.IntegerLiteral
import org.sa.rainbow.configuration.rcl.LogLiteral
import org.sa.rainbow.configuration.rcl.Probe
import org.sa.rainbow.configuration.rcl.PropertyReference
import org.sa.rainbow.configuration.rcl.RainbowConfiguration
import org.sa.rainbow.configuration.rcl.Reference
import org.sa.rainbow.configuration.rcl.StringLiteral
import org.sa.rainbow.configuration.rcl.Value
import org.sa.rainbow.configuration.validation.RclValidator

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class RclGenerator extends AbstractGenerator {

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val model = resource.contents.head as RainbowConfiguration
		var String filename = null
		if (model.export !== null) { 
			filename='''«model.targetName»/«model.export.filename»'''	
		}
		if(filename === null) 
			filename = ("generated-unnamed");
		if (model.probes !== null && !model.probes.isEmpty) {
			fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT,
				outputProbeSpec(resource.URI, model.probes, model.delcaredProperties))
		}
		if ((model.gauges !== null || model.gaugeTypes !== null) &&
			(!model.gauges.isEmpty || !model.gaugeTypes.isEmpty)) {
			fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT,
				outputGuageSpec(resource.URI, model.gauges, model.gaugeTypes, model.delcaredProperties))
			for (gauge : model.gaugeTypes) {
				val hasRegexp = !gauge.body.commands.filter[it.regexp!==null].empty
				if (hasRegexp) {
					val clazz = XtendUtils.unpackString((gauge.body.assignment.findFirst[it.name == "setup"].value.value as Component).assignment.findFirst[it.name=="generatedClass"].value.value as StringLiteral, true)
					var gCFilename = clazz
					gCFilename = gCFilename.replaceAll("\\.", "/") + ".java"
					fsa.generateFile(gCFilename, RainbowOutputConfigurationProvider::RAINBOW_GENERRATED_SOURCE_OUTPUT, toJavaCode(resource.URI, gauge, clazz));
				}
			}
		}
		if ((model.effectors !== null || model.effectorTypes !== null) &&
			(!model.effectors.isEmpty || !model.effectorTypes.isEmpty)) {
			fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT,
				outputEffectorSpec(resource.URI, model.effectors, model.effectorTypes, model.delcaredProperties))
		}
		val utilities = EcoreUtil2.getAllContentsOfType(model, DeclaredProperty).filter[it.component == ComponentType.UTILITY]
		if (!utilities.empty && !model.impacts.empty) {
			val output = outputUtilityModel(resource.URI,utilities, model.impacts)
			fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT, output)
		}
		val uis = EcoreUtil2.getAllContentsOfType(model, DeclaredProperty).filter[it.component == ComponentType.GUI]
		if (!uis.empty) {
			val output = outputUIModel(resource.URI, uis);
			fsa.generateFile(model.targetName + "/ui.yml", RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT, output)
		}

		if (filename.endsWith("properties") ||
			(model.gauges === null && model.probes == null && model.delcaredProperties != null)) {
			var output = outputPropertiesSpec(resource.URI, model.delcaredProperties)
			fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_TARGET_PROPERTIES_OUTPUT, output);
		}
		if (model.factories !== null && !model.factories.empty) {
			for (f : model.factories) {
				outputCommandFactory(resource, fsa, f)
			}
		}

//		fsa.generateFile('greetings.txt', 'People to greet: ' + 
//			resource.allContents
//				.filter(Greeting)
//				.map[name]
//				.join(', '))
	}
	
	def outputUIModel(URI uri, Iterable<DeclaredProperty> properties) {
		val ui = EcoreUtil2.getAllContentsOfType(properties.get(0), Assignment).findFirst[it.name=='specs']
		val gauges = EcoreUtil2.getAllContentsOfType(ui, Assignment).filter[it.name=='gauge']
		val analyzers = EcoreUtil2.getAllContentsOfType(ui, Assignment).filter[it.name=='analyzers'].map[EcoreUtil2.getAllContentsOfType(it, Assignment)].flatten.filter[it.name=="analyzer"]
		val managers = EcoreUtil2.getAllContentsOfType(ui, Assignment).filter[it.name=='managers'].map[EcoreUtil2.getAllContentsOfType(it, Assignment)].flatten.filter[it.name=="manager"]
		val executors = EcoreUtil2.getAllContentsOfType(ui, Assignment).filter[it.name=='executors'].map[EcoreUtil2.getAllContentsOfType(it, Assignment)].flatten.filter[it.name=="executor"]
		val details = EcoreUtil2.getAllContentsOfType(ui, Assignment).filter[it.name=='details'].map[EcoreUtil2.getAllContentsOfType(it, Assignment)].flatten
		'''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################
		gauges:
		  «FOR g : gauges»
		    «(((g.value.value as Component).assignment.findFirst[it.name=='type'].value.value as PropertyReference).referable as GaugeType).name»:
		      builtin:
		        category: «XtendUtils.unpackString((g?.value?.value as Component)?.assignment?.findFirst[it.name=='category']?.value?.value as StringLiteral, true)»
		        command:   «getCommand(g)»
		        value: «((g?.value?.value as Component)?.assignment?.findFirst[it.name=='value.parameter']?.value?.value as IntegerLiteral).value»
		        «IF (g?.value?.value as Component)?.assignment?.findFirst[it.name=='upper'] !== null»
		          upper: «((g?.value?.value as Component)?.assignment?.findFirst[it.name=='upper'].value.value as DoubleLiteral).value»
		        «ENDIF»
		        «IF (g?.value?.value as Component)?.assignment?.findFirst[it.name=='lower'] !== null»
		          lower: «((g?.value?.value as Component)?.assignment?.findFirst[it.name=='lower'].value.value as DoubleLiteral).value»
		        «ENDIF»
		  «ENDFOR»
		analyzers:
		  «FOR a : analyzers»
		    «(((((a.value.value as Component).assignment.findFirst[it.name=='for'].value.value as PropertyReference).referable as DeclaredProperty).^default.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»: «((a.value.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»
		  «ENDFOR»
		managers:
		  «FOR a : managers»
		    «(((((a.value.value as Component).assignment.findFirst[it.name=='for'].value.value as PropertyReference).referable as DeclaredProperty).^default.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»: «((a.value.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»
		  «ENDFOR»
		executors:
		  «FOR a : executors»
		    «(((((a.value.value as Component).assignment.findFirst[it.name=='for'].value.value as PropertyReference).referable as DeclaredProperty).^default.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»: «((a.value.value as Component).assignment.findFirst[it.name=='class'].value.value as Reference).referable.qualifiedName»
		  «ENDFOR»
		details:
		  «FOR d : details»
		    «d.name»: «(d.value.value as Reference).referable.qualifiedName»
		  «ENDFOR»
		'''
	}
	
	def getCommand(Assignment g) {
		val gt = (((g.value.value as Component).assignment.findFirst[it.name=='type'].value.value as PropertyReference).referable as GaugeType)
		val cmd = XtendUtils.unpackString((g?.value?.value as Component)?.assignment?.findFirst[it.name=='command']?.value?.value as StringLiteral, true)
		val command = gt.body.commands.findFirst[it.name == cmd]
		if (command !== null) {
			'''«IF command.target !== null»«command.target».«ENDIF»«command.command»«FOR p : command.formal BEFORE '(' SEPARATOR ', ' AFTER ')'»«p.simpleName»«ENDFOR»'''
		}
		
	}
	
	
	def outputCommandFactory(Resource resource, IFileSystemAccess2 fsa, Factory factory) {
		var filename = factory.clazz
		filename = filename.replaceAll("\\.", "/") + ".java"
		fsa.generateFile(filename, RainbowOutputConfigurationProvider::RAINBOW_GENERRATED_SOURCE_OUTPUT, toJavaCode(resource.URI, factory));
	}
	
	def toJavaCode(URI uri, GaugeType gt, String clazz) '''
		// This file was generated by the Rainbow configuration generator
		// Sourcee: «uri.toString» gaugeType = «gt.name»
		package «clazz.substring(0, clazz.lastIndexOf("."))»;
		import java.util.List;
		import java.util.Map;
		import java.util.regex.Matcher;
		import java.util.regex.Pattern;
		
		import org.sa.rainbow.core.error.RainbowException;
		import org.sa.rainbow.core.models.commands.IRainbowOperation;
		import org.sa.rainbow.core.util.TypedAttribute;
		import org.sa.rainbow.core.util.TypedAttributeWithValue;
		import org.sa.rainbow.core.gauges.ConfigurableRegularPatternGauge;
		
		public class «clazz.substring(clazz.lastIndexOf(".")+1)» extends ConfigurableRegularPatternGauge {
			protected static final String THREAD_NAME = "«gt.name»";
			
			«FOR cmd : gt.body.commands»
			  public static final String «cmd.command.toUpperCase.replaceAll("[\\-.]","_")» = "«cmd.command»";
			  public static final String «cmd.command.toUpperCase.replaceAll("[\\-.]","_")»_PATTERN = "«XtendUtils.unpackString(cmd.regexp, true, true).replace("\\", "\\\\")»";
			«ENDFOR»
			
			public «clazz.substring(clazz.lastIndexOf(".")+1)» (
				String id,
				long beaconPeriod,
				TypedAttribute gaugeDesc,
				TypedAttribute modelDesc, 
				List<TypedAttributeWithValue> setupParams,
				Map<String, IRainbowOperation> mappings
			) throws RainbowException {
				super(THREAD_NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
			}
			
			@Override
			protected void loadPatterns () {
				«FOR cmd : gt.body.commands»
					addPattern(«cmd.command.toUpperCase.replaceAll("[\\-.]","_")», Pattern.compile(«cmd.command.toUpperCase.replaceAll("[\\-.]","_")»_PATTERN));
				«ENDFOR»
			}
			
			
		}
	'''
	
	def toJavaCode(URI uri, Factory factory) '''
		// This file was generated by the Rainbow configuration generator
		// Sourcee: «uri.toString»
		package «factory.clazz.substring(0, factory.clazz.lastIndexOf("."))»;
		import java.io.InputStream;
		import org.acmestudio.acme.element.*;
		import org.sa.rainbow.core.error.RainbowException;
		import org.sa.rainbow.core.models.ModelsManager;
		
		import «factory.defn.modelClass.qualifiedName»;
		«IF factory.defn.extends !== null»
			import «factory.defn.extends.qualifiedName»;
		«ENDIF»
		import «factory.defn.loadCmd.qualifiedName»;
		«IF factory.defn.saveCmd !== null»
			import «factory.defn.saveCmd.qualifiedName»;
		«ENDIF»
		
		import incubator.pval.Ensure;
		
		«FOR cmd : factory.defn.commands»
			import «cmd.cmd.qualifiedName»;
		«ENDFOR»
		
		public class «factory.clazz.substring(factory.clazz.lastIndexOf(".")+1)»
		  «IF factory.defn.extends !== null»
		  	extends «factory.defn.extends.simpleName»
		  «ENDIF»
		{
			public static «factory.defn.loadCmd.simpleName» loadCommand (ModelsManager modelsManager,
					String modelName,
			        InputStream stream,
			        String source) {
			  	return new «factory.defn.loadCmd.simpleName» (modelName, modelsManager, stream, source);
			}
			
			public «classFor(factory.clazz)» («factory.defn.modelClass.simpleName» model) throws RainbowException {
				super(model);	
			}
			
			«FOR cmd : factory.defn.commands»
				public static final String «constantName(cmd.name)» = "«cmd.name»";
			«ENDFOR»
			
			«FOR cmd : factory.defn.commands»
				@Operation(name=«constantName(cmd.name)»)
				public «cmd.cmd.simpleName» «cmd.name»Cmd «FOR p : cmd.formal BEFORE '(' SEPARATOR ',' AFTER ')'»«XtendUtils.formalTypeName(p, false)» «p.name»	«ENDFOR»
				{
					«FOR p : cmd.formal»
					  «IF p.type.acme !== null»
					    Ensure.is_true(«p.name».declaresType("«XtendUtils.getAcmeTypeName(p.type.acme.referable)»"));
					  «ENDIF»
					«ENDFOR»
					return new «cmd.cmd.simpleName» («constantName(cmd.name)», («factory.defn.modelClass.simpleName» )m_modelInstance, «IF cmd.formal.get(0).name != "target"»"", «ENDIF»«FOR p : cmd.formal SEPARATOR ', '»«XtendUtils.convertToString(p)»«ENDFOR»);
				}
				
			«ENDFOR»
		}
		
	'''
	
	def constantName(String string) {
		string.replaceAll('([A-Z])', '_$1').toUpperCase + "_CMD"
	}
	
	def classFor(String name) {
		if (name.contains(".")) return name.substring(name.lastIndexOf(".")+1)
		else return name
	}
		
	def outputUtilityModel(URI uri, Iterable<DeclaredProperty> properties, EList<ImpactVector> list) {
		var model = ""
		var utilities = ""
		var scenarios = ""
		for (um : properties) {
			val value = um.^default.value as Component
			val m = value.assignment.findFirst[it.name=='model'].value.value as PropertyReference
			val theModel = m.referable as DeclaredProperty
			val component = theModel.^default.value  as Component
			val name = stringValue((component).assignment.findFirst[it.name == "name"]?.value) ?:
				theModel.name
			var type = stringValue(component.assignment.findFirst[it.name == "type"]?.value)
			if (type === null) {
				type = getModelTypeFromClass(
					((component.assignment.findFirst[it.name == "factory"]?.value.value) as Reference).
						referable, "MODELTYPE")
				if (type === null) {
					type = "???"
				}
			}
			model = '''
			model:
			  name: «name»
			  type: «type»
			'''
			val us = value.assignment.findFirst[it.name=='utilities'].value.value as Component
			
			utilities = '''
			utilities:
			  «FOR u : us.assignment»
			    «u.name»:
			      label: «surroundString(XtendUtils.unpackString((u.value.value as Component).assignment.findFirst[it.name=='label']?.value.value as StringLiteral, false), true)»
			      mapping: «surroundString(XtendUtils.unpackString((u.value.value as Component).assignment.findFirst[it.name=='mapping']?.value.value as StringLiteral, false), true)»
			      description: «surroundString(XtendUtils.unpackString((u.value.value as Component).assignment.findFirst[it.name=='description']?.value.value as StringLiteral, false), true)»
			      utility:
			        «FOR uv : ((u.value.value as Component).assignment.findFirst[it.name=='utility']?.value.value as Array).values»
			          «RclValidator.getNumber((uv.value as Array).values.get(0))»: «RclValidator.getNumber((uv.value as Array).values.get(1))»
			        «ENDFOR»
			  «ENDFOR»
			  '''
			val scs = value.assignment.findFirst[it.name=='scenarios'].value.value as Array
			scenarios = '''
			weights:
			  «FOR scenario : scs.values»
			  «XtendUtils.unpackString((scenario.value as Component).assignment.findFirst[it.name=="name"].value.value as StringLiteral, true)»:
			    «FOR u : (scenario.value as Component).assignment.filter[it.name != "name"]»
			      «u.name»: «RclValidator.getNumber(u.value)»
			    «ENDFOR»
			  «ENDFOR»
			'''
		}
		
		val vectors='''
		vectors:
		  «FOR iv : list»
		    «iv.tactic.name»:
		      «FOR u : (iv.component.assignment)»
		        «u.name»: «RclValidator.getNumber(u.value)»
		      «ENDFOR»
		  «ENDFOR»
		'''
		'''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################
		«model»
		«utilities»
		«scenarios»
		«vectors»
		'''

	}

	def outputPropertiesSpec(URI uri, EList<DeclaredProperty> vars) {
		val models = vars.filter[it?.component === ComponentType.MODEL]
		val analysis = vars.filter[it?.component === ComponentType.ANALYSIS]
		val executors = vars.filter[it?.component === ComponentType.EXECUTOR]
		val gui = vars.filter[it?.component === ComponentType.GUI]
		val manager = vars.filter[it?.component === ComponentType.MANAGER]
		val effectors = vars.filter[it?.component == ComponentType.EFFECTORMANAGER]
		var properties = vars.filter[
			it?.component === ComponentType.PROPERTY
		]
		'''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################

		«FOR v : properties»
			«IF v.^default !== null»
				«v.name» = «Utils.removeQuotes(stringValue(v.^default,false,false))»
			«ENDIF»
		«ENDFOR»
		«outputModels(models)»
		«outputAnalyses(analysis)»
		«outputManagers(manager)»
		«outputExecutors(executors)»
		«outputEffectorManagers(effectors)»
		«outputGUI(gui)»
		'''
	}
		
	def outputGUI(Iterable<DeclaredProperty> properties) {
		if (properties.empty) ''''''
		else {
			val gui = (properties.get(0).^default.value as Component).assignment.findFirst[it.name=='class']
			var p = '''rainbow.gui=«(gui.value.value as Reference).referable.qualifiedName»'''
			val guispecs = (properties.get(0).^default.value as Component).assignment.findFirst[it.name=='specs']
			if (guispecs !== null) {
				p = p +'''
				rainbow.gui.specs=ui.yml'''
			}
			p
		}
	}
		
		def outputEffectorManagers(Iterable<DeclaredProperty> ems) {
			if (ems === null || ems.empty) return ""
			var i = 0
			var sb = new StringBuffer('''rainbow.effector.manager.size=«ems.size»
			''')
			for (e : ems) {
				val ass = e.^default.value as Component
				val class = ass.assignment.findFirst[it.name == "class"]
				sb.append('''rainbow.effector.manager.class_«i»=«(class.value.value as Reference).referable.qualifiedName»
			''')
			}
			sb.toString
		}
		
		def outputAnalyses(Iterable<DeclaredProperty> analyses) {
			if (analyses === null || analyses.empty) return ""
			var i = 0
			var sb = new StringBuffer('''rainbow.analyses.size=«analyses.size»
			''')
			for (a : analyses) {
				val ass = a.^default.value as Component
				val class = ass.assignment.findFirst[it.name == "class"]
				sb.append('''rainbow.analyses_«i»=«(class.value.value as Reference).referable.qualifiedName»
			''')
			}
			sb.toString
		}

	def outputExecutors(Iterable<DeclaredProperty> executors) {
		if(executors === null || executors.empty) return ""
		var i = 0
		var sb = new StringBuffer('''rainbow.adaptation.executor.size=«executors.size»
		''')
		for (e : executors) {
			val ass = e.^default.value as Component
			val class = ass.assignment.findFirst[it.name == "class"]
			val model = ass.assignment.findFirst[it.name == "model"]
			sb.append('''rainbow.adaptation.executor.class_«i»=«(class.value.value as Reference).referable.qualifiedName»
			''')
			sb.append('''rainbow.adaptation.executor.model_«i»=''')
			val theModel = (model.value.value as PropertyReference).referable as DeclaredProperty
			val component = theModel.^default.value  as Component
			val name = stringValue((component).assignment.findFirst[it.name == "name"]?.value) ?:
				theModel.name
			var type = stringValue(component.assignment.findFirst[it.name == "type"]?.value)
			if (type === null) {
				type = getModelTypeFromClass(
					((component.assignment.findFirst[it.name == "factory"]?.value.value) as Reference).
						referable, "MODELTYPE")
				if (type === null) {
					type = "???"
				}
			}
			sb.append('''«name»:«type»
			''')
			i = i + 1
		}
		sb.toString
	}

	def outputManagers(Iterable<DeclaredProperty> managers) {
		if(managers === null || managers.empty) return ""
		var i = 0
		var sb = new StringBuffer('''rainbow.adaptation.manager.size=«managers.size»
		''')
		for (m : managers) {
			val ass = m.^default.value as Component
			val class = ass.assignment.findFirst[it.name == "class"]
			val model = ass.assignment.findFirst[it.name == "model"]
			sb.append('''rainbow.adaptation.manager.class_«i»=«(class.value.value as Reference).referable.qualifiedName»
			''')
			sb.append('''rainbow.adaptation.manager.model_«i»=''')
			val theModel = (model.value.value as PropertyReference).referable as DeclaredProperty
			val component = theModel.^default.value  as Component
			val name = stringValue((component).assignment.findFirst[it.name == "name"]?.value) ?:
				theModel.name
			var type = stringValue(component.assignment.findFirst[it.name == "type"]?.value)
			if (type === null) {
				type = getModelTypeFromClass(
					((component.assignment.findFirst[it.name == "factory"]?.value.value) as Reference).
						referable, "MODELTYPE")
				if (type === null) {
					type = "???"
				}
			}
			sb.append('''«name»:«type»
			''')
			i = i + 1
		}
		sb.toString
	}

	def getModelTypeFromClass(JvmType type, String stringConstant) {
		// val tp = jvmTypeProviderFactory.createTypeProvider
		val class = this.class.classLoader.loadClass(type.qualifiedName)
		val typeField = class.getDeclaredField(stringConstant)
		if (typeField === null) {
			return null
		}
		val value = typeField.get(null)
		return value as String
	}

	def outputModels(Iterable<DeclaredProperty> models) {
		if(models === null || models.empty) return ""
		var i = 0
		val sb = new StringBuffer('''rainbow.model.number = «models.size»
		''')
		for (m : models) {
			val ass = m.^default.value as Component
			val loadclass = ass.assignment.findFirst[it.name == "factory"]
			val path = ass.assignment.findFirst[it.name == "path"]
			val saveOnClose = ass.assignment.findFirst[it.name == "saveOnClose"]
			val saveLocation = ass.assignment.findFirst[it.name == "saveLocation"]
			val name = Utils.removeQuotes(stringValue(ass.assignment.findFirst[it.name == "name"]?.value)) ?: m.name
			sb.append('''rainbow.model.name_«i»=«name»
			''')
			if (loadclass.value.value instanceof Reference) {
				sb.append('''rainbow.model.load.class_«i»=«(loadclass.value.value as Reference).referable.qualifiedName»
				''')
			}
			else if (loadclass.value.value instanceof PropertyReference &&
					(loadclass.value.value as PropertyReference).referable instanceof Factory
			) {
				val factory = (loadclass.value.value as PropertyReference).referable as Factory
				sb.append('''rainbow.model.load.class_«i»=«factory.clazz»
				''')
			}
			if (path !== null) {
				sb.append('''rainbow.model.path_«i»=«Utils.removeQuotes(stringValue(path.value, false, true))»
				''')
			}
			if (saveOnClose !== null) {
				sb.append('''rainbow.model.saveOnClose_«i»=«Utils.removeQuotes(stringValue(saveOnClose.value, false, false))»
				''')
			}
			if (saveLocation !== null) {
				sb.append('''rainbow.model.saveLocation_«i»=«Utils.removeQuotes(stringValue(saveLocation.value, false, false))»
				''')
			}
			i = i + 1
		}

		sb.toString
	}

	def outputGuageSpec(URI uri, EList<Gauge> gauges, EList<GaugeType> gaugeTypes, EList<DeclaredProperty> vars) '''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################
		«IF !vars.empty»
			vars:
			  «FOR v : vars»
			  	«IF v.^default !== null»
			  		«v.name»: «stringValue(v.^default, false,true)»
			  	«ENDIF»
			  «ENDFOR»
		«ENDIF»
		gauge-types:
		  «FOR type : gaugeTypes»
		  	«outputGaugeType(type)»
		  «ENDFOR»
		gauge-instances:
		  «FOR gauge : gauges»
		  	«outputGauge(gauge)»
		  «ENDFOR»
		  
	'''

	def outputProbeSpec(URI uri, EList<Probe> probes, EList<DeclaredProperty> vars) '''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################
		«IF !vars.empty»
			vars:
			  «FOR v : vars»
			  	«IF v.^default !== null»
			  		«v.name» : «stringValue(v.^default, false,true)»
				«ENDIF»
			«ENDFOR»
		«ENDIF»
		probes:
		  «FOR probe : probes»
		  	«outputProbe(probe)»
		  «ENDFOR»
	'''

	def outputEffectorSpec(URI uri, EList<Effector> effectors, EList<Effector> effectorTypes, EList<DeclaredProperty> vars) '''
		#############################################################
		# Generated by Rainbow XText Configuration DSL -- DO NOT EDIT
		# Source: «uri.toString»
		#############################################################
		«IF !vars.empty»
			vars:
			  «FOR v : vars»
			  	«IF v.^default !== null»
			  		«v.name» : «stringValue(v.^default, false, true)»
			  	«ENDIF»
			  «ENDFOR»
		«ENDIF»
		effector-types:
		  «FOR type : effectorTypes»
		  	«outputEffector(type)»
		  «ENDFOR»
		effectors:
		  «FOR eff : effectors»
		  	«outputEffector(eff)»
		  «ENDFOR»
	'''

	def outputGauge(Gauge gauge) {
		val setup = gauge.body.assignment.stream.filter[it.name == "setup"].findAny
		val config = gauge.body.assignment.stream.filter[it.name == "config"].findAny
		var CharSequence modelName = '''«gauge.body.modelName»:«gauge.body.modeltype»'''
		if (gauge.body.ref !== null) {
			var CharSequence internalName = gauge.body.ref.referable.name
			var CharSequence type = ""
			var gbv = (gauge.body.ref.referable as DeclaredProperty)?.^default.value
			if (gbv instanceof Component) {
				val comp = gbv as Component
				type = stringValue(comp.assignment.findFirst[it.name=="type"].value)
				val n = comp.assignment.findFirst[it.name=="name"]
				if (n !== null) {
					internalName = stringValue(n.value)
				}
			}
			modelName = '''«internalName»:«type»'''
		}
		'''
			«gauge.name»:
			  «IF gauge.superType !== null»
			  	type: «gauge.superType.name»
			  «ENDIF»
			  model: "«modelName»"
			  commands:
			    «FOR command : gauge.body.commands»
			    	«command.name»: «outputCall(command)»
			  «ENDFOR»
			  «IF setup.present && setup.get.value.value instanceof Component»
			  	setupValues:
			  	  «FOR p : (setup.get.value.value as Component).assignment»
			  	  	«p.name»: «stringValue(p.value,false,true)»
			  	  «ENDFOR»
			  «ENDIF»
			  «IF config.present && config.get.value.value instanceof Component»
			  	configValues:
			  	  «FOR p : (config.get.value.value as Component).assignment»
			  	  	«IF p.name == 'targetProbe'»
			  	  	  «IF p.value.value instanceof Array»
			  	  	    targetProbeList: «extractProbeAliases(p.value.value as Array)»
			  	  	  «ELSEIF p.value.value instanceof PropertyReference && (p.value.value as PropertyReference).referable instanceof Probe»
			  	  	    targetProbeType: «extractProbeAlias((p.value.value as PropertyReference).referable as Probe)»
			  	  	  «ENDIF»
			  	  	«ELSE»
			  	  	  «p.name»: «stringValue(p.value,false, true)»
			  	  	«ENDIF»
			  	  «ENDFOR»
			  «ENDIF»
		'''
	}
	
	def extractProbeAliases(Array array) {
		array.values.filter[it.value instanceof PropertyReference && (it.value as PropertyReference).referable instanceof Probe].map[extractProbeAlias((it.value as PropertyReference).referable as Probe)].join(", ")
	}
	
	def extractProbeAlias(Probe probe) {
		'''«stringValue(probe.properties.assignment.findFirst[name=='alias']?.value)»'''
	}

	def outputCall(CommandCall call) {
		var ret = new StringBuilder()
		if (call.target !== null) {
			ret.append(call.target).append(".")
		} else if (call.ref != null) {
			ret.append("${").append(call.ref.referable.name).append("}.")
		}
		ret.append(call.command)
		val params = '''(«call.actual.map[actual(it)].join(", ")»)'''
		ret.append(params)//.append("\"")
		ret
	}

	def actual(Actual p) {
		if (p.id === null && p.pr === null && p.ng === null && p.value !== null)
			stringValue(p.value)
		else if (p.pr !== null)
			'''${«p.pr.referable.name»}'''
		else if (p.ref && p.id !== null) '''$<«p.id»>''' 
		else if (p.ref && p.ng !== null)  '''$<«p.ng»>'''
		else if (p.ref )  '''$<«p.ag»>'''
		else '''«p.id»'''
	}

	def outputGaugeType(GaugeType type) {
		val setup = type.body.assignment.stream.filter[it.name == "setup"].findAny
		val config = type.body.assignment.stream.filter[it.name == "config"].findAny
		'''
			«type.name»:
			  commands:
			    «FOR command : type.body.commands»
			    	«command.name»: «IF command.target !== null»«command.target».«ENDIF»«command.command»«FOR p : command.formal BEFORE '(' SEPARATOR ', ' AFTER ')'»«p.simpleName»«ENDFOR»
			    «ENDFOR»
			  «IF setup.present && setup.get.value.value instanceof Component»
			  	setupParams:
			  	  «FOR p : (setup.get.value.value as Component).assignment»
			  	  	«IF p.name == "generatedClass"»
			  	  	  javaClass:
			  	  	«ELSE»
			  	  	  «p.name»:
			  	  	«ENDIF» 
			  	  	  type: «typeValue(p.value)»
			  	  	  default: «stringValue(p.value, false, true)»
			  	  «ENDFOR»
			  «ENDIF»
			  «IF config.present && config.get.value.value instanceof Component»
			  	configParams:
			  	  «FOR p : (config.get.value.value as Component).assignment»
			  	    «IF p.name != "targetProbe"»
			  	      «p.name»:
			  	        «IF p.value?.value instanceof Reference»
			  	          type: «(p.value.value as Reference).referable.simpleName»
			  	          default: ~
			  	        «ELSE»
			  	          type: «typeValue(p.value)»
			  	          default: «stringValue(p.value,false,true)»
			  	        «ENDIF»			  	     	
			  	 	«ENDIF»
			  	 «ENDFOR»
			  «ENDIF»
		'''

	}

	def outputEffector(Effector effector) {
		val attsST = getAssignmentsMap(effector?.superType?.body?.assignment)
		val atts = getAssignmentsMap(effector?.body?.assignment)
		val command = if (effector.body.command !== null) {
				effector.body.command
			} else if (effector?.superType?.body?.command !== null) {
				effector.superType.body.command
			}
		return '''
			«effector.name»:
			  «IF command !== null»
			  	command: "«outputCall(command)»"
			  «ENDIF»
			  «FOR entry : attsST.entrySet()»
			  	«IF !atts.containsKey(entry.getKey())»
			  		«IF entry.getKey() == "script"»
			  			type: script
			  			scriptInfo: «stringValue(entry.getValue().value, true, null)»
			  		«ELSEIF entry.getKey() == "java"»
			  			type: java
			  			javaInfo: «stringValue(entry.getValue().value, true, null)»
			  		«ELSE»
			  			«entry.getKey()»: «stringValue(entry.getValue().value, true, true)»
			  		«ENDIF»
			  	«ENDIF»
			  «ENDFOR»
			  «FOR entry : atts.entrySet()»
			  	«IF entry.getKey() == "script"»
			  		type: script
			  		scriptInfo: «stringValue(entry.getValue().value, true, null)»
			  	«ELSEIF entry.getKey() == "java"»
			  		type: java
			  		javaInfo: «stringValue(entry.getValue().value, true, null)»
			  	«ELSE»
			  		«entry.getKey()»: «stringValue(entry.getValue().value, true, true)»
			  	«ENDIF»
			  «ENDFOR»
		'''
	}

	def outputProbe(Probe probe) {
		val attsST = getAssignmentsMap(probe?.superType?.properties?.assignment)
		val atts = getAssignmentsMap(probe?.properties?.assignment)
		return '''
			«probe.name»:
			  «FOR entry : attsST.entrySet()»
			  	«IF !atts.containsKey(entry.getKey())»
			  		«IF entry.getKey() == "script"»
			  			type: script
			  			scriptInfo: «stringValue(entry.getValue().value, true, null)»
			  		«ELSEIF entry.getKey() == "java"»
			  			type: java
			  			javaInfo: «stringValue(entry.getValue().value, true, null)»
			  		«ELSE»
			  			«entry.getKey()» : «stringValue(entry.getValue().value, true,true)»
			  		«ENDIF»
			  	«ENDIF»
			  «ENDFOR»
			  «FOR entry : atts.entrySet()»
			  	«IF entry.getKey() == "script"»
			  		type: script
			  		scriptInfo: «stringValue(entry.getValue().value, true, attsST.get(entry.getKey())?.value)»
			  	«ELSEIF entry.getKey() == "java"»
			  		type: java
			  		javaInfo: «stringValue(entry.getValue().value, true, attsST.get(entry.getKey())?.value)»
			  	«ELSEIF entry.getKey() == "mode"»
			  		mode: «Utils.removeQuotes(stringValue(entry.getValue().value, true, attsST.get(entry.getKey())?.value))»
			  	«ELSE»
			  		«entry.getKey()» : «stringValue(entry.getValue().value, true, attsST.get(entry.getKey())?.value)»
			  	«ENDIF»
			  «ENDFOR»
		'''
	}

	def getAssignmentsMap(EList<Assignment> assignments) {
		val map = new HashMap<String, Assignment>()
		if (assignments !== null) {
			for (assignment : assignments) {
				map.put(assignment.name, assignment)
			}
		}
		return map
	}

	def String stringValue(EObject value) {
		switch value {
			StringLiteral:
				XtendUtils.unpackString(value, true)
			IntegerLiteral: '''«value.value»'''
			DoubleLiteral: '''«value.value»'''
			BooleanLiteral: '''«value.isTrue»'''
			Value: stringValue(value.value)
		}
		
	}

	def stringValue(Value value, boolean allowComponent, Value mergeComponentWith) {
		if (value.value instanceof Component && allowComponent) {
			var comp = value.value as Component
			val atts = getAssignmentsMap(comp.assignment)
			val attsST = getAssignmentsMap((mergeComponentWith?.value as Component)?.assignment)
			return '''
				
				  «FOR entry : attsST.entrySet()»
				  	«IF !atts.containsKey(entry.getKey())»
				  		«entry.getKey()»: «stringValue(entry.getValue().value, true, false)»
				  	«ENDIF»
				  «ENDFOR»
				  «FOR entry : atts.entrySet()»
				  	«entry.getKey()»: «stringValue(entry.getValue().value, true, false)»
				  «ENDFOR»
			'''
		}
		else {
			stringValue(value.value)
		}
	}

	def typeValue(Value value) {
		val v = value.value
		switch v {
			StringLiteral: "String"
			BooleanLiteral: "Boolean"
			IntegerLiteral: "Integer"
			DoubleLiteral: "Double"
			Reference: "String"
			PropertyReference: "String"
			default: '''unknown'''
		}

	}

	def CharSequence stringValue(Value value, boolean allowComposite, boolean asString) {
		var v = value.value
		switch v {
			StringLiteral: '''"«XtendUtils.unpackString(v,true)»"'''
			BooleanLiteral:
				surroundString('''«v.isTrue»''', asString)
			IntegerLiteral: '''«v.value»'''
			DoubleLiteral: '''«v.value»'''
			LogLiteral:
				surroundString('''«v.value»''', asString)
			IPLiteral:
				surroundString('''«v.value»''', asString)
			Reference:
				surroundString('''«v.referable.qualifiedName»''', asString)
			PropertyReference:
				if (v.referable instanceof DeclaredProperty) {
					surroundString('''${«v.referable.name»}''', asString)					
				}
				else {
					v.referable.name
				}
			
			Component case allowComposite: '''"Not implemented yet"'''
			default: '''"Not implemented yet"'''
		}

	}

	def surroundString(String string, boolean b) {
		if (b) '''"«string»"''' else string
	}

	

//			}
}