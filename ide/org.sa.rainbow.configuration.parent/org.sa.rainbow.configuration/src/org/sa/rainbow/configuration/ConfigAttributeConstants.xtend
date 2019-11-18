package org.sa.rainbow.configuration

import com.google.inject.Inject
import com.google.inject.name.Named
import java.util.Collections
import java.util.HashSet
import java.util.Set
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.common.types.util.RawSuperTypes
import org.sa.rainbow.configuration.configModel.BooleanLiteral
import org.sa.rainbow.configuration.configModel.Component
import org.sa.rainbow.configuration.configModel.ComponentType
import org.sa.rainbow.configuration.configModel.IPLiteral
import org.sa.rainbow.configuration.configModel.IntegerLiteral
import org.sa.rainbow.configuration.configModel.ProbeReference
import org.sa.rainbow.configuration.configModel.PropertyReference
import org.sa.rainbow.configuration.configModel.Reference
import org.sa.rainbow.configuration.configModel.StringLiteral
import org.sa.rainbow.configuration.configModel.Value
import org.sa.rainbow.core.adaptation.IAdaptationExecutor
import org.sa.rainbow.core.adaptation.IAdaptationManager
import org.sa.rainbow.core.analysis.IRainbowAnalysis
import org.sa.rainbow.core.gauges.AbstractGauge
import org.sa.rainbow.core.models.commands.ModelCommandFactory
import org.sa.rainbow.translator.effectors.EffectorManager
import org.sa.rainbow.translator.probes.AbstractProbe
import org.sa.rainbow.configuration.configModel.Array
import org.sa.rainbow.configuration.validation.ConfigModelValidator

class ConfigAttributeConstants {
	public static val ALL_OFREQUIRED_PROBE_FIELDS = #{"alias", "location"};
	public static val ONE_OFREQUIRED_PROBE_FIELDS = #{"script", "java"}
	public static val ALL_OFREQUIRED_PROBE_SUBFIELDS = #{"script" -> #{"path"},
														 "java" -> #{"class"}}
	public static val OPTIONAL_PROBE_SUBFIELDS = #{"script" -> #{"mode", "argument"},
													"java" -> #{"args", "period"}}
	
	public static val ALL_OFREQUIRED_GAUGE_FIELDS = new HashSet<String> ()
	public static val ALL_OFREQUIRED_GAUGE_SUBFILEDS = #{'setup' -> #{"targetIP", "beaconPeriod", "javaClass"},
														 'config' -> #{"targetProbe", "samplingFrequency"}}
	public static val Set<String> OPTIONAL_GUAGE_FIELDS = #{}
	
	public static val  OPTIONAL_GAUGE_SUBFIELDS = Collections.<String,Set<String>>emptyMap
	
	public static val ALL_OFREQUIRED_EFFECTOR_FIELDS = #{'location'}
	public static val ONE_OFREQUIRED_EFFECTOR_FIELDS = #{'script', 'java'}
	public static val ALL_OFREQUIRED_EFFECTOR_SUBFIELDS = #{'script' -> #{'path'}, 'java' -> #{'class'}}
	public static val OPTIONAL_EFFECTOR_SUBFIELDS = #{'script' -> #{'argument'}}
	
	public static val ALL_OFREQUIRED_MODEL_FIELDS = #{'factory'}
	public static val OPTIONAL_MODEL_FIELDS = #{'name', 'path', 'saveOnClose', 'saveLocation'}
	
	public static val ALL_OFREQUIRED_MANANGER_FIELDS = #{'model', 'class'}
	public static val ALL_OFREQUIRED_EXECUTOR_FIELDS = #{'model', 'class'}
	public static val ALL_OFREQUIRED_ANALYSIS_FIELDS = #{'class'}
	public static val ALL_OFREQUIRED_EFFECTOR_MANAGER_FIELDS = #{'class'}
	
	
	public static val MODEL_KEYWORDS = {
		val set = new HashSet<String>();
		set.addAll(ALL_OFREQUIRED_MODEL_FIELDS)
		set.addAll(OPTIONAL_MODEL_FIELDS)
		set
	}
	
	public static val MANAGER_KEYWORDS = ALL_OFREQUIRED_MANANGER_FIELDS
	public static val EXECUTOR_KEYWORDS = ALL_OFREQUIRED_EXECUTOR_FIELDS
	public static val ANALYSIS_KEYWORDS = ALL_OFREQUIRED_ANALYSIS_FIELDS
	public static val EFFECTOR_MANAGER_KEYWORDS = ALL_OFREQUIRED_EFFECTOR_MANAGER_FIELDS
	
	public static val GAUGE_KEYWORDS = {
		val set = new HashSet<String>();
		set.addAll(ALL_OFREQUIRED_GAUGE_FIELDS);
		set.addAll(ALL_OFREQUIRED_GAUGE_SUBFILEDS.keySet)
		for (e : ALL_OFREQUIRED_GAUGE_SUBFILEDS.values) {
			set.addAll(e);
		}
		set
	}
	public static val PROBE_KEYWORDS = {
		val set = new HashSet<String>();
		set.addAll(ALL_OFREQUIRED_PROBE_FIELDS);
		set.addAll(ONE_OFREQUIRED_PROBE_FIELDS);
		for (e : ALL_OFREQUIRED_PROBE_SUBFIELDS.values) {
			set.addAll(e);
		}
		for (e : OPTIONAL_PROBE_SUBFIELDS.values) {
			set.addAll(e);
		}
		set
	}
	
	public static val EFFECTOR_KEYOWRDS = {
		val set = new HashSet<String>()
		set.addAll(ALL_OFREQUIRED_EFFECTOR_FIELDS)
		set.addAll(ONE_OFREQUIRED_EFFECTOR_FIELDS)
		for (e : ALL_OFREQUIRED_EFFECTOR_SUBFIELDS.values) {
			set.addAll(e)
		}
		for (e : OPTIONAL_EFFECTOR_SUBFIELDS.values) {
			set.addAll(e)
		}
		set
	}
	
	
    public static val PROPERTY_VALUE_CLASSES=#{
    	"rainbow.deployment.factory.class" -> "org.sa.rainbow.core.ports.IRainbowConnectionPortFactory",
    	"rainbow.model.load.class*" -> "org.sa.rainbow.core.models.commands.ModelCommandFactory",
    	"rainbow.analyses*" -> "org.sa.rainbow.core.analysis.IRainbowAnalysis",
    	"rainbow.adaptation.manager.class*" -> "org.sa.rainbow.core.adaptation.IAdaptationManager",
    	"rainbow.adaptation.executor.class*" -> "org.sa.rainbow.core.adaptation.IAdaptationExecutor",
    	"rainbow.effector.manager.class*" -> "org.sa.rainbow.translator.effectors.EffectorManager",
    	"rainbow.gui" -> "org.sa.rainbow.gui.IRainbowGUI"
    }
    
    
    
    static val MODEL_TYPES = 
    		#{'name' -> 
    			#{'func' -> [Value v | v.value instanceof StringLiteral], 'msg' -> 'must be a String', 'extends' -> #[StringLiteral]},
    		  'path' ->
    		    #{'func' -> [Value v | v.value instanceof StringLiteral], 'msg' -> 'must be a String', 'extends' -> #[StringLiteral]},
    		  'saveOnClose' ->
    		    #{'func' -> [Value v | v.value instanceof BooleanLiteral], 'msg' -> 'must be true or false', 'extends' -> #[BooleanLiteral]},
    		  'saveLocation' ->
    		    #{'func' -> [Value v | v.value instanceof StringLiteral], 'msg' -> 'must be a String', 'extends' -> #[StringLiteral]},
    		  'factory' ->
    		    #{'extends' -> #[ModelCommandFactory], 'msg' -> 'must subclass org.sa.rainbow.core.models.commands.ModelCommandFactory'}
    		}
    static val MANAGER_TYPES = 
    	  	#{'model' -> 
    	  		#{'func' -> [Value v | v.value instanceof StringLiteral || (v.value instanceof PropertyReference && (v.value as PropertyReference).referable.component==ComponentType.MODEL)], 'msg' -> 'must be a string or refer to a valid "def model" property', 'extends' -> #[StringLiteral,PropertyReference]},
    	      'class' -> 
    	        #{'extends' -> #[IAdaptationManager], 'msg' -> 'must subclass org.sa.rainbow.core.adaptation.IAdaptationManager'}
    	  	}
    static val EXECUTOR_TYPES = 
    		#{'model' -> 
    	  		#{'func' -> [Value v | v.value instanceof StringLiteral || (v.value instanceof PropertyReference && (v.value as PropertyReference).referable.component==ComponentType.MODEL)], 'msg' -> 'must be a string or refer to a valid "def model" property'},
    	      'class' -> 
    	        #{'extends' -> #[IAdaptationExecutor], 'msg' -> 'must subclass org.sa.rainbow.core.adaptation.IAdaptationExecutor'}
    	  	}
    public static val  COMPONENT_PROPERTY_TYPES=#{
    	ComponentType.MODEL -> MODEL_TYPES,
    	ComponentType.MANAGER -> MANAGER_TYPES,
    	ComponentType.EXECUTOR-> EXECUTOR_TYPES,
    	ComponentType.ANALYSIS->
    		#{'class' ->
    			#{'extends' -> #[IRainbowAnalysis], 'msg' -> 'must subclass org.sa.rainbow.core.analysis.IRainbowAnalysis'}
    		},
    	 ComponentType.EFFECTORMANAGER->
    		#{'class' ->
    			#{'extends' -> #[EffectorManager], 'msg' -> 'must subclass org.sa.rainbow.translator.effectors.EffectorManager'}
    		}    	   
    		
    }
    
    static val IS_STRING =  #{'extends' -> #[StringLiteral], 'msg' -> 'must be a string'}
    static val IS_NUMBER = #{'extends' -> #[IntegerLiteral], 'msg' -> 'must be a number'}
    static val IS_COMPONENT = #{'extends' -> #[Component], 'msg' -> 'must be a composite'}
    
    public static val PROBE_PROPERTY_TYPES = #{
    	'location' -> #{'extends' -> #[StringLiteral,PropertyReference,IPLiteral], 'msg' -> 'must be a string, property reference, or IP'},
    	'alias' -> IS_STRING,
    	'script' -> IS_COMPONENT,
    	'java' -> IS_COMPONENT,
    	'script:mode' -> IS_STRING,
    	'script:path' -> IS_STRING,
    	'script:argument' -> IS_STRING,
    	'java:args' -> IS_STRING,
    	'java:period' -> IS_NUMBER,
    	'java:class' -> #{'extends' -> #[AbstractProbe], 'msg' -> 'must extend AbstractProbe'}
    }
    
    public static val GAUGE_PROPERTY_TYPES = #{
    	'setup' -> IS_COMPONENT,
    	'config' -> IS_COMPONENT,
    	'setup:targetIP' -> #{'extends' -> #[StringLiteral,PropertyReference,IPLiteral], 'msg' -> 'must be a string, property reference, or IP'},
    	'setup:beaconPeriod' -> IS_NUMBER,
    	'setup:javaClass' -> #{'extends' -> #[AbstractGauge], 'msg' -> 'must extend AbstractGauge'},
    	'config:samplingFrequency' -> IS_NUMBER,
    	'config:targetProbe' -> #{'extends' -> #[ProbeReference], 'msg' -> 'must refer to a probe'}
    }
	
	
	public static val EFFECTOR_PROPERTY_TYPES = #{
		'location' -> #{'extends' -> #[StringLiteral,PropertyReference,IPLiteral], 'msg' -> 'must be a string, property reference, or IP'},
    	'script' -> IS_COMPONENT,
	  	'script:path' -> IS_STRING,
    	'script:argument' -> IS_STRING
	}
	
	
	
	public static val UTILITY_PROPERTY_TYPES =#{
		'model' -> #{'func' -> [Value v | (v.value instanceof PropertyReference && (v.value as PropertyReference).referable.component==ComponentType.MODEL)],
			         'extends' -> #[PropertyReference], 'msg' -> 'must refer to a model property'},
		'utilities' -> IS_COMPONENT,
		'scenarios' -> #{'extends' -> #[Array], 'msg' -> 'must be an array', 'checkEach' -> ConfigModelValidator.CHECK_EACH_SCENARIO},
		'utilities::label' -> IS_STRING,
		'utilities::mapping' -> IS_STRING,
		'utilities::description' -> IS_STRING,
		'utilities::utility' -> #{'extends' -> #[Array], 'msg' -> 'must be a two dimensionsal array', 'checkEach' -> ConfigModelValidator.CHECK_UTILITY_MONOTONIC}
	}
	
	public static val ALL_OFREQUIRED_UTILITY_FIELDS = #{'model', 'utilities', 'scenarios'}
	public static val REQUIRED_UTILITY_SUBFILEDS = #{'utilities:' -> #{'label', 'mapping', 'utility'}}
	public static val OPTIONAL_UTILITY_SUBFIELDS =#{'utilities:' -> #{'description'}}
	
	public static val UTILITY_KEYWORDS = {
		val set = new HashSet<String>()
		for (key : UTILITY_PROPERTY_TYPES.keySet) {
			val kw = key.split(':')
			set.add(kw.get(kw.length-1))
		}
		set
	}
	
	@Inject
	@Named("jvmtypes") static private IJvmTypeProvider.Factory jvmTypeProviderFactory;

	@Inject
	static private RawSuperTypes superTypeCollector;
	
	public static def subclasses(Value v, String superclass) {
		if (v.value instanceof Reference) {
			val ref = v.value as Reference
			val jtp = jvmTypeProviderFactory.createTypeProvider(v.eResource.resourceSet)
			val sc = jtp.findTypeByName(superclass)
			val sts = superTypeCollector.collect(ref.referable)
			return sts.contains(sc)
		}
		return false
	}

	
}