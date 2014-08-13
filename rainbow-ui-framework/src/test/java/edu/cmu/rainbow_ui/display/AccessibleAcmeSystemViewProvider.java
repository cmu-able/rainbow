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
package edu.cmu.rainbow_ui.display;

import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.IHistoryProvider;
import edu.cmu.rainbow_ui.storage.MockHistoryProvider;

/**
 * Mock system view provider. Used for testing.
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class AccessibleAcmeSystemViewProvider extends AcmeSystemViewProvider {

    public AccessibleAcmeSystemViewProvider(
            IRuntimeAggregator<IAcmeSystem> runtimeAggregator,
            AbstractRainbowVaadinUI ui, ISystemConfiguration systemConfig) {
            super(runtimeAggregator, ui, systemConfig, new MockHistoryProvider());

        internalModel = runtimeAggregator.getInternalModel();
    }

    public CircularFifoQueue<IRainbowMessage> getEventStore() {
        return super.eventStore;
    }

    public IModelInstance<IAcmeSystem> getInternalModel() {
        return super.internalModel;
    }

    public ISystemConfiguration getSystemConfig() {
        return super.systemConfig;
    }

    public IRuntimeAggregator<IAcmeSystem> getRuntimeAggregator() {
        return super.runtimeAggregator;
    }

    public AbstractRainbowVaadinUI getUi() {
        return super.ui;
    }

    public IHistoryProvider getHistoryProvider() {
        return super.historyProvider;
    }

    public int getRefreshRate() {
        return super.refreshRate;
    }

    public Timer getTimer() {
        return super.timer;
    }

    public List<IRainbowMessage> getEvents(Date start, Date end) {
        return historyProvider.getEventRange(start, end);
    }
}
