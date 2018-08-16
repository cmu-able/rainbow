package org.sa.rainbow.stitch;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestStitchApplicability extends StitchTest {

	@Test
	public void testGT() throws IOException {
		Stitch stitch = loadScript("src/test/resources/gt.s");
		
		boolean applicable = stitch.script.strategies.iterator().next().isApplicable(new HashMap<String,Object>());
		assertFalse(applicable);
	}
	
	@Test
	public void testAndFalse() throws IOException {
		Stitch stitch = loadScript("src/test/resources/andFalse.s");
		boolean applicable = stitch.script.strategies.iterator().next().isApplicable(new HashMap<String,Object>());
		assertFalse(applicable);
	}
	
	@Test
	public void testAndTrue() throws IOException {
		Stitch stitch = loadScript("src/test/resources/andTrue.s");
		boolean applicable = stitch.script.strategies.iterator().next().isApplicable(new HashMap<String,Object>());
		assertTrue(applicable);
	}
	
	@Test
	public void testCompound() throws IOException {
		Stitch stitch = loadScript("src/test/resources/TrueandGT.s");
		Strategy s1 = stitch.script.strategies.get(0);
		Strategy s2 = stitch.script.strategies.get(1);
		
		boolean applicable = s1.isApplicable(new HashMap<String,Object> ());
		assertTrue(applicable);
		applicable = s2.isApplicable(new HashMap<String,Object> ());
		assertFalse(applicable);
	}
	

}
