package org.sa.rainbow.stitch;

public class RubisTest {
	public static int availableServices() {
		return 1;
	}
	
	public static int dimmerFactorToLevel(double dimmer, int dimmerLevels, double dimmerMargin) {
		int level = 1 + (int) Math.round((dimmer - dimmerMargin) * (dimmerLevels - 1) / (1.0 - 2 * dimmerMargin));
		return level;
	}

	public static double dimmerLevelToFactor(int level, int dimmerLevels, double dimmerMargin) {
		double factor = dimmerMargin + (1.0 - 2 * dimmerMargin) * (level - 1.0) / (dimmerLevels - 1.0);
		return factor;
	}
	
	public static int dimmer = 2;
	public static int DIMMER_MARGIN = 1;
}
