target rainbow-example
 
################################################################################
# Purpose:  Common configuration file for the Rainbow infrastructure.
#           Properties are loaded by class org.sa.rainbow.Rainbow .
# Target:   ZNews case study system with Probes, Gauges, and Effectors implemented
#           (rainbow.target = znews1-d)
# Framework-defined special properties:
#     rainbow.path - the canonical path to the target configuration location
#
# History:  see non-target-specific copy
################################################################################

###
# Default values for location specific properties, meaning that, if the
# rainbow-<host>.properties file does not specify a value, the default value
# set here is used.
def rainbow.path # Default property defined by rainbow

### Utility mechanism configuration
#- Config for Log4J, with levels:  OFF,FATAL,ERROR,WARN,INFO,DEBUG,TRACE,ALL 
def logging.level = DEBUG
def event.log.path = "log"
def logging.path = "«event.log.path»/rainbow.out" 
def monitoring.log.path = "«event.log.path»/rainbow-data.log"
# (default)
#logging.pattern = "%d{ISO8601/yyyy-MM-dd HH:mm:ss.SSS} [%t] %p %c %x - %m%n"
#logging.max.size = 1024
#logging.max.backups = 5

### Rainbow component customization
## Rainbow host info and communication infrastructure
#- Location information of the master and this deployment
def rainbow.master.location.host = "rainbow-example"
#- Location information of the deployed delegate
def rainbow.deployment.location = "rainbow-example"
#- default registry port; change if port-tunneling
def rainbow.master.location.port = 1100
#- OS platform, supported modes are:  cygwin | linux
#  Use "cygwin" for Windows, "linux" for MacOSX
def rainbow.deployment.environment = "linux"

def rainbow.delegate.beaconperiod = 10000
def rainbow.deployment.^factory.class = org.sa.rainbow.core.ports.guava.GuavaRainbowPortFactory 

### Rainbow models

# Rainbow Acme model of SWIM
def model SwimSys= {
	^type="Acme" 
	path="model/swim.acme"
	^factory=org.sa.rainbow.^model.acme.swim.commands.SwimCommandFactory
	saveOnClose = true
	saveLocation="model/swim-post.acme"
}

# Rainbow Utility Model
def model USwimSys = {
	name="SwimSys"
	//^type="Utility"
	path="stitch/utilities.yml" 
	^factory=org.sa.rainbow.^model.^utility.UtilityCommandFactory    
}

### Rainbow analyses
def analysis ArchEvaluator = {
	class = org.sa.rainbow.evaluator.acme.ArchEvaluator
}


def adaptation-manager AdaptationManager = {
	^model=««SwimSys»»
	class=org.sa.rainbow.^stitch.adaptation.AdaptationManager
}

def executor StitchExecutor = {
	^model=««SwimSys»»
	class=org.sa.rainbow.^stitch.adaptation.StitchExecutor
}

def effector-manager AcmeEffectorManager = {
	class = org.sa.rainbow.effectors.acme.AcmeEffectorManager
}


def rainbow.^gui=org.sa.rainbow.^gui.RainbowWindoe
def rainbow.^gui.specs = "ui.yml"

def customize.^model.evaluate.period = 60000
def customize.^model.timeseriespredictor.args="LES 0.8 0.15"
def customize.^model.timeseriespredictor.traininglength=15

## Translator customization
#- Gauge spec
def customize.gauges.path = "model/gauges.yml"
#- Probe spec
def customize.probes.path = "system/probes.yml"
#- Operator spec as mapping to effector
def customize.archop.map.path = "model/op.map"
#- Effector spec
def customize.effectors.path = "system/effectors.yml"
## Adaptation Manager
#- Directory of Stitch adaptation script
def customize.scripts.path = "stitch"
#- Utilities description file, Strategy evaluation config, and minimum score threshold
def customize.^utility.path = "stitch/utilities.yml"
def customize.^utility.trackStrategy = "uC"
def customize.^utility.score.minimum.threshold = 0.033
def customize.^utility.scenario = "scenario 1"
#- Whether to enable prediction, ONLY enable if system has predictor probes! 
#customize.prediction.enable = false

## System configuration information 
# These properties may be referred to in various files
# in the target (e.g., Acme, gauges, effectors, probes)
# and are replaced by Rainbow with the actual values.
# They may define deployment information (e.g., in terms of IPs and ports)
# among other things
def customize.system.^target.master = ««rainbow.deployment.location»»
def customize.system.^target.lb = ««rainbow.deployment.location»»

export * to "rainbow-gui.properties" 
