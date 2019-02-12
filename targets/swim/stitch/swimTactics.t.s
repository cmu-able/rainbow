module swim.tactics;

import model "SwimSys:Acme" { SwimSys as M, SwimFam as T};
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.swim.Swim";

define int numberOfServers = Set.size(select s : T.ServerT in M.components | true);

tactic TIncDimmer() {
    condition {
	SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) < M.DIMMER_LEVELS;
    }
    action {
        M.setDimmer(M.LB0, SwimUtils.dimmerLevelToFactor(SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) + 1, M.DIMMER_LEVELS, M.DIMMER_MARGIN));
    }
    effect {
    }
}

tactic TDecDimmer() {
    condition {
	SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) > 1;
    }
    action {
        M.setDimmer(M.LB0, SwimUtils.dimmerLevelToFactor(SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS, M.DIMMER_MARGIN) - 1, M.DIMMER_LEVELS, M.DIMMER_MARGIN));
    }
    effect {
    }
}

tactic TAddServer() {
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
    effect {
	false; // force it to wait for the timeout value in the strategy
    }
}

tactic TRemoveServer() {
    condition {
	numberOfServers > 1;
    }
    action {
	// remove the server with the highest index
	// note that the associated effector removes the server with the highest index
	// regardless of the one selected here
	set servers = select s : T.ServerT in M.components | true;
	object lastServer = SwimUtils.maxOverProperty("index", servers);
	M.removeServer(M.LB0, lastServer);	
    }
    effect {
    }
}

