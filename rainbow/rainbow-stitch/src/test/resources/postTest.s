module rubis.strategies;

import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.Set";
import acme "src/test/resources/swim.acme" { SwimSys as M, SwimFam as T};

define set id = /M.components:!T.ServerT[isArchEnabled==true];

tactic Tactic2000() {
    int numComps = StitchTest.availableComponents(M);
	condition {
		numComps > 1;
	}
	action {
		StitchTest.delayedRemoveComponent(M, "server1", 1500);
	}
	effect @[2000]{
		numComps' == numComps - 1;
	}
}

tactic Tactic1000() {
    int numComps = StitchTest.availableComponents(M);
	condition {
		numComps > 1;
	}
	action {
		StitchTest.delayedRemoveComponent(M, "server1", 1500);
	}
	effect @[1000]{
		numComps' == numComps - 1;
	}
}

strategy PostTimingTrue [true] {
	t1: (true) -> Tactic2000() {
		t1a: (success) -> done;
	}
}

strategy PostTimingFalse [true] {
	t1: (true) -> Tactic1000() {
		t1a: (success) -> done;
	}
}