package org.sa.rainbow.testing.integration;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.testing.implementation.BlackholeGauge;
import org.sa.rainbow.testing.implementation.BlackholeProbe;
import org.sa.rainbow.testing.prepare.RainbowMocker;
import org.sa.rainbow.testing.prepare.stub.ports.LoggerRainbowReportingPort;
import org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.util.YamlUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.sa.rainbow.testing.prepare.RainbowMocker.mockConnectionPortFactory;
import static org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil.stubPortFactoryForGauge;
import static org.sa.rainbow.testing.prepare.utils.MockingUtil.mockProbeIdentifier;
import static org.sa.rainbow.testing.prepare.utils.ProbeTestingUtil.stubPortFactoryForProbe;
import static org.sa.rainbow.testing.prepare.utils.ProbeTestingUtil.waitForOutput;
import static org.sa.rainbow.testing.prepare.utils.ResourceUtil.extractResource;

public class BlackholeIntegrationTest {

    private File tempInput = extractResource("/blackhole/probe-input.txt");
    private GaugeInstanceDescription gd;

    public BlackholeIntegrationTest() throws IOException {
        File yamlFile = extractResource("/blackhole/gauges.yml");
        GaugeDescription gdl = YamlUtil.loadGaugeSpecs(yamlFile);

        gd = gdl.instDescList().get(0);
    }

    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
        stubPortFactoryForGauge(mockedPortFactory);
        stubPortFactoryForProbe(mockedPortFactory);
        RainbowMocker.injectPortFactory(mockedPortFactory);
    }

    @Test
    public void goodPath() throws Exception {
        AbstractProbe probe = new BlackholeProbe("blackhole", 0L, new String[]{tempInput.toString()});
        probe.create();
        probe.activate();
        RegularPatternGauge gauge = new BlackholeGauge(
                gd.gaugeName(), 10000L, new TypedAttribute(gd.gaugeName(), gd.gaugeType()),
                gd.modelDesc(), gd.setupParams(), new HashMap<>(gd.mappings())
        );
        gauge.initialize(new LoggerRainbowReportingPort());
        gauge.start();

        String output = waitForOutput();
        gauge.reportFromProbe(mockProbeIdentifier("mocked", "testing", "testing"), output);

        IRainbowOperation operation = GaugeTestingUtil.waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());
        assertEquals("127.0.0.1", operation.getParameters()[0]);
    }
}
