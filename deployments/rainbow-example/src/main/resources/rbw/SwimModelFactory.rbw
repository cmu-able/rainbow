import acme "acme/swim.acme"

model factory SWIM yields org.sa.rainbow.^model.^acme.swim.commands.SwimCommandFactory {
	extends org.sa.rainbow.^model.^acme.AcmeModelCommandFactory
	for org.sa.rainbow.^model.^acme.AcmeModelInstance
	
	command load is org.sa.rainbow.^model.^acme.swim.commands.SwimLoadModelCommand
	command setDimmer(acme::SwimFam.LoadBalancerT target, int dimmer) is org.sa.rainbow.^model.^acme.swim.commands.SetDimmerCmd;
	command setLoad(acme::SwimFam.ServerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetLoadCmd;
	command setArrivalRate(acme::SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetArrivalRateCmd;
	command setBasicResponseTime(acme::SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetBasicResponseTimeCmd;
	command setBasicThroughput(acme::SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetBasicThroughputCmd;
	command setOptResponseTime(acme::SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetOptResponseTimeCmd;
	command setOptThroughput(acme::SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetOptThroughputCmd
	command enableServer(acme::SwimFam.ServerT target, boolean enabled) is org.sa.rainbow.^model.^acme.swim.^commands.ActivateServerCmd;
	command activateServer(acme::SwimFam.ServerT target, boolean activated) is org.sa.rainbow.^model.^acme.swim.^commands.ActivateServerCmd;
	command addServer(acme::SwimFam.LoadBalancerT target, acme::SwimFam.ServerT server) is org.sa.rainbow.^model.^acme.swim.^commands.AddServerCmd;
	command removeServer(acme::SwimFam.LoadBalancerT target, acme::SwimFam.ServerT server) is org.sa.rainbow.^model.^acme.swim.^commands.RemoveServerCmd;
	
	
	
	
}

