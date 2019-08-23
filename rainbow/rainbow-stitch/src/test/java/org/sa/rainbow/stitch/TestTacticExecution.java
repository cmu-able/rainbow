package org.sa.rainbow.stitch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.sa.rainbow.stitch.core.StitchExecutionException;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestTacticExecution extends StitchTest {

	@Test
	public void testTactic() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/testTacticCall.s");

		Strategy strategy = stitch.script.strategies.iterator().next();
		Outcome o = (Strategy.Outcome) strategy.evaluate(null);
		assertEquals(Outcome.SUCCESS, o);
		assertEquals("TTrueTactic", s_executed);
	}

	@Test
	public void testFunctionReevaluation() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/testTacticCallReeval.s");

		Strategy strategy = stitch.script.strategies.iterator().next();
		Outcome o = (Strategy.Outcome) strategy.evaluate(null);
		assertEquals(Outcome.SUCCESS, o);
		assertTrue(s_system.getComponent("server1") == null);
//		assertEquals("TTrueTactic", s_executed);
	}

}
