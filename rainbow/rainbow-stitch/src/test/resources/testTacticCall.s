module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";

tactic TFalseTactic() {
  condition {
    true;
  }
  action {
    StitchTest.markExecuted("TFalseTactic");
  }
  effect {
    true;
  }
}

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

strategy TestTrue [ true ] {
	t1: (false) -> TFalseTactic() {
	  t1a: (success) -> done;
	}
	t2: (true) -> TTrueTactic() {
	  t2a: (success) -> done;
	}
}

