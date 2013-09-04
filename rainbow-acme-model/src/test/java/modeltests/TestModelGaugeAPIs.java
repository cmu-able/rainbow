package modeltests;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.gauges.CommandRepresentation;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.ports.eseb.ESEBGaugeQueryInterfaceRequirer;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.util.Beacon;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;

public class TestModelGaugeAPIs extends DefaultTCase {


    class SetLoadGauge extends AbstractGauge {

        private Beacon m_reportBeacon;
        private int    m_i = 0;

        public SetLoadGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                List<TypedAttributeWithValue> setupParams, List<IRainbowModelCommandRepresentation> mappings)
                        throws RainbowException {
            super ("SetLoadGauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        }

        @Override
        protected void handleConfigParam (TypedAttributeWithValue triple) {
            super.handleConfigParam (triple);
            if (triple.getName ().equals ("reportingPeriod")) {
                m_reportBeacon = new Beacon ((long )triple.getValue ());
                m_reportBeacon.mark ();
            }

        }

        @Override
        protected void runAction () {
            super.runAction ();
            if (m_reportBeacon.periodElapsed () && m_i <= 3) {
                IRainbowModelCommandRepresentation cr = this.m_commands.get ("setLoad");
                Map<String, String> params = new HashMap<> ();
                params.put ("load", Integer.toString (m_i++));
                issueCommand (cr, params);
            }
        }

        @Override
        protected void initProperty (String name, Object value) {
            // TODO Auto-generated method stub

        }

    }

    private static String s_userDir;
    private RainbowMaster m_master;
    private List<IRainbowModelCommandRepresentation> m_commands;
    private LinkedList<TypedAttributeWithValue>      m_configs;

    @BeforeClass
    public static void rememberUserDir () {
        s_userDir = System.getProperty ("user.dir");
    }

    @Before
    public void setUpMaster () throws Exception {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());

        m_master = new RainbowMaster ();
        m_master.initialize ();
        m_master.start ();

        m_commands = new LinkedList<> ();
        m_commands.add (new CommandRepresentation ("setLoad", "setLoad", "ZNewsSys", "Acme", "s0", "load"));

        m_configs = new LinkedList<> ();
        m_configs.add (new TypedAttributeWithValue ("reportingPeriod", "long", 2000L));
    }

    @After
    public void bringDownMaster () {
        m_master.terminate ();
    }

    @Test
    public void testGaugeReporting () throws Exception {

        SetLoadGauge gauge = new SetLoadGauge ("testLoadGauge", 5000, new TypedAttribute ("setLoadGauge",
                "SetLoadGaugeT"), new TypedAttribute ("ZNewsSys", "Acme"),
                Collections.<TypedAttributeWithValue> emptyList (), m_commands);

        gauge.configureGauge (m_configs);

        IModelInstance<IAcmeSystem> modelInstance = m_master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                "ZNewsSys");
        final IAcmeComponent server = modelInstance.getModelInstance ().getComponent ("s0");
        gauge.start ();

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                float load = (float )ModelHelper.propertyValueToJava (server.getProperty ("load").getValue ());
                return load == 3.0f;
            }
        });

        ESEBGaugeQueryInterfaceRequirer req = new ESEBGaugeQueryInterfaceRequirer (gauge);
        Collection<IRainbowModelCommandRepresentation> allCommands = req.queryAllCommands ();
        assertEquals (allCommands.size (), 1);
        IRainbowModelCommandRepresentation cmd = allCommands.iterator ().next ();
        assertEquals (cmd.getCommandName (), "setLoad");
        assertEquals (cmd.getLabel (), "setLoad");
        assertEquals (cmd.getModelName (), "ZNewsSys");
        assertEquals (cmd.getModelType (), "Acme");
        assertEquals (cmd.getTarget (), "s0");
        assertNotNull (cmd.getParameters ());
        assertEquals (cmd.getParameters ().length, 1);
        assertEquals (cmd.getParameters ()[0], "3");

        IGaugeState qs = gauge.queryGaugeState ();
        IGaugeState rqs = req.queryGaugeState ();
        assertEquals (allCommands, gauge.queryAllCommands ());
        assertEquals (qs.getConfigParams (), rqs.getConfigParams ());
        assertEquals (qs.getSetupParams (), rqs.getSetupParams ());
        assertEquals (qs.getGaugeReports (), rqs.getGaugeReports ());

        gauge.terminate ();


    }

}
