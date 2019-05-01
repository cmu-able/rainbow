package org.sa.rainbow.stitch.adaptation;

import java.util.List;

import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.visitor.Stitch;

public class StitchStrategyAccessor {
	public static Strategy retrieveStrategy(AdaptationManager m, String name) {
		List<Stitch> stitches = m._retrieveRepertoireForTesting();
		for (Stitch stitch : stitches) {
			List<Strategy> strategies = stitch.script.strategies;
			for (Strategy s : strategies) {
				if (s.getName().equals(name))
					return s;
			}
		}
		return null;
	}
}
