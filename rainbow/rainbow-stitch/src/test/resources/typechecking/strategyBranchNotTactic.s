module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.*";

define int numberOfServers = Set.size({"check"});

strategy s [numberOfServers > 49] {
	t1: (true) -> numberOfServers() {
		t1a: (true) -> done;
	}
}

