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
package org.sa.rainbow.core.ports.eseb;


import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

import java.io.IOException;
import java.util.List;

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
