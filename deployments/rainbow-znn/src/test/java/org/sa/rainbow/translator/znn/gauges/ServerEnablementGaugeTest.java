package org.sa.rainbow.translator.znn.gauges;

import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.testing.prepare.RainbowMocker;
import org.sa.rainbow.testing.prepare.utils.MockingUtil;
import org.sa.rainbow.translator.probes.AbstractProbe;
import org.sa.rainbow.translator.probes.IProbe;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.YamlUtil;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.sa.rainbow.testing.prepare.utils.GaugeTestingUtil.stubPortFactoryForGauge;

public class ServerEnablementGaugeTest {
    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory portFactory = RainbowMocker.mockConnectionPortFactory();
        stubPortFactoryForGauge(portFactory);
        RainbowMocker.injectPortFactory(portFactory);
    }

    @Test
    public void testDoMatchDesired() throws Exception {
        GaugeDescription gdl = YamlUtil.loadGaugeSpecs(new File("src/test/resources/ServerEnablementGaugeTest/gauges.yml"));
        assertTrue(gdl.instDescList().size() == 1);
        GaugeInstanceDescription gd = gdl.instDescList().iterator().next();
        Map<String, IRainbowOperation> mappings =  new HashMap<> ();
        mappings.putAll(gd.mappings());

        ServerEnablementGauge gauge = new ServerEnablementGauge(gd.gaugeName(), 10000, new TypedAttribute(gd.gaugeName(), gd.gaugeType()), gd.modelDesc(), gd.setupParams(), mappings);
        gauge.configureGauge(gd.configParams());
        gauge.initialize(mock(IRainbowReportingPort.class));

        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/znn.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        AcmeModelInstance mi = new BareAcmeModelInstance (sys);
        Whitebox.setInternalState(gauge, "m_model", mi);

        String testInput = "o 127.0.0.1:8080";
        IProbeIdentifier testProbe = MockingUtil.mockProbeIdentifier("test", "test", "JAVA");
        gauge.start();
        gauge.reportFromProbe(testProbe, testInput);
        gauge.reportFromProbe(testProbe, testInput);
        gauge.reportFromProbe(testProbe, testInput);
        gauge.reportFromProbe(testProbe, testInput);

        //System.out.println(RainbowMocker.getPortFactoryStub().getOperation().getName());

//        runGauge(gauge);

//        assertTrue(operations != null);
//        System.out.println(operations.get(0).getName());
//        assertTrue(operations.get(0).getName().equals("enableServer"));




    }

    private class BareAcmeModelInstance extends AcmeModelInstance {
        public BareAcmeModelInstance(IAcmeSystem sys) {
            super(sys, "");
        }
        @Override
        public AcmeModelCommandFactory getCommandFactory () {
            return null;
        }

        @Override
        protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
            return new BareAcmeModelInstance (sys);
        }
    }
}