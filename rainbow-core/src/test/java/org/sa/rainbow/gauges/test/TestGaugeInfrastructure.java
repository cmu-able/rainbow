package org.sa.rainbow.gauges.test;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gauges.AbstractGauge;
import org.sa.rainbow.gauges.CommandRepresentation;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.util.Beacon;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;

public class TestGaugeInfrastructure extends DefaultTCase {

    class TestGauge extends AbstractGauge {

        private Long   m_reportingPeriod = 5000L;
        private Beacon m_reportingBeacon;

        public TestGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                List<TypedAttributeWithValue> setupParams, List<IRainbowModelCommandRepresentation> mappings)
                throws RainbowException {
            super ("G - TEST_GAUGE", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        }

        @Override
        protected void handleConfigParam (TypedAttributeWithValue triple) {
            super.handleConfigParam (triple);
            if (triple.getName ().equals ("REPORTING_PERIOD")) {
                if (triple.getValue () instanceof Long) {
                    m_reportingPeriod = (Long )triple.getValue ();
                }
                else {
                    m_reportingPeriod = Long.parseLong (triple.getValue ().toString ());
                }
            }
            m_reportingBeacon = new Beacon (m_reportingPeriod);
            m_reportingBeacon.mark ();
        }

        @Override
        protected void runAction () {
            super.runAction ();
            if (m_reportingBeacon.periodElapsed ()) {
                CommandRepresentation cmd = new CommandRepresentation ("test", "test", "testModel", "Acme", "load",
                        "23");
                issueCommand (cmd, Collections.<String, String> emptyMap ());
                m_reportingBeacon.mark ();
            }
        }

        @Override
        protected void initProperty (String name, Object value) {

        }
    }

    @Test
    public void testSetupGauge () throws Exception {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
        RainbowMaster rm = new RainbowMaster ();
        rm.initialize ();
        rm.start ();
        TestGauge g = new TestGauge ("testGauge", 5000, new TypedAttribute ("testGauge", "Test"), new TypedAttribute (
                "testModel", "Acme"), Collections.<TypedAttributeWithValue> emptyList (),
                Collections.<IRainbowModelCommandRepresentation> emptyList ());
        TypedAttributeWithValue twv = new TypedAttributeWithValue ("REPORTING_PERIOD", "Long", 5000);
        LinkedList<TypedAttributeWithValue> cps = new LinkedList<> ();
        cps.add (twv);
        g.configureGauge (cps);
        g.start ();

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return false;
            }

        }, 100000);

    }

}
