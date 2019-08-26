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
	public void testFunctionOK() throws FileNotFoundException, IOException {
		// Typechecking does not work
		Stitch stitch = loadScript("src/test/resources/typechecking/functionOK.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().isEmpty());
	}
	
	@Test
	public void testWrongTypeInDefine() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/wrongTypeInDefine.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);

	}

	@Test
	public void testWrongTypeInFunctionCall() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/wrongTypeInFunctionCall.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);
	}
	
	@Test
	public void testUnknownFunctionCall() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/unknownFunctionCall.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 2);
	}
	
	@Test
	public void testStrategyApplicabilityOK() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyApplicabilityOK.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 0);
	}
	
	@Test
	public void testStrategyApplicabilityNotBoolean() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyApplicabilityNotBoolean.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);
	}
	
	@Test
	public void testStrategyNodeCondNotBoolean() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyBranchConditionNotBoolean.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);
	}
	
	@Test
	public void testStrategyApplicabilityUndefinedCall() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyApplicabilityUndefinedCall.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 2);
	}
	
	@Test
	public void testStrategyBranchNotTactic() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyBranchNotTactic.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 2);
	}
	
	@Test
	public void testStrategyBranchDurationNotInt() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/strategyBranchDurationNotInteger.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 2);
	}
	
	@Test
	public void testTacticParametersOK() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/tacticCallParametersOK.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 0);
	}
	
	@Test
	public void testTacticCallWrongNumParameters() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/tacticCallWrongNumParameters.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);
	}
	
	@Test
	public void testTacticCallParametersWrongType() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/tacticCallParametersWrongType.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 2);
	}
	
	@Test
	public void testTacticActionUsesParam() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/tacticActionUsesParam.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 0);
	}
	
	@Test
	public void testTacticActionUsesParamWrongly() throws FileNotFoundException, IOException {
		Stitch stitch = loadScript("src/test/resources/typechecking/tacticActionUsesParamWrongly.s", true, true);
		assertTrue(stitch.stitchProblemHandler.unreportedProblems().size() == 1);
	}
}
