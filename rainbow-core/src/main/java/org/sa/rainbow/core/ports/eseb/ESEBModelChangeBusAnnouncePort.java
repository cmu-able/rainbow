package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

public class ESEBModelChangeBusAnnouncePort extends AbstractESEBDisposablePort implements IModelChangeBusPort {

    public ESEBModelChangeBusAnnouncePort (IModelsManager modelsManager) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.MODEL_CHANGE);
        // Runs on master

    }

    @Override
    public void announce (IRainbowMessage event) {
        if (event instanceof RainbowESEBMessage) {
            getConnectionRole().publish ((RainbowESEBMessage )event);
        }
        else
            throw new IllegalArgumentException ("Cannot pass a non ESEB Rainbow message to an ESEB port");
    }

    @Override
    public void announce (List<? extends IRainbowMessage> event) {
        for (IRainbowMessage msg : event) {
            announce (msg);
        }
    }

    @Override
    public IRainbowMessage createMessage () {
        return new RainbowESEBMessage ();
    }

}
