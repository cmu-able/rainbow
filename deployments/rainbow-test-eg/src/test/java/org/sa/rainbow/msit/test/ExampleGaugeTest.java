package org.sa.rainbow.msit.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.translator.probes.IProbe.Kind;
import org.sa.rainbow.util.YamlUtil;

class ExampleGaugeTest {

	private class TestProbe extends AbstractProbe {

		public TestProbe(String id, String type, Kind kind) {
			super(id, type, kind);
		}

	}

	// Shouldn't have to write this file. There should be test stubs for all of this
	private class TestModelUSBus implements IModelsManager {
		org.sa.rainbow.core.ports.IModelsManagerPort modelPort;
		private IRainbowOperation m_command;

		public TestModelUSBus() throws RainbowConnectionException {
			RainbowPortFactory.createModelsManagerProviderPort(this);
		}

		@Override
		public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
			synchronized (this) {
				m_command = command;
				this.notifyAll();
			}
		}

		@Override
		public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
				throws IllegalStateException, RainbowException {
			// TODO Auto-generated method stub

		}

		@Override
		public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void registerModelType(String typeName) {
			// TODO Auto-generated method stub

		}

		@Override
		public Collection<? extends String> getRegisteredModelTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<? extends IModelInstance<?>> getModelsOfType(String modelType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void registerModel(ModelReference modelRef, IModelInstance<?> model) throws RainbowModelException {
			// TODO Auto-generated method stub

		}

		@Override
		public <T> IModelInstance<T> copyInstance(ModelReference modelRef, String copyName)
				throws RainbowModelException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void unregisterModel(IModelInstance<?> model) throws RainbowModelException {
			// TODO Auto-generated method stub

		}

		@Override
		public <T> IModelInstance<T> getModelInstanceByResource(String resource) {
			// TODO Auto-generated method stub
			return null;
		}

		public IRainbowOperation waitForOperation() throws InterruptedException {
			synchronized (this) {
				if (m_command != null)
					return m_command;
				this.wait(30000);
				return m_command;
			}
		}
	}

	@Test
	void testDoMatchDesired() throws Exception {
		String testInput = new String(Files.readAllBytes(Paths.get("src/test/resources/ExampleGaugeTest/input1.txt")), "UTF-8");
				
		// Setup probe for reporting the data
		TestProbe testProbe = new TestProbe("rostopicprobe", "test", Kind.JAVA);
		testProbe.activate();
		
		// Setup listener for gauge commands
		TestModelUSBus modelBus = new TestModelUSBus();
		
		GaugeDescription gdl = YamlUtil.loadGaugeSpecs(new File("src/test/resources/ExampleGaugeTest/gauges.yml"));
		assertTrue(gdl.instDescList().size() == 1);
		GaugeInstanceDescription gd = gdl.instDescList().iterator().next();
		
		Map<String,IRainbowOperation> mappings =  new HashMap<> ();
		mappings.putAll(gd.mappings());
		
		ExampleGauge gauge = new ExampleGauge(gd.gaugeName(), 10000, new TypedAttribute(gd.gaugeName(), gd.gaugeType()), gd.modelDesc(), gd.setupParams(), mappings);
		gauge.configureGauge(gd.configParams());
		gauge.start();
		
		testProbe.reportData(testInput);
		
		IRainbowOperation output = modelBus.waitForOperation();
		
		assertTrue(output != null);
		assertTrue(output.getName().equals(gd.mappings().get("setCurrentLocation")));
		assertTrue(output.getParameters().length == 3);
		assertTrue(Double.parseDouble(output.getParameters()[0]) == 0.000499877724796);
		assertTrue(Double.parseDouble(output.getParameters()[1]) == 0.00382659238105);
		assertTrue(Double.parseDouble(output.getParameters()[2]) == 0.00125420003);
		
	}

}
