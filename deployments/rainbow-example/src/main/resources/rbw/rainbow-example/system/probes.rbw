target rainbow-example // generates "system/probes.yml"
import properties "../properties.rbw"  
def probes.commonPath = "«rainbow.path»/system/probes"
 
probe type GenericProbeT = {  
	location = ««customize.system.^target.lb»»
	script = {
		mode = "continual" 
		path = "«probes.commonPath»/genericProbe.pl"
	}
} 

probe DimmerProbe -> GenericProbeT = {
	alias = "dimmer"
	script = {
		argument = "get_dimmer"
	}
}

probe ServersProbe -> GenericProbeT = {
	alias = "servers"
	script = {
		argument = "servers"
	}	
}

probe ActivateServersProbe -> GenericProbeT = {
	alias = "activeServers"
	script = {
		argument="get_active_servers"
	}
}

probe LoadProbe1 -> GenericProbeT = { 
	alias="load1" 
	script = {
		argument = "-d 5000 get_utilization server1"    
	}
} 

probe LoadProbe2 -> GenericProbeT = {
	alias = "load2"
	script = {
		argument = "-d 5000 get_utilization server2"
	}
}

probe LoadProbe3 -> GenericProbeT = {
	alias = "load3"
	script = {
		argument = "-d 5000 get_utilization server3"
	}
}

probe BasicResponseTimeProbe -> GenericProbeT = {
	alias = "basicResponseTime"
	script = {
		argument = "get_basic_rt"
	} 
}

probe OptResponseTimeProbe -> GenericProbeT = {
	alias = "optResponseTime"
	script = {
		argument = "get_opt_rt"
	}
}

export * to "system/probes.yml"  

