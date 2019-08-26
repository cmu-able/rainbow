module rubis.strategies;

import op "org.sa.rainbow.stitch.RubisTest";
import op "org.sa.rainbow.stitch.StitchTest";
import op "org.sa.rainbow.stitch.lib.*";

define bool numberOfServers = Set.size({"check"});


strategy s [true] {
	t1: (true) -> done;
}
