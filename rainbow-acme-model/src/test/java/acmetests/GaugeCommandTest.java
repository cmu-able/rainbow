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
import org.sa.rainbow.core.gauges.CommandRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.core.ports.IRainbowModelUSBusPort;
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

        IRainbowModelUSBusPort usPort = RainbowPortFactory
                .createModelsManagerClientUSPort (new Identifiable () {

                    @Override
                    public String id () {
                        return "testGaugeCommunicationSide";
                    }

                });

        final IModelInstance<IAcmeSystem> modelInstance = usPort.<IAcmeSystem> getModelInstance ("Acme", "ZNewsSys");
        assertNotNull (modelInstance);

        ModelCommandFactory<IAcmeSystem> cf = modelInstance.getCommandFactory ();
        assertNotNull (cf);

        IRainbowModelCommandRepresentation command = cf.generateCommand ("setLoad", "ZNewsSys.s0", "10.5");
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
        IRainbowModelUSBusPort usPort = RainbowPortFactory.createModelsManagerClientUSPort (new Identifiable () {

            @Override
            public String id () {
                return "testGaugeCommunicationSide";
            }

        });

        final IModelInstance<IAcmeSystem> modelInstance = usPort.<IAcmeSystem> getModelInstance ("Acme", "ZNewsSys");
        assertNotNull (modelInstance);

        ModelCommandFactory<IAcmeSystem> cf = modelInstance.getCommandFactory ();
        assertNotNull (cf);

        CommandRepresentation command = new CommandRepresentation ("", "", "", "", "", "");
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
