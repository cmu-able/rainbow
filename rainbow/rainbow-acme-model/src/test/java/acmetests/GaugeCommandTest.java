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
package acmetests;

import java.io.File;
import java.io.IOException;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import auxtestlib.BooleanEvaluation;
import auxtestlib.DefaultTCase;

public class GaugeCommandTest extends DefaultTCase {

    private static String s_user_dir;
    private RainbowMaster m_master;
    @SuppressWarnings ("unused")
    @Test
    public void testGaugeCommunicationSide () throws Exception {


//        RainbowDelegate delegate = new RainbowDelegate ();
//        delegate.start ();

        IModelUSBusPort usPort = RainbowPortFactory
                .createModelsManagerClientUSPort (new Identifiable () {

                    @Override
                    public String id () {
                        return "testGaugeCommunicationSide";
                    }

                });

        final IModelInstance<IAcmeSystem> modelInstance = usPort.<IAcmeSystem> getModelInstance (new ModelReference (
                "ZNewsSys", "Acme"));
        assertNotNull (modelInstance);

        ModelCommandFactory<IAcmeSystem> cf = modelInstance.getCommandFactory ();
        assertNotNull (cf);

        IRainbowOperation command = cf.generateCommand ("setLoad", "ZNewsSys.s0", "10.5");
        assertNotNull (command);

        usPort.updateModel (command);

        wait_for_true (new BooleanEvaluation () {

            @Override
            public boolean evaluate () throws Exception {
                IAcmeProperty loadProp = modelInstance.getModelInstance ().getComponent ("s0").getProperty ("load");
                return (float )ModelHelper.propertyValueToJava (loadProp.getValue ()) == 10.5;
            }
        });

    }

    @Test
    public void testBadCommand () throws Exception {
        IModelUSBusPort usPort = RainbowPortFactory.createModelsManagerClientUSPort (new Identifiable () {

            @Override
            public String id () {
                return "testGaugeCommunicationSide";
            }

        });

        final IModelInstance<IAcmeSystem> modelInstance = usPort.<IAcmeSystem> getModelInstance (new ModelReference (
                "ZNewsSys", "Acme"));
        assertNotNull (modelInstance);

        ModelCommandFactory<IAcmeSystem> cf = modelInstance.getCommandFactory ();
        assertNotNull (cf);

        OperationRepresentation command = new OperationRepresentation ("", new ModelReference ("", ""), "", "", "");
        assertNotNull (command);

        usPort.updateModel (command);
    }

    @BeforeClass
    public static void rememberUserDir () {
        s_user_dir = System.getProperty ("user.dir");
    }

    @Before
    public void configureAndStartMaster () throws Exception {
        configureTestPath ();

        m_master = new RainbowMaster ();
        m_master.initialize ();
        m_master.start ();
    }

    @After
    public void resetAndTerminate () {
        System.setProperty ("user.dir", s_user_dir);
        m_master.terminate ();
    }

    private void configureTestPath () throws IOException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

}
