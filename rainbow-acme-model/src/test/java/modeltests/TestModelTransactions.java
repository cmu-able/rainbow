package modeltests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.model.acme.AcmeRainbowCommandEvent.CommandEventT;
import org.sa.rainbow.model.acme.znn.commands.NewServerCmd;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

public class TestModelTransactions {

    private RainbowMaster master;

    @Test
    public void testEventsPropagatedOk () throws Exception {
        PrimitiveScope scope = new PrimitiveScope ();
        try (BusConnection connection = new BusConnection ("localhost", (short )1234, scope);) {
            final BusDataQueue q = new BusDataQueue ();
            connection.queue_group ().add (q);
            final List<DataValue> vals = new ArrayList<> ();
            q.dispatcher ().add (new BusDataQueueListener () {

                @Override
                public void data_added_to_queue () {
                    BusData dv = null;
                    while ((dv = q.poll ()) != null) {
                        vals.add (dv.value ());
                    }

                }
            });
            connection.start ();
            IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                    "ZNewsSys");
            NewServerCmd cmd = new NewServerCmd ("newServer", modelInstance.getModelInstance (), "lbproxy", "server");
            master.modelsManager ().requestModelUpdate (cmd);
            List<? extends IRainbowMessage> generatedEvents = cmd.getGeneratedEvents ();
            // each event should have a MSG_TYPE_KEY
            Thread.sleep (500);
            for (DataValue dv : vals) {
                assertTrue (dv instanceof MapDataValue);
                MapDataValue mdv = (MapDataValue )dv;
                assertTrue (mdv.all ().containsKey (scope.string ().make (ESEBConstants.MSG_TYPE_KEY)));
            }
            // size of events on the client should be the same as the size of the events generated
            assertEquals (generatedEvents.size (), vals.size ());

            // The first and last events should be START and FINISH
            assertEquals (generatedEvents.get (0).getProperty (IRainbowModelChangeBusPort.EVENT_TYPE_PROP),
                    CommandEventT.START_COMMAND.name ());
            assertEquals (
                    generatedEvents.get (generatedEvents.size () - 1).getProperty (
                            IRainbowModelChangeBusPort.EVENT_TYPE_PROP), CommandEventT.FINISH_COMMAND.name ());
        }
    }

    @Test
    public void testTransactionOK () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                "ZNewsSys");

        List<IRainbowModelCommandRepresentation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "lbproxy", "server"));
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "lbproxy", "server"));
        master.modelsManager ().requestModelUpdate (commands, true);

        assertNotNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNotNull (modelInstance.getModelInstance ().getComponent ("server0"));
    }

    @Test
    public void testTransactionFail () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                "ZNewsSys");

        List<IRainbowModelCommandRepresentation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "lbproxy", "server"));
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "illegalproxy", "server"));
        master.modelsManager ().requestModelUpdate (commands, true);

        assertNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNull (modelInstance.getModelInstance ().getComponent ("server0"));

    }

    @Test
    public void testNonTransactionPartial () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance ("Acme",
                "ZNewsSys");

        List<IRainbowModelCommandRepresentation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "lbproxy", "server"));
        commands.add (new NewServerCmd ("newServer", modelInstance.getModelInstance (), "illegalproxy", "server"));
        master.modelsManager ().requestModelUpdate (commands, false);

        assertNotNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNull (modelInstance.getModelInstance ().getComponent ("server0"));

    }

    @Before
    public void setup () throws IOException, RainbowConnectionException, RainbowException {
        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());

        master = new RainbowMaster ();
        master.initialize ();
        master.start ();
    }

    @After
    public void shutdown () {
        if (master != null) {
            master.terminate ();
        }
        master = null;
    }

}
