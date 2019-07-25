package org.sa.rainbow.stitch;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.model.util.core.UMBooleanValue;
import org.acmestudio.acme.model.util.core.UMFloatingPointValue;
import org.junit.Test;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.visitor.Stitch;

public class TestSwimStrategyApplicability extends StitchTest {

	@Test
	public void testHighRTWhenHighRT() throws FileNotFoundException, IOException, IllegalStateException, AcmeException {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeResponseTimeHigh(loadScript);

		Var highRT = (Var) loadScript.script.lookup("HighRT");
		assertNotNull(highRT);
		highRT.clearState();
		highRT.computeValue();
		assert (highRT.getValue() instanceof Boolean);
		assertTrue((Boolean) highRT.getValue());
	}

	protected void makeResponseTimeHigh(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set high response time
		IAcmeComponent LB0 = acme.getModelInstance().getComponent("LB0");
		assertNotNull(LB0);
		acme.getModelInstance().getCommandFactory()
				.propertyValueSetCommand(LB0.getProperty("basicResponseTime"), new UMFloatingPointValue(1)).execute();
	}

	@Test
	public void testNotHighRTWhenNotHighRT()
			throws FileNotFoundException, IOException, IllegalStateException, AcmeException {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeResponseTimeLow(loadScript);

		Var highRT = (Var) loadScript.script.lookup("HighRT");
		assertNotNull(highRT);
		highRT.clearState();
		highRT.computeValue();
		assertTrue(highRT.getValue() instanceof Boolean);
		assertTrue(!(Boolean) highRT.getValue());
	}

	protected void makeResponseTimeLow(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set high response time
		IAcmeComponent LB0 = acme.getModelInstance().getComponent("LB0");
		assertNotNull(LB0);
		acme.getModelInstance().getCommandFactory()
				.propertyValueSetCommand(LB0.getProperty("basicResponseTime"), new UMFloatingPointValue(0.5)).execute();
	}

	@Test
	public void testUnderloadedWhenAllUndertloaded()
			throws FileNotFoundException, IOException, IllegalStateException, AcmeException {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeUnderloaded(loadScript);

		Var underloaded = (Var) loadScript.script.lookup("Underloaded");
		assertNotNull(underloaded);
		underloaded.computeValue();
		assertTrue(underloaded.getValue() instanceof Boolean);
		assertTrue((Boolean) underloaded.getValue());
	}

	protected void makeUnderloaded(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set low load
		for (int i = 1; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory()
					.propertyValueSetCommand(serverN.getProperty("load"), new UMFloatingPointValue(0.1)).execute();
		}
	}

	@Test
	public void testUnderloadedWhenNotAllUndertloaded()
			throws FileNotFoundException, IOException, IllegalStateException, AcmeException {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set low load
		for (int i = 1; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory().propertyValueSetCommand(serverN.getProperty("load"),
					new UMFloatingPointValue((i % 2) == 0 ? 0.4 : 0.1)).execute();
		}

		Var underloaded = (Var) loadScript.script.lookup("Underloaded");
		assertNotNull(underloaded);
		underloaded.computeValue();
		assertTrue(underloaded.getValue() instanceof Boolean);
		assertTrue((Boolean) underloaded.getValue());
	}

	@Test
	public void testNotUnderloadedWhenNoneUnderloaded()
			throws FileNotFoundException, IOException, IllegalStateException, AcmeException {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeWellLoaded(loadScript);

		Var underloaded = (Var) loadScript.script.lookup("Underloaded");
		assertNotNull(underloaded);
		underloaded.computeValue();
		assertTrue(underloaded.getValue() instanceof Boolean);
		assertTrue(!(Boolean) underloaded.getValue());
	}

	protected void makeWellLoaded(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set low load
		for (int i = 1; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory()
					.propertyValueSetCommand(serverN.getProperty("load"), new UMFloatingPointValue(0.4)).execute();
		}
	}

	@Test
	public void testExtraServersWhenExtraServers() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeOnlyOneServerActive(loadScript);

		Var extraServers = (Var) loadScript.script.lookup("ExtraServers");
		assertNotNull(extraServers);
		extraServers.computeValue();
		assertTrue(extraServers.getValue() instanceof Boolean);
		assertTrue((Boolean) extraServers.getValue());
	}

	protected void makeOnlyOneServerActive(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		for (int i = 2; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory()
					.propertyValueSetCommand(serverN.getProperty("isArchEnabled"), new UMBooleanValue(false)).execute();
		}
	}

	@Test
	public void testExtraServersWhenNotExtraServers() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeAllServersActive(loadScript);
		Var extraServers = (Var) loadScript.script.lookup("ExtraServers");
		assertNotNull(extraServers);
		extraServers.computeValue();
		assertTrue(extraServers.getValue() instanceof Boolean);
		assertTrue(!(Boolean) extraServers.getValue());
	}

	protected void makeAllServersActive(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);
		// Set low load
		for (int i = 1; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory()
					.propertyValueSetCommand(serverN.getProperty("isArchEnabled"), new UMBooleanValue(true)).execute();
		}
	}
	
	@Test
	public void tesMoreThanOneAvaliableWhenNot() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		AcmeModelInstance acme = loadScript.script.models.get(0);
		for (int i = 1; i < 4; i++) {
			IAcmeComponent serverN = acme.getModelInstance().getComponent("server" + i);
			assertNotNull(serverN);
			acme.getModelInstance().getCommandFactory()
					.propertyValueSetCommand(serverN.getProperty("isArchEnabled"), new UMBooleanValue(false)).execute();
		}
		Var moreThanOneActive = (Var) loadScript.script.lookup("MoreThanOneActiveServer");
		assertNotNull(moreThanOneActive);
		moreThanOneActive.computeValue();
		assertTrue(moreThanOneActive.getValue() instanceof Boolean);
		assertTrue(!(Boolean) moreThanOneActive.getValue());
	}
	
	@Test
	public void tesMoreThanOneAvaliableWhenTrue() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeAllServersActive(loadScript);
		Var moreThanOneActive = (Var) loadScript.script.lookup("MoreThanOneActiveServer");
		assertNotNull(moreThanOneActive);
		moreThanOneActive.computeValue();
		assertTrue(moreThanOneActive.getValue() instanceof Boolean);
		assertTrue((Boolean) moreThanOneActive.getValue());
	}
	
	@Test
	public void testDimmerDecreasable() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		AcmeModelInstance acme = loadScript.script.models.get(0);
		Var dimmerDecreasable = (Var) loadScript.script.lookup("DimmerDecreasable");
		assertNotNull(dimmerDecreasable);
		dimmerDecreasable.computeValue();
		assertTrue(dimmerDecreasable.getValue() instanceof Boolean);
		assertTrue((Boolean) dimmerDecreasable.getValue());
	}
	
	@Test
	public void testDimmerNotDecreasable() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeUndimmable(loadScript);
		Var dimmerDecreasable = (Var) loadScript.script.lookup("DimmerDecreasable");
		assertNotNull(dimmerDecreasable);
		dimmerDecreasable.computeValue();
		assertTrue(dimmerDecreasable.getValue() instanceof Boolean);
		assertTrue(!(Boolean) dimmerDecreasable.getValue());
	}

	protected void makeUndimmable(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);

		// Set high response time
		IAcmeComponent LB0 = acme.getModelInstance().getComponent("LB0");
		assertNotNull(LB0);
		acme.getModelInstance().getCommandFactory()
				.propertyValueSetCommand(LB0.getProperty("dimmer"), new UMFloatingPointValue(0.1)).execute();
	}
	protected void makeDimmable(Stitch loadScript) throws AcmeException {
		AcmeModelInstance acme = loadScript.script.models.get(0);
		
		// Set high response time
		IAcmeComponent LB0 = acme.getModelInstance().getComponent("LB0");
		assertNotNull(LB0);
		acme.getModelInstance().getCommandFactory()
		.propertyValueSetCommand(LB0.getProperty("dimmer"), new UMFloatingPointValue(0.9)).execute();
	}
	
	
	@Test
	public void testReduceContentWhenOK() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeResponseTimeHigh(loadScript);
		makeOnlyOneServerActive(loadScript);
		
		Strategy ReduceContentAndAddServer = (Strategy) loadScript.script.lookup("ReduceContentAndAddServer");
		assertNotNull(ReduceContentAndAddServer);
		assertTrue(ReduceContentAndAddServer.isApplicable(new HashMap<>()));
	}
	
	@Test
	public void testReduceContentWhenNotOK() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeResponseTimeHigh(loadScript);
//		makeOnlyOneServerActive(loadScript);
		
		Strategy ReduceContentAndAddServer = (Strategy) loadScript.script.lookup("ReduceContentAndAddServer");
		assertNotNull(ReduceContentAndAddServer);
		assertTrue(!ReduceContentAndAddServer.isApplicable(new HashMap<>()));
	}
	
	@Test
	public void testRemoveServerWhenUandM() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeUnderloaded(loadScript);
//		makeOnlyOneServerActive(loadScript);
		
		Strategy RemoveServer = (Strategy) loadScript.script.lookup("RemoveServer");
		assertNotNull(RemoveServer);
		assertTrue(RemoveServer.isApplicable(new HashMap<>()));
	}
	
	@Test
	public void testRemoveServerWhenUandNotMandNotD() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeUnderloaded(loadScript);
		makeOnlyOneServerActive(loadScript);
		makeUndimmable(loadScript);
		Strategy RemoveServer = (Strategy) loadScript.script.lookup("RemoveServer");
		assertNotNull(RemoveServer);
		assertTrue(!RemoveServer.isApplicable(new HashMap<>()));
	}
	
	@Test
	public void testRemoveServerWhenUandNotMandD() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeUnderloaded(loadScript);
		makeOnlyOneServerActive(loadScript);
		makeDimmable(loadScript);
		Strategy RemoveServer = (Strategy) loadScript.script.lookup("RemoveServer");
		assertNotNull(RemoveServer);
		assertTrue(RemoveServer.isApplicable(new HashMap<>()));
	}
	
	@Test
	public void testRemoveServerWhennotU() throws Exception {
		Stitch loadScript = loadScript("src/test/resources/testStrategyConditions.s");
		makeWellLoaded(loadScript);
		makeOnlyOneServerActive(loadScript);
		
		Strategy RemoveServer = (Strategy) loadScript.script.lookup("RemoveServer");
		assertNotNull(RemoveServer);
		assertTrue(!RemoveServer.isApplicable(new HashMap<>()));
	}
	
	
}
