package org.sa.rainbow.stitch;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestPathExpressionApplicability extends StitchTest {

	@Test 
	public void testEqliteral() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/eqLit.s");
		boolean applicable = stitch.script.strategies.iterator().next().isApplicable(new HashMap<String,Object>());
		assertTrue(applicable);
	}

}
