module swim.strategies;
import op "org.sa.rainbow.stitch.lib.*"; 
import lib "swimTactics.t.s";
import model "SwimSys:Acme" { SwimSys as M, SwimFam as T};
 


define boolean HighRT = M.LB0.basicResponseTime >= M.RT_THRESHOLD;
define boolean Underloaded = M.seqAverage(/M.components:!T.ServerT[isArchEnabled==true]/...load) < 0.3;
strategy LowerResponseTime1 [HighRT] {
	t1: (HightRT) -> TAddServer() @[15000] {
		t1a: (HighRT) -> TDecDimmer() {
		  t1a1: (default) -> done;
		}
		t1b: (default) -> done;
	}
}

strategy LowerResponseTime2 [HighRT] {
	t1: (HighRT) -> TAddServer() @[15000] {
	    t1b: (HighRT) -> TAddServer() @[15000] {
	    	t1b1: (default) -> done;
	    }
		t1a: (default) -> done;
	}
}

strategy DecreaseCost [Underloaded] {
	t1: (Underloaded) -> TRemoveServer() {
		t1a: (success) -> done;
	}
/*	t1: (Underloaded) -> TRemoveServer() {
		t1a: (Underloaded) -> TIncDimmer() {
		  t1a1: (default) -> done;
		}
	}
	t2: (Underloaded) -> TIncDimmer() {
		t2a: (Underloaded) -> TRemoveServer() {
		  t2a1: (default) -> done;
		}
	}*/
}


