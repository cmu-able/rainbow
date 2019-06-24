module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";

tactic TTrueTactic() {
  condition {
    true;
  }
  action {
    StitchTest.markExecuted("TTrueTactic");
  }
  effect @[10000]{
    StitchTest.executedValue();
  }
}

strategy TestTrue [ true ] {
	t2: (true) -> TTrueTactic() {
	  t2a: (success) -> done;
	}
}

