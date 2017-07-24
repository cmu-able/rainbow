package org.sa.rainbow.gauges.test;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.IRainbowRunnable.State;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBReceiverSideGaugeLifecyclePort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeConfigurationRequirerPort;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestGaugeInfrastructure extends DefaultTCase {

    class TestGauge extends AbstractGauge {

        private Long   m_reportingPeriod = 5000L;
        private Beacon m_reportingBeacon;

        public TestGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
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
                OperationRepresentation cmd = new OperationRepresentation ("test", new ModelReference (
                        "testModel", "Acme"), "load",
                        "23");
                issueCommand (cmd, Collections.<String, String> emptyMap ());
                m_reportingBeacon.mark ();
            }
        }

    }

    private static String s_currentDirectory;

    @BeforeClass
    public static void rememberUserDir () {
        s_currentDirectory = System.getProperty ("user.dir");
    }

    @After
    public void resetUserDir () throws Exception {
        m_rm.terminate ();
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_rm.state () == State.TERMINATED;
            }
        });
        m_rm = null;
        System.setProperty ("user.dir", s_currentDirectory);
    }

    @Before
    public void setUserDir () throws Exception {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
        m_rm = new RainbowMaster ();
        m_rm.initialize ();
        m_rm.start ();
    }

    List<TypedAttributeWithValue> vals = null;

    @Test
    public void testGaugeConfigurationMessage () throws Exception {
        vals = null;

        new ESEBReceiverSideGaugeLifecyclePort (new IGaugeLifecycleBusPort () {

            @Override
            public void sendBeacon (IGaugeIdentifier gauge) {
            }

            @Override
            public void reportDeleted (IGaugeIdentifier gauge) {
            }

            @Override
            public void reportCreated (IGaugeIdentifier gauge) {
                if ("__TEST".equals (gauge.id ())) {
                    m_gaugeCreated = true;
                }
            }

            @Override
            public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
            }

            @Override
            public void dispose () {
                // TODO Auto-generated method stub

            }
        });

        AbstractGauge testGauge = new AbstractGauge ("G - TEST_GAUGE", "__TEST", 5000, new TypedAttribute ("testGauge",
                "Test"), new TypedAttribute ("testModel", "Acme"), Collections.<TypedAttributeWithValue> emptyList (),
                Collections.<String, IRainbowOperation> emptyMap ()) {


            @Override
            public boolean configureGauge (List<TypedAttributeWithValue> configParams) {
                vals = configParams;
                return super.configureGauge (configParams);
            }

        };

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_gaugeCreated;
            }
        });

        ESEBGaugeConfigurationRequirerPort req = new ESEBGaugeConfigurationRequirerPort (testGauge);
        List<TypedAttributeWithValue> configParams = new LinkedList<> ();
        configParams.add (new TypedAttributeWithValue ("testString", "string", "football"));
        configParams.add (new TypedAttributeWithValue ("testShort", "short", (short )1234));
        configParams.add (new TypedAttributeWithValue ("testDouble", "double", 1234.5678));

        req.configureGauge (configParams);
        Thread.sleep (500);
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return vals != null;
            }
        }, 5000);
        assertTrue (vals != null);
        assertEquals (vals.size (), configParams.size ());
        assertEquals (vals, configParams);
    }

    boolean               m_gaugeCreated      = false;
    boolean               m_gaugeReconfigured = false;
    private RainbowMaster m_rm;

    @Test
    public void testGaugeReconfigurationMessage () throws Exception {

        m_gaugeCreated = false;
        m_gaugeReconfigured = false;

        new ESEBReceiverSideGaugeLifecyclePort (new IGaugeLifecycleBusPort () {

            @Override
            public void sendBeacon (IGaugeIdentifier gauge) {
            }

            @Override
            public void reportDeleted (IGaugeIdentifier gauge) {
            }

            @Override
            public void reportCreated (IGaugeIdentifier gauge) {
                if ("__TEST".equals (gauge.id ())) {
                    m_gaugeCreated = true;
                }
            }

            @Override
            public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
            }

            @Override
            public void dispose () {
                // TODO Auto-generated method stub

            }
        });

        AbstractGauge testGauge = new AbstractGauge ("G - TEST_GAUGE", "__TEST", 5000, new TypedAttribute ("testGauge",
                "Test"), new TypedAttribute ("testModel", "Acme"), Collections.<TypedAttributeWithValue> emptyList (),
                Collections.<String, IRainbowOperation> emptyMap ()) {


            @Override
            public boolean reconfigureGauge () {
                m_gaugeReconfigured = true;
                return super.reconfigureGauge ();
            }
        };

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_gaugeCreated;
            }
        });

        ESEBGaugeConfigurationRequirerPort req = new ESEBGaugeConfigurationRequirerPort (testGauge);
        req.reconfigureGauge ();
        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                return m_gaugeReconfigured;
            }
        }, 5000);
        testGauge.terminate ();
    }

//    @Test
//    public void testSetupGauge () throws Exception {
//
//        RainbowMaster rm = new RainbowMaster ();
//        rm.initialize ();
//        rm.start ();
//        TestGauge g = new TestGauge ("testGauge", 5000, new TypedAttribute ("testGauge", "Test"), new TypedAttribute (
//                "testModel", "Acme"), Collections.<TypedAttributeWithValue> emptyList (),
//                Collections.<IRainbowModelCommandRepresentation> emptyList ());
//        TypedAttributeWithValue twv = new TypedAttributeWithValue ("REPORTING_PERIOD", "Long", 5000);
//        LinkedList<TypedAttributeWithValue> cps = new LinkedList<> ();
//        cps.add (twv);
//        g.configureGauge (cps);
//        g.start ();
//
//        wait_for_true (new BooleanEvaluation () {
//
//            @Override
//            public boolean evaluate () throws Exception {
//                return false;
//            }
//
//        }, 100000);
//
//    }

}
