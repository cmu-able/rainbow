################################################################################
# Purpose:  Common configuration file for the Rainbow infrastructure.
#           Properties are loaded by class org.sa.rainbow.Rainbow .
# Target:   
#
# Framework-defined special properties:
#     rainbow.path - the canonical path to the target configuration location
#
# History:  see non-target-specific copy
################################################################################

###
# Default values for location specific properties, meaning that, if the
# rainbow-<host>.properties file does not specify a value, the default value
# set here is used.

### Utility mechanism configuration
#- Config for Log4J, with levels:  OFF,FATAL,ERROR,WARN,INFO,DEBUG,TRACE,ALL
logging.level = [=logging_level]
event.log.path = [=event_log_path]
logging.path = ${event.log.path}/rainbow.out
monitoring.log.path = ${event.log.path}/rainbow-data.log
# (default)
#logging.pattern = "%d{ISO8601/yyyy-MM-dd HH:mm:ss.SSS} [%t] %p %c %x - %m%n"
#logging.max.size = 1024
#logging.max.backups = 5

### Rainbow component customization
## Rainbow host info and communication infrastructure
#- Location information of the master and this deployment
rainbow.master.location.host = [=rainbow_master_location_host]
#- Location information of the deployed delegate
rainbow.deployment.location = [=rainbow_deployment_location]
#- default registry port; change if port-tunneling
rainbow.master.location.port = 1100
#- OS platform, supported modes are:  cygwin | linux
#  Use "cygwin" for Windows, "linux" for MacOSX
rainbow.deployment.environment = [=rainbow_deployment_environment]
#- Event infrastructure, type of event middleware: rmi | jms | que | eseb
rainbow.event.service = [=rainbow_event_service]
eBus Relay

rainbow.delegate.beaconperiod = 10000
rainbow.deployment.factory.class = org.sa.rainbow.core.ports.eseb.ESEBRainbowPortFactory

### Rainbow models
rainbow.model.number=[=number_of_models]

# Rainbow Acme model
[#if number_of_models?number > 0]
	[#list 1..number_of_models?number as num]
		rainbow.model.path_[=num] = 
		rainbow.model.load.class_[=num] = 
		rainbow.model.name_[=num] = 
		rainbow.model.saveOnClose_[=num] = 
		rainbow.model.saveLocation_[=num] = 
	[/#list]
[/#if]

### Rainbow analyses
rainbow.analyses.size = [=number_of_analyses]
# Checks architecture for architectural errors
[#if number_of_analyses?number > 0]
	[#list 1..number_of_analyses?number as num]
		rainbow.analyses_[=num] = org.sa.rainbow.evaluator.acme.ArchEvaluator
	[/#list]
[/#if]

#Rainbow adaptation & stitch components
rainbow.adaptation.plasdp.reachPath = ${PLADAPT}/reach/reach.sh
rainbow.adaptation.plasdp.reachModel = ${PLADAPT}/reach/model/reachAddSvrDimmer
rainbow.adaptation.pla.operaConfig = model/opera/opera.config
rainbow.adaptation.plasb.prismTemplate = template.prism

rainbow.adaptation.manager.size = [=number_of_adaption_managers]
[#if number_of_adaption_managers?number > 0]
	[#list 1..number_of_adaption_managers?number as num]
		rainbow.adaptation.manager.class_[=num] = org.sa.rainbow.stitch.adaptation.AdaptationManager
		rainbow.adaptation.manager.model_[=num] = 
	[/#list]
[/#if]

rainbow.adaptation.executor.size = [=number_of_adaption_executors]
[#if number_of_adaption_executors?number > 0]
	[#list 1..number_of_adaption_executors?number as num]
		rainbow.adaptation.executor.class_[=num] = org.sa.rainbow.stitch.adaptation.StitchExecutor
		rainbow.adaptation.executor.model_[=num] = 
	[/#list]
[/#if]

rainbow.effector.manager.size = [=number_of_effector_managers]
[#if number_of_effector_managers?number > 0]
	[#list 1..number_of_effector_managers?number as num]
		rainbow.effector.manager.class_[=num] = org.sa.rainbow.effectors.acme.AcmeEffectorManager
	[/#list]
[/#if]

rainbow.gui = 
rainbow.gui.specs = 

customize.model.evaluate.period = 
customize.model.timeseriespredictor.args = 
customize.model.timeseriespredictor.traininglength = 

## Translator customization
#- Gauge spec
customize.gauges.path = model/gauges.yml
#- Probe spec
customize.probes.path = system/probes.yml
#- Operator spec as mapping to effector
customize.archop.map.path = model/op.map
#- Effector spec
customize.effectors.path = system/effectors.yml
## Adaptation Manager
#- Directory of Stitch adaptation script
customize.scripts.path = stitch
#- Utilities description file, Strategy evaluation config, and minimum score threshold
customize.utility.path = stitch/utilities.yml
customize.utility.trackStrategy = 
customize.utility.score.minimum.threshold = 
customize.utility.scenario = 
#- Whether to enable prediction, ONLY enable if system has predictor probes!
#customize.prediction.enable = false

## System configuration information
# These properties may be referred to in various files
# in the target (e.g., Acme, gauges, effectors, probes)
# and are replaced by Rainbow with the actual values
customize.system.target.master = ${rainbow.deployment.location}
customize.system.target.lb = ${rainbow.deployment.location}
customize.system.target.lb.httpPort = 