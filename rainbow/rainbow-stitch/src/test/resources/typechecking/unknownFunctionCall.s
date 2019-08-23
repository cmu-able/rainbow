module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.*";

define int numberOfServers = Set.sizzle({"check"});

strategy s [true] {
	t1: (true) -> done;
}

