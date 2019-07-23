module swim.tactics;

import model "SwimSys:Acme" { SwimSys as M, SwimFam as T};
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.swim.Swim";

define int numberOfServers = Set.size(select s : T.ServerT in M.components | true);

tactic TIncDimmer() {
    int dimmerLevel = SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS,M.DIMMER_MARGIN);
    condition {
    	
		dimmerLevel < M.DIMMER_LEVELS;
    }
    action {
        M.setDimmer(M.LB0, SwimUtils.dimmerLevelToFactor(SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) + 1, M.DIMMER_LEVELS, M.DIMMER_MARGIN));
    }
    effect @[16000] {
    	dimmerLevel' > dimmerLevel;
    }
}

tactic TDecDimmer() {
	int dimmerLevel = SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN);
    condition {
		dimmerLevel > 1;
    }
    action {
        M.setDimmer(M.LB0, SwimUtils.dimmerLevelToFactor(SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) - 1, M.DIMMER_LEVELS, M.DIMMER_MARGIN));
    }
    effect @[16000]{
    	dimmerLevel' < dimmerLevel;
    }
}

tactic TAddServer() {
	int unusedServers = Swim.availableServices(M, T.ServerT);
    condition {
		Swim.availableServices(M, T.ServerT) > 0;
    }
    action {
	// add first server not enabled
	// note that the associated effector adds the next server in the
	// secuence, regardless of the one selected here
	set servers = Swim.findServices(M, T.ServerT);
	object newServer = SwimUtils.minOverProperty("index", servers);
	M.addServer(M.LB0, newServer);	
    }
    effect @[30000] {
		unusedServers' == unusedServers - 1;
    }
}

tactic TRemoveServer() {
	int availableServers = M.size(/M.components:!T.ServerT[isArchEnabled]);
	
    condition {
	availableServers > 1;
    }
    action {
	// remove the server with the highest index
	// note that the associated effector removes the server with the highest index
	// regardless of the one selected here
	set servers = select s : T.ServerT in M.components | true;
	object lastServer = SwimUtils.maxOverProperty("index", servers);
	M.removeServer(M.LB0, lastServer);	
    }
    effect @[30000] {
    availableServers' == availableServers - 1;
    }
}

