package acmetests;

import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Test;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelCommand;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

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
        cns.setEventAnnouncePort (new IRainbowModelChangeBusPort () {

            @Override
            public IRainbowMessage createMessage () {
                return new RainbowESEBMessage ();
            }

            @Override
            public void announce (List<IRainbowMessage> event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void announce (IRainbowMessage event) {
                // TODO Auto-generated method stub

            }
        });
        IAcmeComponent server = cns.execute (znn);
        assertNotNull (server);
        assertNotNull (sys.getConnector ("proxyconn"));
        assertNotNull (sys.getAttachment (server.getPort ("http"), sys.getConnector ("proxyconn").getRole ("rec")));
        assertNotNull (sys.getAttachment (proxy.getPort ("fwd"), sys.getConnector ("proxyconn").getRole ("req")));
        List<? extends IRainbowMessage> generatedEvents = cns.getGeneratedEvents ();
        outputMessages (generatedEvents);

        cns = znn.getCommandFactory ().connectNewServerCmd (proxy, "server");
        server = cns.execute (znn);
        assertNotNull (server);
        assertNotNull (sys.getAttachment (proxy.getPort ("fwd4"), sys.getConnector ("proxyconn4").getRole ("req")));
        assertNotNull (sys.getAttachment (server.getPort ("http"), sys.getConnector ("proxyconn4").getRole ("rec")));
        generatedEvents = cns.getGeneratedEvents ();
    }

    private void outputMessages (List<? extends IRainbowMessage> events) {
        for (IRainbowMessage msg : events) {
            System.out.println (msg.toString ());
        }
    }

}
