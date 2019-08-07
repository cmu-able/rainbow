package org.sa.rainbow.util;

import java.util.Collection;

import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;

public interface IRainbowConfigurationChecker {

	void checkRainbowConfiguration();

	void setRainbowMaster(IRainbowMaster master);

	Collection<Problem> getProblems();
}
