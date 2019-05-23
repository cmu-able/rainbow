module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.Set";
import op "org.sa.rainbow.stitch.lib.Sequence";
import acme "src/test/resources/swim.acme" { SwimSys as M, SwimFam as T};

define set loads = /M.components:!T.ServerT[isArchEnabled==true]/load;
define seq load_seq = /M.components:!T.ServerT[isArchEnabled==true]/...load;

tactic TTrueTactic() {
  condition {
    true;
  }
  action {
    StitchTest.markExecuted("TTrueTactic");
  }
  effect {
    true;
  }
}


strategy TestSet [ Set.size(loads) == 1 ] {
	t2: (true) -> TTrueTactic() {
	  t2a: (success) -> done;
	}
}

strategy TestSeq [ Sequence.size(load_seq) == 3] {
	t2: (true) -> TTrueTactic() {
	  t2a: (success) -> done;
	}
}

