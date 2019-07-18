package org.sa.rainbow.evaluator.znn;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;
import org.sa.rainbow.testing.prepare.RainbowMocker;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZNNStateAnalyzerTest {
    List<? extends IRainbowMessage> generatedEvents;

    @Before
    public void setUp() throws Exception {
        RainbowMocker.injectRainbow();
        IRainbowConnectionPortFactory portFactory = RainbowMocker.mockConnectionPortFactory();
        RainbowMocker.injectPortFactory(portFactory);
        when(portFactory.createModelChangeBusSubscriptionPort()).thenReturn(mock(IModelChangeBusSubscriberPort.class));
        ZNNStateAnalyzer analyzer = new ZNNStateAnalyzer();
        when(portFactory.createModeslManagerRequirerPort ()).thenReturn(mock(IModelsManagerPort.class));
        when(portFactory.createModelsManagerClientUSPort(analyzer)).thenReturn(mock(IModelUSBusPort.class));
    }

    @Test
    public void runAction() throws Exception {
        StandaloneResource resource = StandaloneResourceProvider.instance ().acmeResourceForString("src/test/resources/acme/znn.acme");
        IAcmeSystem sys = resource.getModel ().getSystems ().iterator ().next ();
        ZNNModelUpdateOperatorsImpl model = new ZNNModelUpdateOperatorsImpl(sys, "src/test/resources/acme/znn.acme");
        LinkedBlockingQueue<ZNNModelUpdateOperatorsImpl> queue = new LinkedBlockingQueue<> ();
        queue.offer(model);
        ZNNStateAnalyzer analyzer = new ZNNStateAnalyzer();
        Whitebox.setInternalState(analyzer, "m_modelCheckQ", queue);
        ZNNModelUpdateOperatorsImpl znn = new ZNNModelUpdateOperatorsImpl (sys, "src/test/resources/acme/znn.acme");
        IAcmeComponent proxy = sys.getComponent ("lbproxy");
        AcmeModelOperation<IAcmeComponent> cns = znn.getCommandFactory ().connectNewServerCmd (proxy, "server",
                "10.5.6.6", "1080");
        IModelChangeBusPort announcePort = new IModelChangeBusPort () {

            @Override
            public IRainbowMessage createMessage () {
                return new RainbowESEBMessage();
            }

            @Override
            public void announce (List<? extends IRainbowMessage> event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void announce (IRainbowMessage event) {
                // TODO Auto-generated method stub

            }

            @Override
            public void dispose () {
                // TODO Auto-generated method stub

            }
        };
        assertTrue (cns.canExecute ());
        generatedEvents = cns.execute (znn, announcePort);
        ModelReference mr = new ModelReference("ZNewsSys", "Acme");
        IRainbowReportingPort port = new IRainbowReportingPort() {
            String msg;

            @Override
            public void fatal(RainbowComponentT type, String msg, Throwable e, Logger logger) {

            }

            @Override
            public void fatal(RainbowComponentT type, String msg, Logger logger) {

            }

            @Override
            public void fatal(RainbowComponentT type, String msg, Throwable e) {

            }

            @Override
            public void fatal(RainbowComponentT type, String msg) {

            }

            @Override
            public void error(RainbowComponentT type, String msg, Throwable e, Logger logger) {
                this.msg = msg;
            }

            @Override
            public void error(RainbowComponentT type, String msg, Logger logger) {

            }

            @Override
            public void error(RainbowComponentT type, String msg, Throwable e) {
                this.msg = msg;
            }

            @Override
            public void error(RainbowComponentT type, String msg) {

            }

            @Override
            public void warn(RainbowComponentT type, String msg, Throwable e, Logger logger) {

            }

            @Override
            public void warn(RainbowComponentT type, String msg, Logger logger) {

            }

            @Override
            public void warn(RainbowComponentT type, String msg, Throwable e) {

            }

            @Override
            public void warn(RainbowComponentT type, String msg) {

            }

            @Override
            public void info(RainbowComponentT type, String msg, Logger logger) {

            }

            @Override
            public void info(RainbowComponentT type, String msg) {

            }

            @Override
            public void trace(RainbowComponentT type, String msg) {

            }

            @Override
            public void dispose() {

            }
        };
        analyzer.initialize (port);
        for (IRainbowMessage msg : generatedEvents) {
            analyzer.onEvent(mr, msg);
        }
        analyzer.runAction();
        String msg = Whitebox.getInternalState(port, "msg");
        assertTrue(msg.equals("Failed to evaluate an expression"));

    }
}