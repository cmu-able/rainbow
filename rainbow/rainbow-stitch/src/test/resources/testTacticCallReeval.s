module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.Set";
import acme "src/test/resources/swim.acme" { SwimSys as M, SwimFam as T};

define set id = /M.components:!T.ServerT[isArchEnabled==true];

tactic TTacticWithReEval() {
	condition {
		Set.contains(id, M.server1);
	}
	action {
		StitchTest.removeComponent(M, "server1");
	}
	effect {
		!Set.contains(id, M.server1);
	}
}

strategy TestTacticStrategy [true] {
	t1: (true) -> TTacticWithReEval() {
		t1a: (success) -> done;
	}
}



