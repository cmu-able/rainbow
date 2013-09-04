package acmetests;

import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Test;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IRainbowModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelCommand;
import org.sa.rainbow.model.acme.AcmeRainbowCommandEvent.CommandEventT;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;

import auxtestlib.DefaultTCase;


public class TestNewServerCmd extends DefaultTCase {

    @Test
    public void test () throws Exception {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString (
                "src/test/resources/acme/znn.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        assertTrue (sys.getDeclaredTypes ().iterator ().next ().isSatisfied ());
        ZNNModelUpdateOperatorsImpl znn = new ZNNModelUpdateOperatorsImpl (sys);
        IAcmeComponent proxy = sys.getComponent ("lbproxy");
        AcmeModelCommand<IAcmeComponent> cns = znn.getCommandFactory ().connectNewServerCmd (proxy, "server");
        IRainbowModelChangeBusPort announcePort = new IRainbowModelChangeBusPort () {

            @Override
            public IRainbowMessage createMessage () {
                return new RainbowESEBMessage ();
            }

            @Override
            public void announce (List<? extends IRainbowMessage> event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void announce (IRainbowMessage event) {
                // TODO Auto-generated method stub

            }
        };
        List<? extends IRainbowMessage> generatedEvents = cns.execute (znn, announcePort);
        assertTrue (cns.canExecute ());
        IAcmeComponent server = cns.getResult ();
        assertTrue (cns.canUndo ());
        assertFalse (cns.canExecute ());
        assertFalse (cns.canRedo ());
        assertNotNull (server);
        assertNotNull (sys.getConnector ("proxyconn"));
        assertNotNull (sys.getAttachment (server.getPort ("http"), sys.getConnector ("proxyconn").getRole ("rec")));
        assertNotNull (sys.getAttachment (proxy.getPort ("fwd"), sys.getConnector ("proxyconn").getRole ("req")));
        outputMessages (generatedEvents);
        checkEventProperties (generatedEvents);

        cns = znn.getCommandFactory ().connectNewServerCmd (proxy, "server");

        generatedEvents = cns.execute (znn, announcePort);
        server = cns.getResult ();
        assertNotNull (server);
        assertNotNull (sys.getAttachment (proxy.getPort ("fwd4"), sys.getConnector ("proxyconn4").getRole ("req")));
        assertNotNull (sys.getAttachment (server.getPort ("http"), sys.getConnector ("proxyconn4").getRole ("rec")));
        outputMessages (generatedEvents);
        checkEventProperties (generatedEvents);
    }

    private void checkEventProperties (List<? extends IRainbowMessage> generatedEvents) {
        assertTrue (generatedEvents.size () > 0);
        assertTrue (generatedEvents.iterator ().next ().getProperty (IRainbowModelChangeBusPort.EVENT_TYPE_PROP).equals (CommandEventT.START_COMMAND.name ()));
        assertTrue (generatedEvents.get (generatedEvents.size () - 1).getProperty (IRainbowModelChangeBusPort.EVENT_TYPE_PROP).equals (CommandEventT.FINISH_COMMAND.name ()));
        for (IRainbowMessage msg : generatedEvents) {
            assertTrue (msg.getPropertyNames ().contains (ESEBConstants.MSG_TYPE_KEY));
        }
    }

    private void outputMessages (List<? extends IRainbowMessage> events) {
        for (IRainbowMessage msg : events) {
            System.out.println (msg.toString ());
        }
    }

}
