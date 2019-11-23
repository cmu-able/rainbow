module swim.strategies;
import op "org.sa.rainbow.stitch.lib.*"; 
import lib "swimTactics.t.s";
import model "SwimSys:Acme" { SwimSys as M, SwimFam as T};
 

define boolean HighRT = M.LB0.basicResponseTime >= M.RT_THRESHOLD || M.LB0.optResponseTime >= M.RT_THRESHOLD;
define boolean Underloaded = M.seqAverage(/M.components:!T.ServerT[isArchEnabled==true]/...load) < 0.3;
define boolean ExtraServers = M.size(/M.components:!T.ServerT[isArchEnabled==false])>=1;
define boolean MoreThanOneActiveServer = M.size(/M.components:!T.ServerT[isArchEnabled==true])>1;
define boolean DimmerDecreasable = SwimUtils.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS,M.DIMMER_MARGIN) > 1;



strategy LowerResponseTime1 [HighRT] {
	t1: (HighRT && ExtraServers) -> TAddServer() @[30000] {
		t1a: (HighRT) -> TDecDimmer() {
		  t1a1: (default) -> done;
		}
		t1b: (default) -> done;
	}
}

strategy LowerResponseTime2 [HighRT] {
	t1: (HighRT && ExtraServers) -> TAddServer() @[30000] {
	    t1b: (HighRT) -> TAddServer() @[30000] {
	    	t1b1: (default) -> done;
	    }
	    t1c: (HighRT) -> TDecDimmer() @[30000] {
	    	t1c1: (default) -> done;
	    }
	}
}

strategy DecreaseCost [Underloaded && (MoreThanOneActiveServer || DimmerDecreasable)] {
	t0: (Underloaded) -> TRemoveServer() @[10000]{
		t0a: (!Underloaded) -> done;
		t0b: (Underloaded && MoreThanOneActiveServer) -> TRemoveServer() {
		   t0ba: (success) -> done;
		}
		t0c: (Underloaded && DimmerDecreasable) -> TIncDimmer() {
			t0ca: (success) -> done;
		}
	}
	t1: (Underloaded) -> TRemoveServer() @[10000]{
		t1a: (Underloaded) -> TIncDimmer() {
		  t1a1: (default) -> done;
		}
		t1b: (default) -> done;
	}
	t2: (Underloaded) -> TIncDimmer() {
		t2a: (Underloaded) -> TRemoveServer() @[10000] {
		  t2a1: (default) -> done;
		}
		t2b: (default) -> done;
	}
}


