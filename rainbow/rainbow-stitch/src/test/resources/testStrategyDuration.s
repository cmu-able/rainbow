module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";

tactic TTrueTactic() {
  condition {
    true;
  }
  action {
    StitchTest.markExecuted("TTrueTactic");
  }
  effect {
    StitchTest.executedValue();
  }
}

strategy TestTrue [ true ] {
	t1: (true) -> TTrueTactic() @[10000]{
	  t1a: (StitchTest.executedValue()) -> done;
	}
}

