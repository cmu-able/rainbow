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
package edu.cmu.rainbow_ui.common;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.model.acme.AcmeModelOperation;

import edu.cmu.rainbow_ui.ingestion.EventProcessingException;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;

/**
 * This class will mock a Rainbow system by feeding random events to the runtime
 * aggregator
 * 
 * @author Zachary Sweigart <zsweigar@cmu.edu>
 */
public class MockRainbow {
    private int updateRate; // in milliseconds
    private static final int DEFAULT_UPDATE_RATE = 1000;
    private TimerTask task;
    private final IRuntimeAggregator<?> agg;
    private Timer timer;
    private final int NUM_MESSAGE_TYPES = 1;

    /*
     * Set this to say how long you want to wait until the rate increases in
     * milliseconds
     */
    private final int TIME_TO_INCREASE = 30000;
    /*
     * Set this to say how many events you want to send per second for
     * increasing
     */
    private final int INCREASE_START = 10;
    /*
     * Set this to say how many events you want increase at each increment for
     * increasing
     */
    private final int INCREASE_STEP = 10;

    private final int MAX = 100;

    /*
     * Set this to the type of load you want
     */
    private final TestType type = TestType.LOW;

    private static enum TestType {
        /*
         * 1 event per second
         */
        LOW,
        /*
         * Increase from start to max by step every time seconds
         */
        INCREASING,
        /*
         * Max events per second
         */
        LOAD,
        /*
         * 1.1 * Max events per second
         */
        OVER,
    }

    /**
     * Constructor for a mock rainbow component
     * 
     * @param runtimeAgg the runtime aggregator that will consume the random
     *        events
     */
    public MockRainbow(IRuntimeAggregator<?> runtimeAgg) {
        this.agg = runtimeAgg;
        task = new MockRainbowTimerTask();
        updateRate = DEFAULT_UPDATE_RATE;
        timer = new Timer();
    }

    /**
     * Constructor for a mock rainbow component
     * 
     * @param agg the runtime aggregator that will consume the random events
     * @param updateRate the rate at which the random events will be provided
     */
    public MockRainbow(IRuntimeAggregator<?> agg, int updateRate) {
        this.agg = agg;
        task = new MockRainbowTimerTask();
        this.updateRate = updateRate;
        timer = new Timer();
    }

    /**
     * Starts the creation of random events
     */
    public void run() {
        task = new MockRainbowTimerTask();
        switch (type) {
        case INCREASING:
            for (int i = INCREASE_START; i < MAX; i += INCREASE_STEP) {
                timer = new Timer();
                updateRate = (int) (1000 / (1.0f * i));
                try {
                    timer.schedule(task, 1, updateRate);
                } catch (java.lang.IllegalStateException ex) {
                    timer = new Timer();
                    timer.schedule(task, 1, updateRate);
                }
                try {
                    Thread.sleep(TIME_TO_INCREASE);
                } catch (InterruptedException e) {
                    Logger.getLogger(MockRainbow.class.getName()).log(
                            Level.SEVERE, null, e);
                }
            }
            break;
        case LOAD:
            updateRate = (int) (1000 / (1.0f * MAX));
            break;
        case OVER:
            updateRate = (int) (1000 / ((1.0f / 1.1f) * MAX));
            break;
        case LOW:
            // fall through
        default:
            updateRate = 1000;
        }
        if (type != TestType.INCREASING) {
            try {
                timer.schedule(task, 1, updateRate);
            } catch (java.lang.IllegalStateException ex) {
                timer = new Timer();
                timer.schedule(task, 1, updateRate);
            }
        }
    }

    /**
     * Stops the creation of random events
     */
    public void cancel() {
        task.cancel();
    }

    /**
     * Class used internally to represent a new timer task
     */
    private class MockRainbowTimerTask extends TimerTask {

        /**
         * Specifies the code that will execute when the timer task is run
         */
        @Override
        public void run() {
            sendEvent();
        }

    }

    private void sendEvent() {
        int messageType = (int) (Math.random() * NUM_MESSAGE_TYPES);
        // create specific message
        switch (messageType) {
        case 0: // Model Change - Property Update - Event
            Float expRes = (float) (99.9 * Math.random());
            IRainbowMessage event = new RainbowESEBMessage();
            IModelInstance<IAcmeSystem> system = (IModelInstance<IAcmeSystem>) (agg
                    .getInternalModel());

            // Select a component randomly from the model
            Object[] components = system.getModelInstance().getComponents()
                    .toArray();
            int randVal = (int) (components.length * Math.random());
            IAcmeComponent component = (IAcmeComponent) components[randVal];

            // Select a property randomly from the component
            Object[] properties = component.getProperties().toArray();
            randVal = (int) (properties.length * Math.random());
            IAcmeProperty property = (IAcmeProperty) properties[randVal];

            String propertyProp = property.getQualifiedName();

            /* Generate random property value based on its type */
            String propertyValue = "";
            boolean typeRecognized = true;
            switch (property.getType().getName()) {
            case "int":
                propertyValue = Integer.toString((int) (Math.random() * 99.9));
                break;
            case "float":
                propertyValue = Float.toString((float) (Math.random() * 99.9));
                break;
            case "string":
                /* Keep the same */
                propertyValue = property.getValue().toString();
                break;
            case "boolean":
                propertyValue = Boolean.toString((Math.random() >= 0.5));
                break;
            default:
                typeRecognized = false;
            }

            if (typeRecognized) {

                try {
                    event.setProperty(IRainbowMessageFactory.EVENT_TYPE_PROP,
                            AcmeModelEventType.SET_PROPERTY_VALUE.toString());
                    event.setProperty(AcmeModelOperation.PROPERTY_PROP,
                            propertyProp);
                    event.setProperty(AcmeModelOperation.VALUE_PROP,
                            propertyValue);
                    event.setProperty(ESEBConstants.MSG_SENT,
                            System.currentTimeMillis() + "");
                    agg.processEvent("MODEL_CHANGE", event);
                    /* Send the same event to another channel */
                    agg.processEvent("MODEL_DS", event);
                } catch (RainbowException | EventProcessingException e) {
                    Logger.getLogger(MockRainbow.class.getName()).log(
                            Level.SEVERE, null, e);
                }
            }
        }
    }
}
