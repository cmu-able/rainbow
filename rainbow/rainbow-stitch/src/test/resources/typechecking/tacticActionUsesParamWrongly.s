module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.*";

define int numberOfServers = Set.size({"check"});

tactic TTrueTactic(int name) {
  condition {
    true;
  }
  action {
    StitchTest.markExecuted(name);
  }
  effect {
    true;
  }
}

strategy s [numberOfServers > 49] {
	t1: (true) -> TTrueTactic(1) {
		ta1: (true) -> done;
	}
}

