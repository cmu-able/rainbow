package org.sa.rainbow.testing.gauge;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.testing.implementation.BlackholeGauge;
import org.sa.rainbow.testing.prepare.RainbowMocker;
import org.sa.rainbow.testing.prepare.stub.ports.LoggerRainbowReportingPort;
import org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil;
import org.sa.rainbow.util.YamlUtil;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.sa.rainbow.testing.prepare.RainbowMocker.mockConnectionPortFactory;
import static org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil.stubPortFactoryForGauge;
import static org.sa.rainbow.testing.prepare.utils.MockingUtil.mockProbeIdentifier;
import static org.sa.rainbow.testing.prepare.utils.ResourceUtil.extractResource;

public class BlackholeGaugeTest {

    private GaugeInstanceDescription gd;

    @Before
    public void setUp() throws Exception {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory mockedPortFactory = mockConnectionPortFactory();
        stubPortFactoryForGauge(mockedPortFactory);
        RainbowMocker.injectPortFactory(mockedPortFactory);

        File yamlFile = extractResource("/blackhole/gauges.yml");
        GaugeDescription gdl = YamlUtil.loadGaugeSpecs(yamlFile);
        gd = gdl.instDescList().get(0);
    }

    @Test
    public void goodPath() throws Exception {
        RegularPatternGauge gauge = new BlackholeGauge(
                gd.gaugeName(), 10000L, new TypedAttribute(gd.gaugeName(), gd.gaugeType()),
                gd.modelDesc(), gd.setupParams(), new HashMap<>(gd.mappings())
        );
        gauge.initialize(new LoggerRainbowReportingPort());
        gauge.start();
        IRainbowOperation operation = GaugeTestingUtil.waitForNextOperation(1000);
        assertNull(operation);
        gauge.reportFromProbe(mockProbeIdentifier("mocked", "testing", "testing"), "1.1.1.1");
        operation = GaugeTestingUtil.waitForNextOperation();
        assertEquals("setBlackholed", operation.getName());
        assertEquals("1.1.1.1", operation.getParameters()[0]);
    }

}
