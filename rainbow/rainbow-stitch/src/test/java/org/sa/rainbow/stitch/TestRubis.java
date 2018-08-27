package org.sa.rainbow.stitch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestRubis extends StitchTest {

	@Test
	public void test() throws FileNotFoundException, IOException {
		Stitch loadScript = loadScript("src/test/resources/rubisStrategies.s");
		
		Strategy rcaas = loadScript.script.strategies.get(0);
		Strategy rs = loadScript.script.strategies.get(1);
		Map<String,Object> moreVars = new HashMap<> ();
		boolean applicable = rcaas.isApplicable(moreVars);
		assertTrue(applicable);
		
		applicable = rs.isApplicable(moreVars);
		assertFalse(applicable);
		
	}

}
