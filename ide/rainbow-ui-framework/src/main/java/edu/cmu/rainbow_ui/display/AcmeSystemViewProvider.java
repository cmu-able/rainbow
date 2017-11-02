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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.HistoryProvider;
import edu.cmu.rainbow_ui.storage.IHistoryProvider;

/**
 * Provides access to the model of the system under control at the current time
 * or a historical point to the display
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class AcmeSystemViewProvider implements ISystemViewProvider {

    protected IModelInstance<IAcmeSystem> internalModel;
    protected final ISystemConfiguration systemConfig;
    protected final IRuntimeAggregator<IAcmeSystem> runtimeAggregator;
    protected final AbstractRainbowVaadinUI ui;
    protected final IHistoryProvider historyProvider;
    protected boolean isCurrent;
    protected final int refreshRate;
    protected Timer timer;
    protected TimerTask currentTask;
    protected TimerTask historicalTask;
    protected boolean currentIsWrite;
    private Date historicalTime;

    protected int eventStoreQueueHead;
    protected int newEvents;
    protected List<IRainbowMessage> historicalEvents;

    /**
     * The event store is based on a circular fifo queue of the fixed size. New
     * coming event will replace the oldest if the queue is full.
     */
    protected final CircularFifoQueue<IRainbowMessage> eventStore;
    private final static int EVENT_STORE_SIZE = 500;

    protected AcmeSystemViewProvider(
            IRuntimeAggregator<IAcmeSystem> runtimeAggregator,
            AbstractRainbowVaadinUI ui, ISystemConfiguration systemConfig,
            IHistoryProvider provider) {
        this.runtimeAggregator = runtimeAggregator;
        internalModel = runtimeAggregator.getInternalModel();

        eventStore = new CircularFifoQueue<>(EVENT_STORE_SIZE);
        historicalEvents = new ArrayList<>();

        this.ui = ui;
        this.systemConfig = systemConfig;
        historyProvider = provider;
        isCurrent = true;
        historicalTime = null;

        timer = new Timer();

        refreshRate = this.systemConfig.getUpdateRate();
        newEvents = 0;
        currentIsWrite = true;
    }

    /**
     * Create a new AcmeSystemViewProvider and start it using the current view
     * 
     * @param runtimeAggregator the system's runtime aggregator
     * @param ui the system's ui
     * @param systemConfig system configuration
     */
    public AcmeSystemViewProvider(
            IRuntimeAggregator<IAcmeSystem> runtimeAggregator,
            AbstractRainbowVaadinUI ui, ISystemConfiguration systemConfig) {
        this.runtimeAggregator = runtimeAggregator;
        internalModel = runtimeAggregator.getInternalModel();

        historicalEvents = new ArrayList<>();
        eventStore = new CircularFifoQueue<>(EVENT_STORE_SIZE);
        eventStoreQueueHead = 0;

        this.ui = ui;
        this.systemConfig = systemConfig;
        historyProvider = new HistoryProvider(this, this.systemConfig);
        isCurrent = true;

        timer = new Timer();

        refreshRate = this.systemConfig.getUpdateRate();
        newEvents = 0;
        currentIsWrite = true;
    }

    /**
     * {@inheritDoc}
     * 
     * Empty the event store and start the scheduled update task
     */
    @Override
    public void setUseCurrent() {
        if (historicalTask != null) {
            historicalTask.cancel();
        }
        if (currentTask != null) {
            currentTask.cancel();
        }
        isCurrent = true;
        historicalTime = null;
        eventStore.clear();
        runtimeAggregator.getEventBuffer().clear();
        runtimeAggregator.getEventBuffer().activate();
        // Schedule immediately, reschedule every rate
        currentTask = new TimerTask() {
            @Override
            public void run() {
                internalModel = runtimeAggregator.getInternalModel();
                update();
            }
        };
        timer = new Timer();
        timer.schedule(currentTask, 1, refreshRate);

        Logger.getLogger(AcmeSystemViewProvider.class.getName()).info(
                "SVP set to use current data.");
    }

    /**
     * {@inheritDoc}
     * 
     * Empty the event store and load historical events. Also cancel the update
     * task
     */
    @Override
    public void setUseHistorical(Date time) throws SystemViewProviderException {
        if (historicalTask != null) {
            historicalTask.cancel();
        }
        if (currentTask != null) {
            currentTask.cancel();
        }
        final IModelInstance<IAcmeSystem> historicalModel = (IModelInstance<IAcmeSystem>) historyProvider
                .getModelState(time);
        if (historicalModel == null) {
            setUseCurrent();
            throw new SystemViewProviderException(
                    "No data available for the specified time point: "
                            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                    .format(time));
        }

        historicalEvents = historyProvider.getNumberOfEventsBefore(time,
                eventStore.maxSize());

        isCurrent = false;
        historicalTime = time;
        runtimeAggregator.getEventBuffer().deactivate();
        internalModel = historicalModel;
        historicalTask = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };
        timer = new Timer();
        timer.schedule(historicalTask, 1, refreshRate);

        Logger.getLogger(AcmeSystemViewProvider.class.getName()).log(
                Level.INFO, "SVP set to use historical data {0}",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(time));
    }
    
    @Override
    public Date getHistoricalTime() {
        return isCurrent ? null : historicalTime;
    }

    @Override
    public void update() {
        if (isCurrent) {
            int added = runtimeAggregator.getEventBuffer().drainToCollection(
                    eventStore);
            newEvents += added;
            eventStoreQueueHead -= added;
            eventStoreQueueHead = (eventStoreQueueHead < 0) ? 0
                    : eventStoreQueueHead;
        }
        ui.getViewControl().pushUpdate();
    }

    /**
     * Gets a particular value of a property in the model
     * 
     * @param mapping a string which uniquely identifies a property in the model
     * @return a data value which is the value of the property in the system
     *         view providers internal model which may be current or historical
     * @throws edu.cmu.rainbow_ui.display.AcmeConversionException
     */
    public DataValue getValue(String mapping) throws AcmeConversionException {
        IAcmeProperty property = (IAcmeProperty) ModelHelper
                .getElementFromQualifiedName(internalModel.getModelInstance(),
                        mapping);
        return AcmeConverterSupport.convertToDataValue(property);
    }

    @Override
    public boolean isCurrent() {
        return this.isCurrent;
    }

    @Override
    public IModelInstance<?> getView() {
        return this.internalModel;
    }

    @Override
    public void getHistoricalEventRange(Date start, Date end) {
        historyProvider.getEventRange(start, end);
    }

    @Override
    public void getHistoricalEventRangeByType(String channel, Date start,
            Date end) {
        historyProvider.getEventRangeByType(channel, start, end);
    }

    @Override
    public void getHistoricalModelEventRange(Date start, Date end) {
        historyProvider.getModelEventRange(start, end);
    }

    @Override
    public void provideHistoricalView(IModelInstance<?> view) {
        this.internalModel = (IModelInstance<IAcmeSystem>) view;
    }

    @Override
    public void provideHistoricalEvent(IRainbowMessage message) {
        eventStore.add(message);
    }

    @Override
    public void provideHistoricalEventRange(List<IRainbowMessage> messages) {
        eventStore.addAll(messages);
    }

    @Override
    public List<IRainbowMessage> getNewEvents() {
        List<IRainbowMessage> messages = new ArrayList<>();

        for (int i = eventStoreQueueHead; i < eventStore.size(); i++) {
            if (eventStore.get(i) != null) {
                messages.add(eventStore.get(i));
            }
        }

        newEvents = 0;
        return messages;
    }

    @Override
    public int getNewEventsCount() {
        return newEvents;
    }

    @Override
    public ArrayList<String> getSessionList() {
        return historyProvider.getSessionList();
    }

    @Override
    public void setSession(String session) {
        historyProvider.setSession(session);
        if (historyProvider.currentSessionIsWriteSession()) {
            currentIsWrite = true;
        } else {
            currentIsWrite = false;
        }
    }

    /**
     * Get the name of the session used to read data by a certain DBC instance.
     * 
     * @return the name of the session that is open to read
     */
    public String getReadSession() {
        return historyProvider.getReadSession();
    }

    @Override
    public Date getStartDate() {
        return historyProvider.getStartDate();
    }

    @Override
    public Date getMaxDate() {
        return historyProvider.getMaxDate();
    }

    @Override
    public boolean currentSessionIsWriteSession() {
        return currentIsWrite;
    }

    @Override
    public String getSession() {
        return historyProvider.getReadSession();
    }

    @Override
    public List<IRainbowMessage> getHistoricalEvents() {
        return this.historicalEvents;
    }

    @Override
    public List<IRainbowMessage> getNumberOfEventsBefore(Date endTime,
            int numEvents) {
        return historyProvider.getNumberOfEventsBefore(endTime, numEvents);
    }

    @Override
    public void stop() {
        historyProvider.closeDatabaseConnection();
        timer.cancel();
        runtimeAggregator.getEventBuffer().deactivate();
        runtimeAggregator.getEventBuffer().clear();
        Logger.getLogger(AcmeSystemViewProvider.class.getName()).info("System view provider has stopped");
    }
}
