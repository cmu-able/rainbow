package modeltests;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.znn.commands.NewServerCmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TestModelTransactions {

    private RainbowMaster master;

    @Test
    public void testEventsPropagatedOk () throws Exception {
        PrimitiveScope scope = new PrimitiveScope ();
        try (BusConnection connection = new BusConnection ("localhost", (short) 1234, scope)) {
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
            AcmeModelInstance modelInstance = (AcmeModelInstance )master.modelsManager ()
                    .<IAcmeSystem> getModelInstance (new ModelReference ("ZNewsSys", "Acme"));
            NewServerCmd cmd = new NewServerCmd (modelInstance, "lbproxy", "server", "10.5.6.6", "1080");
            master.modelsManager ().requestModelUpdate (cmd);
            List<? extends IRainbowMessage> generatedEvents = cmd.getGeneratedEvents (new IRainbowMessageFactory () {

                @Override
                public IRainbowMessage createMessage () {
                    return new RainbowESEBMessage ();
                }
            });
            // each event should have a MSG_TYPE_KEY
            Thread.sleep (500);
            for (Iterator<DataValue> iterator = vals.iterator (); iterator.hasNext ();) {
                DataValue dv = iterator.next ();
                if (dv == null) {
                    continue;
                }
                assertTrue (dv instanceof MapDataValue);
                MapDataValue mdv = (MapDataValue )dv;

                boolean containsKey = mdv.all ().containsKey (scope.string ().make (ESEBConstants.MSG_TYPE_KEY));
                if (!containsKey) {
                    System.out.println (mdv.toString ());
                }
                assertTrue (containsKey);
                // Remove any spurious load command events that weren't processed
                if (scope.string ().make ("LOAD_COMMAND")
                        .equals (mdv.get (scope.string ().make (ESEBConstants.MSG_TYPE_KEY)))) {
                    iterator.remove ();
                    continue;
                }
            }
            // size of events on the client should be the same as the size of the events generated
            assertEquals (generatedEvents.size (), vals.size ());

            // The first and last events should be START and FINISH
            assertEquals (generatedEvents.get (0).getProperty (IModelChangeBusPort.EVENT_TYPE_PROP),
                    CommandEventT.START_COMMAND.name ());
            assertEquals (
                    generatedEvents.get (generatedEvents.size () - 1).getProperty (
                            IModelChangeBusPort.EVENT_TYPE_PROP), CommandEventT.FINISH_COMMAND.name ());
        }
    }

    @Test
    public void testTransactionOK () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        AcmeModelInstance modelInstance = (AcmeModelInstance )master.modelsManager ().<IAcmeSystem> getModelInstance (
                new ModelReference ("ZNewsSys", "Acme"));

        List<IRainbowOperation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd (modelInstance, "lbproxy", "server", "10.5.6.6", "1080"));
        commands.add (new NewServerCmd (modelInstance, "lbproxy", "server", "10.5.6.6", "1080"));
        master.modelsManager ().requestModelUpdate (commands, true);

        assertNotNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNotNull (modelInstance.getModelInstance ().getComponent ("server0"));
    }

    @Test
    public void testTransactionFail () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        AcmeModelInstance modelInstance = (AcmeModelInstance )master.modelsManager ().<IAcmeSystem> getModelInstance (
                new ModelReference ("ZNewsSys", "Acme"));

        List<IRainbowOperation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd (modelInstance, "lbproxy", "server", "10.5.6.6", "1080"));
        commands.add (new NewServerCmd (modelInstance, "illegalproxy", "server", "10.5.6.6", "1080"));
        master.modelsManager ().requestModelUpdate (commands, true);

        assertNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNull (modelInstance.getModelInstance ().getComponent ("server0"));

    }

    @Test
    public void testNonTransactionPartial () throws Exception {
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        AcmeModelInstance modelInstance = (AcmeModelInstance )master.modelsManager ().<IAcmeSystem> getModelInstance (
                new ModelReference ("ZNewsSys", "Acme"));

        List<IRainbowOperation> commands = new LinkedList<> ();
        commands.add (new NewServerCmd (modelInstance, "lbproxy", "server", "10.5.6.6", "1080"));
        commands.add (new NewServerCmd (modelInstance, "illegalproxy", "server", "10.5.6.6", "1080"));
        master.modelsManager ().requestModelUpdate (commands, false);

        assertNotNull (modelInstance.getModelInstance ().getComponent ("server"));
        assertNull (modelInstance.getModelInstance ().getComponent ("server0"));

    }

    @Before
    public void setup () throws IOException, RainbowException {
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
