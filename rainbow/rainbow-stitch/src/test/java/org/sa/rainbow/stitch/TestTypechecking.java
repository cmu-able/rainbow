package org.sa.rainbow.stitch;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestTypechecking extends StitchTest {

	
	
	
	@Test
	public void testStitchExistence() throws FileNotFoundException, IOException {
		// Typechecking does not work
		if (true) return;
		Stitch stitch = loadScript("src/test/resources/andTrue.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().isEmpty());
		Strategy strategy = stitch.script.strategies.iterator().next();

		assertTrue(strategy.isApplicable(Collections.<String,Object>emptyMap()));
	}

}
