target rainbow-example
import properties "../properties.rbw"
import configuration "../system/probes.rbw"
import factory "../../SwimModelFactory.rbw"

def int.pattern = "[0-9]+"
def double.pattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"

gauge type LoadGaugeT = {
	model factory ««SWIM»»
	command ^load = "(?<load>«double.pattern»)" -> ServerT.setLoad(double)      
	setup = {
		targetIP = "localhost"
		beaconPeriod = 20000
		generatedClass = "org.sa.rainbow.translator.swim.gauges.LoadGauge" 
	}
	config = {
		samplingFrequency = 5000 
		targetProbe = ««GenericProbeT»»   
	}
	comment = "LoadGaugeT measures and reports CPU load for the target host"
}

gauge type DimmerGaugeT = {
	model factory ««SWIM»»
	command dimmer = "(?<dimmer>«int.pattern»)" -> LoadBalancerT.setDimmer(int)
	setup = {
		targetIP = "localhost"
		beaconPeriod = 30000
		generatedClass = "org.sa.rainbow.translator.swim.gauges.DimmerGauge"
	}
	config = {
		samplingFrequency = 1500
	}
	comment = "DimmerGaugeT measures and reports the dimmer value of the system"
}

gauge type ServerEnablementGaugeT = {
	model factory ««SWIM»»
	command serverEnabled = ServerT.enableServer(boolean)
	setup = {
		targetIP = "localhost" 
		beaconPeriod = 30000
		javaClass = org.sa.rainbow.translator.swim.gauges.ServerEnabledGauge
	}
	config = {
		samplingFrequency = 10000
		serverNum = java.lang.Integer
	}
}

gauge type ServerActivationGaugeT = {   
	model factory ««SWIM»»
	command activateServer = ServerT.activateServer(boolean)
	setup = {
		targetIP = "localhost"
		beaconPeriod = 30000
		javaClass = org.sa.rainbow.translator.swim.gauges.ServerActiveGauge
	}
	config = {
		samplingFrequency = 10000
		serverNum = java.lang.Integer
	}
	comment = "ServerActivationGaugeT reports if a server is active or not. serverNum is the server number (e.g., 1, 2 3), so that if there are N active servers, this server is active if N >= serverNum"
}

gauge type BasicResponseTimeT = {
	model factory ««SWIM»»
	command basicResponseTime = "(?<rt>«double.pattern»)" -> LoadBalancerT.setBasicResponseTime(double)
	setup = {
		targetIP = "localhost"
		beaconPeriod = 30000
		generatedClass = "org.sa.rainbow.translator.swim.gauges.BasicResponseTimeGauge"
	}
	config = {
		samplingFrequency = 10000
	}
}

gauge type OptResponseTimeT = {
	model factory ««SWIM»»
	command optResponseTime = "(?<rt>«double.pattern»)" -> LoadBalancerT.setOptResponseTime(double)
	setup = {
		targetIP = "localhost"
		beaconPeriod = 30000
		generatedClass = "org.sa.rainbow.translator.swim.gauges.OptResponseTimeGauge"
	}
	config = {
		samplingFrequency = 10000
	}
}

gauge DimmerG0 -> DimmerGaugeT = {
	model ««SwimSys»»
	command dimmer =  LB0.setDimmer($<dimmer>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««DimmerProbe»»
	}
	comment = "DimmerG0 is associated with the component LB0 of the System, SwimSys, defined as an Acme model"
}

gauge ServerEnabledG1 -> ServerEnablementGaugeT = {
	model ««SwimSys»»
	command serverEnabled = server1.enableServer($<servers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ServersProbe»»
		serverNum = 1
	}
}

gauge ServerEnabledG2 -> ServerEnablementGaugeT = {
	model ««SwimSys»»
	command serverEnabled = server2.enableServer($<servers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ServersProbe»»
		serverNum = 2
	}
}

gauge ServerEnabledG3 -> ServerEnablementGaugeT = {
	model ««SwimSys»»
	command serverEnabled = server3.enableServer($<servers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ServersProbe»»
		serverNum = 3
	}
}

gauge ServerActiveG1 -> ServerActivationGaugeT = {
	model ««SwimSys»»
	command activateServer = server1.activateServer($<activerServers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ActivateServersProbe»»
		serverNum = 1
	}
}

gauge ServerActiveG2 -> ServerActivationGaugeT = {
	model ««SwimSys»»
	command activateServer = server2.activateServer($<activerServers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ActivateServersProbe»»
		serverNum = 2
	}
}

gauge ServerActiveG3 -> ServerActivationGaugeT = {
	model ««SwimSys»»
	command activateServer = server3.activateServer($<activerServers>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
	}
	config = {
		targetProbe = ««ActivateServersProbe»»
		serverNum = 3
	}
}

gauge BasicResponseTimeG0 -> BasicResponseTimeT = {
	model ««SwimSys»»
	command basicResponseTime = LB0.setBasicResponseTime($<basicResponseTime>)
	setup = {
		targetIP = ««customize.system.^target.lb»»
		beaconPeriod = 30000
		javaClass = org.sa.rainbow.translator.swim.gauges.SimpleGauge
	}
	config = {
		targetProbe = ««BasicResponseTimeProbe»»
		samplingFrequency = 15000
	}
} 

gauge OptResponseTimeG0 -> OptResponseTimeT = {
	model ««SwimSys»»
	command optResponseTime = LB0.setOptResponseTime($<optResponseTime>) 
	setup = {
		targetIP = ««customize.system.^target.lb»»
		beaconPeriod = 30000
		javaClass = org.sa.rainbow.translator.swim.gauges.SimpleGauge 
	}
	config = { 
		targetProbe = ««OptResponseTimeProbe»»
		samplingFrequency = 15000
	}
}

gauge LoadG1 -> LoadGaugeT= {
	model SwimSys::Acme
	command ^load = server1.setLoad($<^load>)   
	setup = {
		targetIP = ««customize.system.^target.lb»»
	} 
	config = {
		targetProbe = ««LoadProbe1»»
	} 
}

gauge LoadG2 -> LoadGaugeT= {
	model SwimSys::Acme
	command ^load = server2.setLoad($<^load>)   
	setup = {
		targetIP = ««customize.system.^target.lb»»
	} 
	config = {
		targetProbe = ««LoadProbe2»»
	} 
}

gauge LoadG3 -> LoadGaugeT= {
	model SwimSys::Acme
	command ^load = server3.setLoad($<^load>)   
	setup = {
		targetIP = ««customize.system.^target.lb»»
	} 
	config = {
		targetProbe = ««LoadProbe3»»
	} 
}

export * to "model/gauges.yml"