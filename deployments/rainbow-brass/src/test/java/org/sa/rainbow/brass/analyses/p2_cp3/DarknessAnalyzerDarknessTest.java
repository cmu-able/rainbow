package org.sa.rainbow.brass.analyses.p2_cp3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMapArc;

public class DarknessAnalyzerDarknessTest {

	@Test
	public void testProcessDarkDataBadToDark() {
		EnvMapArc arc = new EnvMapArc("l1", "l2", 50, true);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			arc.addSuccessRate(c, 1.0);
		}
		
		DarknessAnalyzer da = new DarknessAnalyzer();
		
		da.processDarkData(150, arc);
		
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.75, arc.getSuccessRate(c), 0.001);
		}
		
		da.processDarkData(120, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.75, arc.getSuccessRate(c), 0.001);
		}
		
		da.processDarkData(20, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.375, arc.getSuccessRate(c), 0.001);
		}
		
		da.processDarkData(255, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.375, arc.getSuccessRate(c), 0.001);
		}
	}
	
	@Test
	public void testProcessDarkDark() {
		EnvMapArc arc = new EnvMapArc("l1", "l2", 50, true);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			arc.addSuccessRate(c, 1.0);
		}
		
		DarknessAnalyzer da = new DarknessAnalyzer();
		da.processDarkData(20, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.5, arc.getSuccessRate(c), 0.001);
		}
		
		da.processDarkData(20, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.5, arc.getSuccessRate(c), 0.001);
		}
		
		da.processDarkData(180, arc);
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			assertEquals(0.5, arc.getSuccessRate(c), 0.001);
		}
	}

}
