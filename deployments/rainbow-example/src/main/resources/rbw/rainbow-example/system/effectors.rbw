target rainbow-example
import properties "../properties.rbw"  
def effectors.commonPath = "«rainbow.path»/system/effectors"

effector setDimmer = { 
	model ««SwimSys»»
	command ««customize.system.^target.lb»».setDimmer($<dimmer>)  
	location = ««customize.system.^target.lb»» 
	script = {
		path = "«effectors.commonPath»/setDimmer.sh" 
		argument = "{0}"
	}
}

effector addServer = {
	model ««SwimSys»»
	command ««customize.system.^target.lb»».addServer($<server>)
	location = ««customize.system.^target.lb»»
	script = {
		path = "«effectors.commonPath»/addServer.sh"
		argument = ""
	}
}

effector removeServer = {
	model ««SwimSys»»
	command  ««customize.system.^target.lb»».removeServer($<server>)
	location = ««customize.system.^target.lb»»
	script = {
		path = "«effectors.commonPath»/removeServer.sh"
		argument = ""
	}
}

export * to "system/effectors.yml"