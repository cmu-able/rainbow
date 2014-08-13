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

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * Acme model Runtime Aggregator class for Rainbow UI Framework.
 *
 * <p>
 * Implements Rainbow event handlers. Each event is stored in the database. Model related events are
 * processed on internal model. Other events are buffered for further processing.
 *
 * This instance of Runtime Aggregator uses Acme system representation as an internal model.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeRuntimeAggregator implements IRuntimeAggregator<IAcmeSystem> {
    /* Synchronization to be used on model updates and model copy */

    protected static final Object copyLock = new Object();

    /* Internal Acme-based model */
    protected AcmeModelInstance internalModel;

    /* Internal event buffers */
    protected final IEventBuffer eventBuffer;
    private final static int EVENT_BUFFER_SIZE = 100;

    /* Deserializer for model update events */
    protected final AcmeEventDeserializer deserializer = new AcmeEventDeserializer();

    /* Rainbow connector */
    protected final IRainbowConnector rainbowConnector;

    /* System configuration */
    protected final ISystemConfiguration systemConfig;

    /* Whether the RA has been started */
    protected boolean isRunning;

    /* Connector for the database */
    protected final IDatabaseConnector databaseConnector;

    /* Snapshot rate and current number of processed model events */
    private int snapshotRate;
    private int processedModelEvents;

    /**
     * Runtime Aggregator constructor.
     *
     * @param config system configuration
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    public AcmeRuntimeAggregator(ISystemConfiguration config)
            throws RuntimeAggregatorException {
        this(config, new DatabaseConnector(config));
    }

    /**
     * Runtime Aggregator constructor with external DB connector.
     *
     * This method should only be used for unit testing
     *
     * @param config system configuration
     * @param dbConn external DB connector
     * @throws edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException
     */
    public AcmeRuntimeAggregator(ISystemConfiguration config,
            IDatabaseConnector dbConn) throws RuntimeAggregatorException {
        systemConfig = config;

        /* Load needed system configuration constants */
        snapshotRate = systemConfig.getSnapshotRate();

        /**
         * Create connector to the Rainbow system. This call just instantiate the connector object.
         * Real connection is established first time an attachment is made.
         */
        rainbowConnector = new ESEBRainbowConnector(systemConfig, this);

        /* Get Model */
        internalModel = getModel();

        /* Create event buffers */
        eventBuffer = new EventBuffer(EVENT_BUFFER_SIZE);

        /* Create connection to DB */
        databaseConnector = dbConn;
        isRunning = false;
    }

    /**
     * Obtain the model.
     *
     * The model may be stored either in local file or get from the Rainbow system remotely.
     *
     * @return obtained model instance
     * @throws RuntimeAggregatorException
     */
    private AcmeModelInstance getModel() throws RuntimeAggregatorException {
        AcmeModelInstance model = null;
        String modelClass = systemConfig.getModelFactoryClass();
        String modelName = systemConfig.getModelName();
        String modelPath = systemConfig.getConfigDir()
                + systemConfig.getModelPath();

        if (systemConfig.isModelLocal()) {
            try {
                /* Load the model class */
                Class loadClass = Class.forName(modelClass);
                Method method = loadClass.getMethod("loadCommand",
                        ModelsManager.class, String.class, InputStream.class,
                        String.class);
                if (!Modifier.isStatic(method.getModifiers())
                        || method.getDeclaringClass() != loadClass) {
                    throw new RuntimeAggregatorException(
                            MessageFormat
                            .format("The class {0} does not implement a static method loadCommand, used to generate modelInstances",
                                    method.getDeclaringClass()
                                    .getCanonicalName()));
                }
                /* Load the model from external file */
                AbstractLoadModelCmd<?> load = (AbstractLoadModelCmd<?>) method
                        .invoke(null, null, modelName, new FileInputStream(
                                        modelPath), modelPath);
                load.execute(null, null);
                model = (AcmeModelInstance) load.getResult();
            } catch (Exception ex) {
                Logger.getLogger(AcmeRuntimeAggregator.class.getName()).log(
                        Level.SEVERE, null, ex);
                throw new RuntimeAggregatorException(
                        "Cannot load the model from the local file.");
            }
        } else {
            try {
                /* Get the model from Rainbow via RPC */
                model = (AcmeModelInstance) rainbowConnector
                        .getRemoteModel(modelName);
            } catch (IOException | ParticipantException ex) {
                Logger.getLogger(AcmeRuntimeAggregator.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }

        return model;
    }

    @Override
    public void start() throws RuntimeAggregatorException {
        /**
         * Refresh the model. This action may fail and throw an exception, everything below will not
         * be executed.
         */
        internalModel = getModel();

        /* Attach to Rainbow */
        try {
            rainbowConnector.attachEventListeners();
        } catch (IOException ex) {
            throw new RuntimeAggregatorException("Cannot attach event listeners to Rainbow buses");
        }

        /**
         * Start new session in the database. Note that the session name can contain only
         * alphanumeric characters.
         */
        DateFormat df = new SimpleDateFormat("MMddyyyy_HHmmss");
        Date now = Calendar.getInstance().getTime();
        String sessionDate = df.format(now);
        String modelName = systemConfig.getModelName().replaceAll("\\W", "");
        databaseConnector.createSession("session_" + sessionDate + "_" + modelName);
        databaseConnector.writeSnapshot(internalModel, now);

        processedModelEvents = 0;
        isRunning = true;
    }

    @Override
    public void stop() {
        /* Detach from Rainbow */
        rainbowConnector.detachEventListeners();

        /* Disregard all consequent events */
        isRunning = false;

        /* Close the writing session in the DB */
        databaseConnector.closeWriteSession();
    }

    @Override
    public IModelInstance<IAcmeSystem> getInternalModel() {
        return internalModel;
    }

    @Override
    public IModelInstance<IAcmeSystem> copyInternalModel()
            throws RainbowCopyException {
        /* Lock model updates while copiing */
        synchronized (copyLock) {
            return internalModel.copyModelInstance("Acme");
        }
    }

    @Override
    public IEventBuffer getEventBuffer() {
        return eventBuffer;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The method is synchronized to avoid out-of-order execution of events
     * </p>
     */
    @Override
    synchronized public void processEvent(String channel, IRainbowMessage event)
            throws EventProcessingException {
        /* Do not process events when the RA is not active */
        if (!isRunning) {
            return;
        }

        switch (channel) {
            case "MODEL_CHANGE":
                processModelUpdate(event);
                break;
            case "MODEL_DS":
            case "MODEL_US":
                processStreamingEvent(channel, event);
                break;
            default:
                throw new EventProcessingException("Unsupported channel: "
                        + channel);
        }
    }

    /**
     * Get the event timestamp.
     *
     * Package-scoped for testing.
     *
     * @param event a Rainbow message
     * @return timestamp in a Date format
     */
    Date getEventTimestamp(IRainbowMessage event) {
        long sent = Long.parseLong((String) event.getProperty(ESEBConstants.MSG_SENT));
        Date timestamp = new Date(sent);
        return timestamp;
    }

    /**
     * Process model update event.
     *
     * <p>
     * Decomposes an event into an Acme command and executes it.
     * </p>
     *
     * @param event - Rainbow event message
     * @throws edu.cmu.rainbow_ui.ingestion.EventProcessingException
     */
    protected void processModelUpdate(IRainbowMessage event)
            throws EventProcessingException {
        /* Execute the update on the model */
        try {
            IAcmeCommand<?> command = deserializer.deserialize(event,
                    internalModel.getModelInstance());
            /*
             * Lock the model while the command is executed to avoid
             * inconsistency on model copiing
             */
            synchronized (copyLock) {
                command.execute();
            }
        } catch (IllegalStateException | AcmeException | RainbowDeserializationException ex) {
            throw new EventProcessingException(ex.toString());
        }

        /* Save snapshot if needed by the number of events */
        /*
         * Note on synchronization: the method processEvent is synchronized, so
         * no invocations of runtime aggregator will be in this method at the
         * same time. Thus, it is safe to modify the counter directly.
         */
        processedModelEvents += 1;
        if (processedModelEvents >= snapshotRate) {
            processedModelEvents = 0;
            /*
             * TODO: the code below doesn't works, since copying the internal model
             * actually create a copy of the system under AcmeModel which results in linear
             * growth of the model size(proportional to the number of systems = number
             * of times copy model was called).
             * 
             * When the issue with copying will be resolved the code with copying is more
             * preferable, since it doesn't involve internal model lock for the time model 
             * snapshot is being writtent to the database.
             */
//            try {
//                IModelInstance<IAcmeSystem> copy = copyInternalModel();
//                databaseConnector.writeSnapshot(copy, getEventTimestamp(event));
//
//            } catch (RainbowCopyException ex) {
//                throw new EventProcessingException(ex.toString());
//            }
            /*
             * TODO: this works, but is not optimal
             */
            synchronized (copyLock) {
                databaseConnector.writeSnapshot(internalModel, getEventTimestamp(event));
            }
        }
        /**
         * Save event to special model update storage
         */
        databaseConnector.writeModelUpdateEvent(event, getEventTimestamp(event));
    }

    /**
     * Process a streaming event.
     *
     * <p>
     * This method stores streaming(non-model related) events to the internal event buffer.
     * </p>
     *
     * @param channel channel the event came from
     * @param event event to process
     */
    protected void processStreamingEvent(String channel, IRainbowMessage event) {
        eventBuffer.add(event);
        /**
         * Save all events in the history DB
         */
        databaseConnector.writeEvent(channel, event, getEventTimestamp(event));
    }
}
