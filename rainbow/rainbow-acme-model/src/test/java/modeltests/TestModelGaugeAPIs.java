/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeQueryRequirerPort;
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
                List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
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
                IRainbowOperation cr = getCommand ("setLoad");
                Map<String, String> params = new HashMap<> ();
                params.put ("load", Integer.toString (m_i++));
                issueCommand (cr, params);
            }
        }


    }

    private static String s_userDir;
    private RainbowMaster m_master;
    private Map<String, IRainbowOperation>      m_commands;
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

        m_commands = new HashMap<> ();
        m_commands
        .put ("setLoad", new OperationRepresentation ("setLoad", new ModelReference ("ZNewsSys", "Acme"),
                "s0", "load"));

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

        IModelInstance<IAcmeSystem> modelInstance = m_master.modelsManager ().<IAcmeSystem> getModelInstance (
                new ModelReference ("ZNewsSys", "Acme"));
        final IAcmeComponent server = modelInstance.getModelInstance ().getComponent ("s0");
        gauge.start ();

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                float load = (float )ModelHelper.propertyValueToJava (server.getProperty ("load").getValue ());
                return load == 3.0f;
            }
        });

        ESEBGaugeQueryRequirerPort req = new ESEBGaugeQueryRequirerPort (gauge);
        Collection<IRainbowOperation> allCommands = req.queryAllCommands ();
        assertEquals (allCommands.size (), 1);
        IRainbowOperation cmd = allCommands.iterator ().next ();
        assertEquals (cmd.getName (), "setLoad");
        assertEquals (cmd.getModelReference ().getModelName (), "ZNewsSys");
        assertEquals (cmd.getModelReference ().getModelType (), "Acme");
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
