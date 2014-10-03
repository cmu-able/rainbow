/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
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
package edu.cmu.rainbow_ui.ingestion;

import java.io.IOException;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.eseb.ESEBConnector;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBModelsManagerRequirerPort;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.rainbow_ui.common.ISystemConfiguration;

/**
 * ESEB Rainbow Connector class for Rainbow UI Framework.
 *
 * <p>
 * Performs connection to Rainbow event bus and attachment of event handlers. The event listener
 * attaches to the following ESEB busses:
 * <ul>
 * <li>Model change bus</li>
 * </ul>
 *
 * The connector also performs obtaining the model from the Rainbow system.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class ESEBRainbowConnector implements IRainbowConnector {
    /* Rainbow host and port */

    private final String rainbowHost;
    private final short rainbowPort;

    /* Runtime Aggregator */
    private final IRuntimeAggregator runtimeAggregator;

    /* Port to connect to the remote model manager */
    private ESEBModelsManagerRequirerPort modelManagerPort;

    /* Port and callback to connect to the model change bus */
    private ESEBRainbowSubscriberPort modelChangePort;
    private final IRainbowListenerCallback modelChangeCallback;

    /* Port and callback to connect to the model change bus */
    private ESEBRainbowSubscriberPort modelUpstreamPort;
    private final IRainbowListenerCallback modelDownstreamCallback;

    /* Port and callback to connect to the model change bus */
    private ESEBRainbowSubscriberPort modelDownstreamPort;
    private final IRainbowListenerCallback modelUpstreamCallback;

    /**
     * EventListener constructor.
     *
     * @param config - system configuration
     * @param ra - runtime aggregator to be used as event handlers
     */
    ESEBRainbowConnector(ISystemConfiguration config, IRuntimeAggregator ra) {
        rainbowHost = config.getRainbowHost();
        rainbowPort = config.getRainbowPort();
        runtimeAggregator = ra;

        /* Create callbacks */
        modelChangeCallback
        = new GenericRuntimeAggregatorCallback(runtimeAggregator, ESEBConnector.ChannelT.MODEL_CHANGE);

        modelDownstreamCallback
        = new GenericRuntimeAggregatorCallback(runtimeAggregator, ESEBConnector.ChannelT.MODEL_DS);

        modelUpstreamCallback
        = new GenericRuntimeAggregatorCallback(runtimeAggregator, ESEBConnector.ChannelT.MODEL_US);
    }

    @Override
    public void attachEventListeners() throws IOException {
        attachToModelChange();
        attachToModelUpstream();
        attachToModelDownstream();
    }

    /**
     * Attach model change listeners
     */
    private void attachToModelChange() throws IOException {
        if (modelChangePort == null) {
            modelChangePort = new ESEBRainbowSubscriberPort(
                    rainbowHost, rainbowPort, ESEBConnector.ChannelT.MODEL_CHANGE);
        }
        modelChangePort.subscribe(modelChangeCallback);
    }

    /**
     * Attach upstream model event listeners
     */
    private void attachToModelUpstream() throws IOException {
        if (modelUpstreamPort == null) {
            modelUpstreamPort = new ESEBRainbowSubscriberPort(
                    rainbowHost, rainbowPort, ESEBConnector.ChannelT.MODEL_DS);
        }
        modelUpstreamPort.subscribe(modelUpstreamCallback);
    }

    /**
     * Attach downstream model events listeners
     */
    private void attachToModelDownstream() throws IOException {
        if (modelDownstreamPort == null) {
            modelDownstreamPort = new ESEBRainbowSubscriberPort(
                    rainbowHost, rainbowPort, ESEBConnector.ChannelT.MODEL_DS);
        }

        modelDownstreamPort.subscribe(modelDownstreamCallback);
    }

    @Override
    public void detachEventListeners() {
        modelChangePort.unsubscribe(modelChangeCallback);
        modelDownstreamPort.unsubscribe(modelDownstreamCallback);
        modelUpstreamPort.unsubscribe(modelUpstreamCallback);
    }

    @Override
    public IModelInstance<?> getRemoteModel(String modelName)
            throws IOException, ParticipantException {
        if (modelChangePort == null) {
            modelManagerPort = new ESEBModelsManagerRequirerPort(rainbowHost,
                    rainbowPort);
        }
        return modelManagerPort.getModelInstance (new ModelReference (modelName, "Acme"));
    }

}
