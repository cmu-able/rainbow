package acmetests;

import auxtestlib.DefaultTCase;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.model.acme.AcmeModelOperation;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;
import org.sa.rainbow.testing.prepare.RainbowMocker;

import java.util.List;

import static org.sa.rainbow.testing.prepare.utils.EffectorTestingUtil.mockAnnouncePort;


public class TestSetFidelityCmd extends DefaultTCase {

    @Before
    public void setUp() {
        RainbowMocker.injectRainbow();
    }

    @Test
    public void test() throws Exception {

        // Construct SetFidelityCmd from CommandFactory
        StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(
                "src/test/resources/acme/znn.acme");
        IAcmeSystem sys = resource.getModel().getSystems().iterator().next();
        assertTrue(sys.getDeclaredTypes().iterator().next().isSatisfied());
        ZNNModelUpdateOperatorsImpl znn = new ZNNModelUpdateOperatorsImpl(sys, "src/test/resources/acme/znn.acme");
        IAcmeComponent server = sys.getComponent("s0");
        AcmeModelOperation cns = znn.getCommandFactory().setFidelityCmd(server, 3);

        // Execute SetLoadCmd
        IModelChangeBusPort announcePort = mockAnnouncePort();
        assertTrue(cns.canExecute());
        List<? extends IRainbowMessage> generatedEvents = cns.execute(znn, announcePort);

        // assert and print its results
        assertTrue(cns.canUndo());
        assertFalse(cns.canExecute());
        assertFalse(cns.canRedo());
        outputMessages(generatedEvents);
        checkEventProperties(generatedEvents);
    }

    private void checkEventProperties(List<? extends IRainbowMessage> generatedEvents) {
        assertTrue(generatedEvents.size() > 0);
        assertTrue(generatedEvents.iterator().next().getProperty(IModelChangeBusPort.EVENT_TYPE_PROP).equals(CommandEventT.START_COMMAND.name()));
        assertTrue(generatedEvents.get(generatedEvents.size() - 1).getProperty(IModelChangeBusPort.EVENT_TYPE_PROP).equals(CommandEventT.FINISH_COMMAND.name()));
        for (IRainbowMessage msg : generatedEvents) {
            assertTrue(msg.getPropertyNames().contains(ESEBConstants.MSG_TYPE_KEY));
        }
    }

    private void outputMessages(List<? extends IRainbowMessage> events) {
        for (IRainbowMessage msg : events) {
            System.out.println(msg.toString());
        }
    }

}