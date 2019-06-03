package org.sa.rainbow.stitch;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.sa.rainbow.stitch.core.StitchExecutionException;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestTacticPostCondition extends StitchTest {

	@Test
	public void testPostCondition() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/postTest.s");
		Strategy strategy = stitch.script.strategies.iterator().next();
		Outcome o = (Strategy.Outcome) strategy.evaluate(null);
		assertEquals(Outcome.SUCCESS, o);
	}
	
	@Test
	public void testFailedPostCondition() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/postTest.s");
		Strategy strategy = stitch.script.strategies.get(1);
		Outcome o = (Strategy.Outcome) strategy.evaluate(null);
		assertEquals(Outcome.FAILURE, o);
	}

}
