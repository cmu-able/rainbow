import acme "acme/swim.acme"

model factory SWIM yields org.sa.rainbow.^model.^acme.swim.commands.SwimCommandFactory {
	extends org.sa.rainbow.^model.^acme.AcmeModelCommandFactory
	for org.sa.rainbow.^model.^acme.AcmeModelInstance
	
	command load is org.sa.rainbow.^model.^acme.swim.commands.SwimLoadModelCommand
	command setDimmer(SwimFam.LoadBalancerT target, int dimmer) is org.sa.rainbow.^model.^acme.swim.commands.SetDimmerCmd;
	command setLoad(SwimFam.ServerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetLoadCmd;
	command setArrivalRate(SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetArrivalRateCmd;
	command setBasicResponseTime(SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetBasicResponseTimeCmd;
	command setBasicThroughput(SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetBasicThroughputCmd;
	command setOptResponseTime(SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetOptResponseTimeCmd;
	command setOptThroughput(SwimFam.LoadBalancerT target, double value) is org.sa.rainbow.^model.^acme.swim.^commands.SetOptThroughputCmd
	command enableServer(SwimFam.ServerT target, boolean enabled) is org.sa.rainbow.^model.^acme.swim.^commands.ActivateServerCmd;
	command activateServer(SwimFam.ServerT target, boolean activated) is org.sa.rainbow.^model.^acme.swim.^commands.ActivateServerCmd;
	command addServer(SwimFam.LoadBalancerT target, SwimFam.ServerT server) is org.sa.rainbow.^model.^acme.swim.^commands.AddServerCmd;
	command removeServer(SwimFam.LoadBalancerT target, SwimFam.ServerT server) is org.sa.rainbow.^model.^acme.swim.^commands.RemoveServerCmd;
	
	
	
	
}

