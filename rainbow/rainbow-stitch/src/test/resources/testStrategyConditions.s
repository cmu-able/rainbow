module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.Set";
import op "org.sa.rainbow.stitch.lib.Sequence";
import acme "src/test/resources/swim.acme" { SwimSys as M, SwimFam as T};

define boolean HighRT = M.LB0.basicResponseTime >= M.RT_THRESHOLD || M.LB0.optResponseTime >= M.RT_THRESHOLD;
define boolean Underloaded = M.seqAverage(/M.components:!T.ServerT[isArchEnabled==true]/...load) < 0.3;
define boolean ExtraServers = M.size(/M.components:!T.ServerT[isArchEnabled==false])>=1;
define boolean MoreThanOneActiveServer = M.size(/M.components:!T.ServerT[isArchEnabled==true])>1;
define boolean DimmerDecreasable = RubisTest.dimmerFactorToLevel(M.LB0.dimmer, M.DIMMER_LEVELS,M.DIMMER_MARGIN) > 1;

tactic TAddServer() {
    condition {
        ExtraServers;
    }
    action {
        // add first server not enabled
        // note that the associated effector adds the next server in the
        // secuence, regardless of the one selected here
  		StitchTest.markExecuted("TAddServer"); 
    }
    effect {
        false; // force it to wait for the timeout value in the strategy
    }
}

tactic TRemoveServer() {
    condition {
        MoreThanOneActiveServer;
    }
    action {
  		StitchTest.markExecuted("TRemoveServer");  
    }
    effect {
    }
}

tactic TDecDimmer() {
    condition {
        true;
    }
    action {
       StitchTest.markExecuted("TDecDimmer");
    }
    effect {
    }
}




strategy ReduceContentAndAddServer
[ HighRT && ExtraServers] {
  t1: (true) -> TDecDimmer() { 
      t1a: (true) -> TAddServer() @[180000 /*ms*/] {
        t1a1: (default) -> done;
      }
  }
}

strategy RemoveServer
[ Underloaded && (MoreThanOneActiveServer || DimmerDecreasable) ] {
  t1: (true) -> TRemoveServer() { 
      t1a: (default) -> done;
  }
}

