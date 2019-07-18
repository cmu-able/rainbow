package org.sa.rainbow.translator.znn.gauges;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.testing.prepare.RainbowMocker;
import org.sa.rainbow.testing.prepare.utils.MockingUtil;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.YamlUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil.stubPortFactoryForGauge;
import static org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil.waitForNextOperation;
import static org.sa.rainbow.testing.prepare.utils.ProbeTestingUtil.stubPortFactoryForProbe;

public class BlackholeGaugeTest {

    private class TestProbe extends AbstractProbe {

        public TestProbe(String id, String type, Kind kind) {
            super(id, type, kind);
        }

    }

    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory portFactory = RainbowMocker.mockConnectionPortFactory();
        stubPortFactoryForGauge(portFactory);
        RainbowMocker.injectPortFactory(portFactory);
    }


    @Test
    public void testDoMatchDesired() throws Exception {

        GaugeDescription gdl = YamlUtil.loadGaugeSpecs(new File("src/test/resources/BlackholeGaugeTest/gauges.yml"));
        assertTrue(gdl.instDescList().size() == 1);
        GaugeInstanceDescription gd = gdl.instDescList().iterator().next();
        Map<String, IRainbowOperation> mappings = new HashMap<>();
        mappings.putAll(gd.mappings());

        BlackholeGauge gauge = new BlackholeGauge(gd.gaugeName(), 10000, new TypedAttribute(gd.gaugeName(), gd.gaugeType()), gd.modelDesc(), gd.setupParams(), mappings);
        gauge.initialize(mock(IRainbowReportingPort.class));

        String testInput = "1.0.0.1";
        String testInput2 = "127.0.0.1";
        String testInput3 = "192.168.0.1";
        String emptyInput = "";
        String invalidInput = "1234.123.123.23";
        gauge.start();
        IProbeIdentifier testProbe = MockingUtil.mockProbeIdentifier("testProbe", "testing", "JAVA");
        gauge.reportFromProbe(testProbe, testInput);
        gauge.reportFromProbe(testProbe, testInput2);
        gauge.reportFromProbe(testProbe, testInput3);
        gauge.reportFromProbe(testProbe, emptyInput);
        gauge.reportFromProbe(testProbe, invalidInput);

        IRainbowOperation operation = waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());
        operation = waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());
        operation = waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());

    }
}