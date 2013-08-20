package acmetests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.Test;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.management.ports.RainbowPortFactory;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.commands.ModelCommandFactory;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;

public class GaugeCommandTest extends TestCase {

    @SuppressWarnings ("unused")
    @Test
    public void testGaugeCommunicationSide () throws Exception {
        configureTestPath ();

        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        master.start ();

        RainbowDelegate delegate = new RainbowDelegate ();
        delegate.start ();

        IRainbowModelUSBusPort usPort = RainbowPortFactory
                .createModelsManagerClientUSPort (new Identifiable () {

                    @Override
                    public String id () {
                        return "testGaugeCommunicationSide";
                    }

                });

        IModelInstance<IAcmeSystem> modelInstance = usPort.<IAcmeSystem> getModelInstance ("Acme", "ZNewsSys");
        assertNotNull (modelInstance);

        ModelCommandFactory<IAcmeSystem> cf = modelInstance.getCommandFactory ();
        assertNotNull (cf);

        IRainbowModelCommandRepresentation command = cf.generateCommand ("setLoad", "ZNewsSys.Server0", "10.5");
        assertNotNull (command);

        usPort.updateModel (command);

        Thread.sleep (250);

        // Add in checks to check the model

    }


    private void configureTestPath () throws IOException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowModelsTest");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
    }

}
