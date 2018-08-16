module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";

define int numberOfServers = Set.size({"check"});


tactic TAddServer() {
    condition {
        RubisTest.availableServices() > 0;
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
        numberOfServers > 1;
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
[ RubisTest.availableServices() > 0 && RubisTest.dimmer > RubisTest.DIMMER_MARGIN] {
  t1: (true) -> TDecDimmer() { 
      t1a: (true) -> TAddServer() @[180000 /*ms*/] {
        t1a1: (default) -> done;
      }
  }
}

strategy RemoveServer
[ numberOfServers > 1 ] {
  t1: (true) -> TRemoveServer() { 
      t1a: (default) -> done;
  }
}
