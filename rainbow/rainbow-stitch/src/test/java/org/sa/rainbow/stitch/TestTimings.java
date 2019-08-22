package org.sa.rainbow.stitch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.stitch.core.StitchExecutionException;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestTimings extends StitchTest{
	
	@Before
	public void reset() {
		s_executionDelay = null;
		s_executed = null;
	}
	
	@Test
	public void testStrategyDurations() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/testStrategyDuration.s");
		stitch.stitchProblemHandler = new TestProblemHandler() {
			public void setProblem(org.sa.rainbow.stitch.error.IStitchProblem problem) {
				problems.add(problem);

			}
		};
		Tactic timedTactic = stitch.findTactic("TTrueTactic");
		assertNull(timedTactic.getDurationExpr());
		Strategy next = stitch.script.strategies.iterator().next();
		StrategyNode rootNode = next.getRootNode();
		for (StrategyNode n : next.gatherChildrenNodes(rootNode)) {
			assertNotEquals(n.getDuration(), 0);
		}
		Outcome o = (Outcome )next.evaluate(null);
		assertEquals( Outcome.SUCCESS, o);	
		
		reset();
		s_executionDelay = 5000L;
		o = (Outcome )next.evaluate(null);
		assertEquals( Outcome.SUCCESS, o);	
		
		reset();
		s_executionDelay = 15000L;
		o = (Outcome )next.evaluate(null);
		assertEquals( Outcome.FAILURE, o);	
		
	}
	
	@Test
	public void testTacticDurations() throws FileNotFoundException, IOException, StitchExecutionException {
		Stitch stitch = loadScript("src/test/resources/testTacticDuration.s");

		Tactic timedTactic = stitch.findTactic("TTrueTactic");
		assertNotNull(timedTactic.getDurationExpr());
		Strategy next = stitch.script.strategies.iterator().next();
		StrategyNode rootNode = next.getRootNode();
		for (StrategyNode n : next.gatherChildrenNodes(rootNode)) {
			assertEquals(n.getDuration(), 0);
		}
		s_executionDelay = 5000L;
		Outcome o = (Outcome )next.evaluate(null);
		assertEquals(Outcome.SUCCESS, o);
		
		reset();
		s_executionDelay = 15000L;
		o = (Outcome )next.evaluate(null);
		assertEquals(Outcome.FAILURE, o);
	}
	

	
}
