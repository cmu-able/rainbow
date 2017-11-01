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
package org.sa.rainbow.core.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.util.Util;

/**
 * The Models manager is a component that provides access to the models in Rainbow. It should also contain: - a
 * reference to the model update US bus, as a subscriber to events to get information - a reference to the model change
 * bus, as a publisher - all executed commands should publish events to this bus
 *
 * @author Bradley Schmerl: schmerl
 */
public class ModelsManager extends AbstractRainbowRunnable implements IModelsManager {
    static final Logger LOGGER = Logger.getLogger (ModelsManager.class);

    /**
     * The bus on which to announce model change events
     **/
    protected IModelChangeBusPort m_changeBusPort;

    /**
     * The bus on which requests to change models are made by analyses and gauges
     **/
    protected IModelUSBusPort m_upstreamBusPort;

    /**
     * Provides remote access to the models
     **/
    private IModelsManagerPort m_remoteModelManagerPort;

    /**
     * Contains all the models -- keyed by Type then name
     **/
    protected final Map<String, Map<String, IModelInstance<?>>> m_modelMap = new HashMap<> ();

    /**
     * Contains the queue of commands waiting to be executed on the models
     **/
    protected final BlockingQueue<Object> commandQ = new LinkedBlockingQueue<> ();

    protected final Map<ModelReference, File> m_modelsToSave = new HashMap<> ();

    protected final Map<ModelReference, FileChannel> m_modelLogs = new HashMap<> ();

    public ModelsManager () {
        super ("Models Manager");
        try {
            m_reportingPort = new DisconnectedRainbowDelegateConnectionPort ();
        } catch (IOException e) {
            // Should never happen
        }
    }


    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        initializeModels ();
    }

    public void initializeModels () {
        String numberOfModelsStr = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_NUMBER, "0");
        int numberOfModels = Integer.parseInt (numberOfModelsStr);
        for (int modelNum = 0; modelNum < numberOfModels; modelNum++) {
            String factoryClassName = Rainbow.instance ().getProperty (
                    RainbowConstants.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum);
            if (factoryClassName == null || "".equals (factoryClassName)) {
                continue;
            }
            String modelName = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_NAME_PREFIX + modelNum);
            String path = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_PATH_PREFIX + modelNum);
            String saveOnClose = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_SAVE_PREFIX +
                    modelNum);
            // It is possible for a model not to be sourced from a file, in which case
            // the load command may just create and register the model in the manager
            File modelPath = null;
            if (path != null) {
                modelPath = new File (path);
                if (!modelPath.isAbsolute ()) {
                    modelPath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), path);
                }

            }
            try {
                // The factory class should provide a static method for generating a load command that can be
                // called to load a model into the model manager
                // This is currently done through a set of properties.
                // The load command is responsible for registering the model after loading
                // Q: is this the right place for that?
                Class loadClass = Class.forName (factoryClassName);
                Method method = loadClass.getMethod ("loadCommand", ModelsManager.class, String.class,
                        InputStream.class, String.class);
                if (!Modifier.isStatic (method.getModifiers ()) || method.getDeclaringClass () != loadClass)
                    throw new UnsupportedOperationException (MessageFormat
                            .format (
                                    "The class {0} does not implement a " +
                                            "static method loadCommand, used" +
                                            " to generate modelInstances",
                                            method.getDeclaringClass ()
                                            .getCanonicalName ()));
                AbstractLoadModelCmd load = (AbstractLoadModelCmd) method.invoke (null, this, modelName,
                        modelPath == null ? null : new
                                FileInputStream (modelPath)
                                , modelPath == null ? null
                                        : modelPath.getAbsolutePath
                                        ());
                List<? extends IRainbowMessage> events = load.execute (null, m_changeBusPort);
                // Announce the loading on the change bus.
                // Q: should this be done in clients or in the commands themselves?
                if (m_changeBusPort != null) {
                    m_changeBusPort.announce (events);
                }
                IModelInstance instance = (IModelInstance) load.getResult ();
                boolean toSave = saveOnClose == null ? false : Boolean.valueOf (saveOnClose);
                ModelReference ref = new ModelReference (instance.getModelName (), instance.getModelType ());
                if (toSave) {
                    String saveLocation = Rainbow.instance ().getProperty (RainbowConstants
                            .RAINBOW_MODEL_SAVE_LOCATION_PREFIX
                            + modelNum);
                    if (saveLocation == null) {
                        saveLocation = path;
                    }
                    File savePath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), saveLocation);
                    m_modelsToSave.put (ref, savePath);
                }

                // Open log files
                File targetPath = Rainbow.instance ().getTargetPath ();
                File logPath = new File (targetPath, "log");
                logPath.mkdirs ();
                File logFile = new File (logPath, ref.getModelName () + "-" + ref.getModelType () + ".log");

                FileOutputStream fos = new FileOutputStream (logFile);
                m_modelLogs.put (ref, fos.getChannel ());

                m_reportingPort.info (
                        getComponentType (),
                        "Successfully loaded and registered " + instance.getModelName () + ":"
                                + instance.getModelType (), LOGGER);
            } catch (ClassNotFoundException e) {

                String msg = MessageFormat.format (
                        "Could not locate the class ''{0}'' to load the model. ''{1}'' not loaded.", factoryClassName,
                        modelPath);
                m_reportingPort.error (getComponentType (), msg, e, LOGGER);
            } catch (IllegalAccessException e) {
                m_reportingPort.error (getComponentType (), "Exception", e, LOGGER);
            } catch (FileNotFoundException e) {
                String msg = MessageFormat.format (
                        "Could not load the model file ''{0}''. It was resolved to the path ''{1}''.", path,
                        modelPath != null ? modelPath.getAbsolutePath () : null);
                m_reportingPort.error (getComponentType (), msg, e, LOGGER);
            } catch (IllegalStateException | RainbowException e) {
                String msg = MessageFormat.format (
                        "Could not execute the load command for the model ''{0}''. ''{1}'' not loaded.", modelName,
                        modelPath);
                m_reportingPort.error (getComponentType (), msg, e, LOGGER);
            } catch (NoSuchMethodException | SecurityException e) {
                String msg = MessageFormat.format (
                        "Could not access static method loadCommand in ''{0}''. ''{1}'' not loaded.", factoryClassName,
                        modelPath);
                m_reportingPort.error (getComponentType (), msg, e, LOGGER);
            } catch (UnsupportedOperationException e) {
                m_reportingPort.error (getComponentType (), e.getMessage (), e, LOGGER);
            } catch (IllegalArgumentException | InvocationTargetException e) {
                String msg = MessageFormat.format ("Error calling loadCommand in {0}. {1} not loaded.",
                        factoryClassName, modelPath);
                m_reportingPort.error (getComponentType (), msg, e, LOGGER);
            } catch (Throwable e) {
                m_reportingPort.error (getComponentType (), MessageFormat.format (
                        "There was an error creating the model in {0}. Exception: {1}", modelPath, e.getMessage ()), e);
            }
        }
    }

    private void initializeConnections () {
        try {
            // Publish to change bus
            m_changeBusPort = RainbowPortFactory.createChangeBusAnnouncePort ();
            // Listen to upstream messages
            m_upstreamBusPort = RainbowPortFactory.createModelsManagerUSPort (this);
            // Create a remote port for model access
            m_remoteModelManagerPort = RainbowPortFactory.createModelsManagerProviderPort (this);
        } catch (RainbowConnectionException e) {
            LOGGER.error ("Could not connect the appropriate ports", e);
        }
    }

    @Override
    public void registerModelType (String typeName) {
        if (!m_modelMap.containsKey (typeName)) {
            m_modelMap.put (typeName, new HashMap<String, IModelInstance<?>> ());
        }
    }


    @Override
    public Collection<? extends String> getRegisteredModelTypes () {
        return m_modelMap.keySet ();
    }


    @Override
    public Collection<? extends IModelInstance<?>> getModelsOfType (String modelType) {
        Map<String, IModelInstance<?>> map = m_modelMap.get (modelType);
        if (map != null) return map.values ();
        return Collections.<IModelInstance<?>>emptySet ();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> IModelInstance<T> getModelInstance (ModelReference modelRef) {
        Map<String, IModelInstance<?>> models = m_modelMap.get (modelRef.getModelType ());
        if (models != null) return (IModelInstance<T>) models.get (modelRef.getModelName ());
        return null;
    }

    @Override
    public synchronized <T> IModelInstance<T> getModelInstanceByResource (String resource) {
        Collection<Map<String, IModelInstance<?>>> values = m_modelMap.values ();
        IModelInstance<T> foundModel = null;
        for (Iterator<Map<String, IModelInstance<?>>> iterator = values.iterator (); iterator.hasNext ()
                && foundModel == null; ) {
            Map<String, IModelInstance<?>> map = iterator.next ();
            Collection<IModelInstance<?>> values2 = map.values ();
            for (Iterator<IModelInstance<?>> it2 = values2.iterator (); it2.hasNext () && foundModel == null; ) {
                IModelInstance<?> model = it2.next ();
                if (resource.equals (model.getOriginalSource ())) {
                    foundModel = (IModelInstance<T>) model;
                }
            }
        }
        return foundModel;
    }

    @Override
    public synchronized <T> IModelInstance<T> copyInstance (ModelReference modelRef, String copyName)
            throws RainbowModelException {
        Map<String, IModelInstance<?>> models = m_modelMap.get (modelRef.getModelType ());
        if (models != null) {
            IModelInstance<T> model = (IModelInstance<T>) models.get (modelRef.getModelName ());
            if (model != null) {
                if (models.get (copyName) == null) {
                    try {
                        synchronized (model.getModelInstance ()) {
                            IModelInstance<T> copy = model.copyModelInstance (copyName);
                            registerModel (new ModelReference (copyName, modelRef.getModelType ()), copy);
                            return copy;
                        }
                    } catch (RainbowCopyException e) {
                        throw new RainbowModelException (e);
                    }
                } else
                    throw new RainbowModelException (MessageFormat.format (
                            "A model with the name ''{0}'' of the type ''{1}'' already exists!", copyName,
                            modelRef.getModelType ()));
            } else
                throw new RainbowModelException (MessageFormat.format (
                        "No model of type ''{0}'' exists with name ''{1}''!", modelRef.getModelType (),
                        modelRef.getModelName ()));
        }
        throw new RainbowModelException (MessageFormat.format ("The type ''{0}'' is not a registered model type.",
                modelRef.getModelType ()));
    }

    @Override
    public synchronized void registerModel (ModelReference modelRef, IModelInstance<?> model)
            throws RainbowModelException {
        Map<String, IModelInstance<?>> models = m_modelMap.get (modelRef.getModelType ());
        if (models != null) {
            // Should I check if the instance is already registered?
            IModelInstance<?> existingModel = models.get (modelRef.getModelName ());
            if (existingModel != null) {
                synchronized (existingModel) {
                    models.put (modelRef.getModelName (), model);
                }
            } else {
                models.put (modelRef.getModelName (), model);
//            model.setChangePort (m_changeBusPort);
                // TODO: attach the change bus port to the model
            }
        } else
            throw new RainbowModelException (MessageFormat.format ("The type ''{0}'' is not a registered model type.",
                    modelRef.getModelType ()));
    }

    @Override
    public synchronized void unregisterModel (IModelInstance<?> model) throws RainbowModelException {
        Map<String, IModelInstance<?>> models = m_modelMap.get (model.getModelType ());
        boolean success = false;
        if (models != null) {
            synchronized (model.getModelInstance ()) {
                success = unregisterModel (models, model);
            }
        } else {
            // do it the slow way
            synchronized (model.getModelInstance ()) {
                for (Map<String, IModelInstance<?>> m : m_modelMap.values ()) {
                    success = unregisterModel (m, model);
                    if (success) {
                        break;
                    }
                }
            }
        }
        if (!success) {
            LOGGER.warn (MessageFormat.format (
                    "The model does not seem to be registered with the Models Manager: {0} + (type: ''{1}'')", model,
                    model.getModelType ()));
        }
    }

    private synchronized boolean unregisterModel (Map<String, IModelInstance<?>> models, IModelInstance<?> model) {
        Iterator<Entry<String, IModelInstance<?>>> it = models.entrySet ().iterator ();
        boolean deleted = false;
        while (it.hasNext () && !deleted) {
            Entry<String, IModelInstance<?>> e = it.next ();
            if (e.getValue () == model) {
                it.remove ();
                try {
                    model.dispose ();
                } catch (RainbowException e1) {
                    LOGGER.warn ("Failed to dispose of the system in model manager registerd as "
                            + model.getModelName () + ":" + model.getModelType ());
                }
                deleted = true;
            }
        }
        return deleted;
    }

    /**
     * Adds commands to the command queue to be processed by the models
     *
     * @param command The command to add
     * @throws IllegalStateException
     * @throws RainbowException
     */
    @Override
    public void requestModelUpdate (IRainbowOperation command) throws IllegalStateException,
    RainbowException {
        commandQ.offer (command);


    }

    private IRainbowModelOperation setupCommand (IRainbowOperation command,
            IModelInstance<?> modelInstance) throws RainbowException {
        IRainbowModelOperation rcmd;
        if (!(command instanceof IRainbowModelOperation)) {
            // Try to use the model instanceo to turn it into an executable command
            String[] cargs = new String[1 + command.getParameters ().length];
            cargs[0] = command.getTarget ();
            for (int i = 0; i < command.getParameters ().length; i++) {
                cargs[1 + i] = command.getParameters ()[i];
            }
            rcmd = modelInstance.getCommandFactory ().generateCommand (command.getName (), cargs);
            rcmd.setOrigin (command.getOrigin ());
        } else {
            rcmd = (IRainbowModelOperation) command;
        }

//        if (!(command instanceof IRainbowModelCommand)) throw new RainbowException (MessageFormat.format ("The
// command {0} is not an executable command.",
//                command.getCommandName ()));
        return rcmd;
    }

    @Override
    public void requestModelUpdate (List<IRainbowOperation> commands, boolean transaction) {
        LOGGER.info (MessageFormat.format ("Updating the model with {0} commands, transaction = {1}", commands.size (),
                transaction));
        // If the command is to be executed transactionally, then add the list to the queue, otherwise add each
        // command individually
        if (transaction) {
            commandQ.offer (commands);
        } else {
            for (IRainbowOperation command : commands) {
                commandQ.offer (command);
            }
        }

    }

    @Override
    public void dispose () {

        for (Entry<ModelReference, File> modelEntries : m_modelsToSave.entrySet ()) {
            ModelReference ref = modelEntries.getKey ();
            File saveTo = modelEntries.getValue ();

            Map<String, IModelInstance<?>> map = m_modelMap.get (ref.getModelType ());
            if (map != null) {
                IModelInstance<?> model = map.get (ref.getModelName ());
                try {
                    AbstractSaveModelCmd saveCommand = model.getCommandFactory ().saveCommand (
                            saveTo.getAbsolutePath ());
                    saveCommand.execute (model, null);
                } catch (IllegalStateException | RainbowException e) {
                    m_reportingPort.error (getComponentType (), "Failed to save " + ref.toString ());
                }
            }

        }

        for (Entry<String, Map<String, IModelInstance<?>>> mts : m_modelMap.entrySet ()) {
            for (Entry<String, IModelInstance<?>> entry : mts.getValue ().entrySet ()) {
                try {
                    entry.getValue ().dispose ();
                } catch (RainbowException e) {
                    e.printStackTrace ();
                }
            }
        }

        m_changeBusPort.dispose ();
        m_upstreamBusPort.dispose ();

        m_changeBusPort = null;
        m_upstreamBusPort = null;
    }

    @Override
    protected void log (String txt) {

    }

    @Override
    protected synchronized void runAction () {
        Object poll = commandQ.poll ();
        if (poll instanceof IRainbowOperation) {
            try {
                IRainbowOperation command = (IRainbowOperation) poll;
                IModelInstance<?> modelInstance = getModelInstance (command.getModelReference ());
                if (modelInstance == null) {
                    reportingPort ().error (RainbowComponentT.MODEL,
                            MessageFormat.format ("Could not find model {0} for " +
                                    "command: {1}",
                                    command.getModelReference ().toString (),
                                    command.toString ()));
                    return;
                }
                IRainbowModelOperation cmd = setupCommand (command, modelInstance);
                List<? extends IRainbowMessage> events;
                synchronized (modelInstance.getModelInstance ()) {
                    events = cmd.execute (modelInstance, m_changeBusPort);
                }
                if (events.size () > 0) {
                    m_reportingPort.info (RainbowComponentT.MODEL, MessageFormat.format (
                            "Executing {0}", command.toString ()));
                }
                if (cmd.canUndo () && events.size () > 0) {
                    // The command executed correctly if we can undo it.
                    // Announce all the changes on the the change bus
                    m_changeBusPort.announce (events);
                    logModelOperation (command, true);
                } else {
                    logModelOperation (command, false);
                }
            } catch (IllegalStateException | RainbowException e) {
                e.printStackTrace ();
            }
        } else if (poll instanceof List) {
            List<IRainbowOperation> commands = (List<IRainbowOperation>) poll;
            boolean transaction = true;
            // Keep track of successfully executed commands in case we need to undo 
            Stack<IRainbowModelOperation> executedCommands = new Stack<> ();
            // Stores the events that will be reported to the change bus
            List<IRainbowMessage> events = new LinkedList<> ();
            // Indicates whether all the commands have been executed successfully so far
            boolean complete = true;
            if (!commands.isEmpty ()) {
                IRainbowOperation c = commands.iterator ().next ();
                // The model being updated should be the same for all commands, so just grab the first one
                IModelInstance<?> modelInstance = getModelInstance (c.getModelReference ()); //c.getModelReference ()
                // .getModelType (), c.getModelReference ().getModelName ());
                synchronized (modelInstance.getModelInstance ()) {

                    for (IRainbowOperation cmd : commands) {
                        try {
                            // Make sure that the model is the same 
                            IModelInstance<?> mi = getModelInstance (cmd.getModelReference ()); //cmd.getModelType ()
                            // , cmd.getModelName ());
                            if (mi != modelInstance) {
                                if (transaction) {
                                    // If not the same, this is an error so log it as such an mark as incomplete
                                    complete = false;
                                    m_reportingPort.error (RainbowComponentT.MODEL,
                                            "All commands in a transaction must be on the same model.");
                                    break;
                                } else {
                                    // Otherwise, just log a warning
                                    m_reportingPort.warn (RainbowComponentT.MODEL,
                                            "All commands in the transaction should be on the same " +
                                            "model.");
                                }
                            }
                            // Make sure the command is executable, and add the ancilliary execution information
                            String name = cmd.getName ();
                            cmd = setupCommand (cmd, mi);
                            // If it is not executable, the throw
                            if (cmd == null)
                                throw new RainbowException (MessageFormat.format (
                                        "The command {0} is not an executable command.", name));
                            IRainbowModelOperation mcmd = (IRainbowModelOperation) cmd;
                            // Execute the command
                            List<? extends IRainbowMessage> cmdEvents = mcmd.execute (mi, m_changeBusPort);
                            if (cmdEvents.size () > 0) {
                                m_reportingPort.info (RainbowComponentT.MODEL,
                                        MessageFormat.format ("Executing {0}", mcmd.toString ()));
                            }

                            // Store all the generated events to announce later
                            events.addAll (cmdEvents);
                            // Recall what we executed in case we need to rollback
                            executedCommands.push (mcmd);
                        } catch (IllegalStateException | RainbowException e) {
                            complete = false;
                            break;
                        }
                    }

                    if (!complete && transaction && !executedCommands.isEmpty ()) {
                        // Undo executed commands if we didn't complete.
                        m_reportingPort.warn (RainbowComponentT.MODEL, MessageFormat.format (
                                "Not all of the commands completed successfully. {0} did, so undoing them.",
                                executedCommands.size ()));
                        logModelOperations (commands, false);
                        IRainbowModelOperation<?, ?> cmd;
                        while (!executedCommands.isEmpty ()) {
                            try {
                                cmd = executedCommands.pop ();
                                cmd.undo ();
                            } catch (IllegalStateException | RainbowException e) {
                                LOGGER.error ("Could not undo the commands. Model could be in an inconsistent state",
                                        e);
                            }
                        }
                    } else {
                        // Announce the changes
                        m_changeBusPort.announce (events);
                        logModelOperations (commands, true);
                    }
                }
            }
        }
    }

    private void logModelOperations (List<IRainbowOperation> commands, boolean b) {
        for (IRainbowOperation op : commands) {
            logModelOperation (op, b);
        }
    }

    private void logModelOperation (IRainbowOperation command, boolean success) {
        FileChannel file = m_modelLogs.get (command.getModelReference ());
        if (file != null) {
            Date d = new Date ();
            String log = MessageFormat.format ("{0,number,#},{1},{2}\n", d.getTime (), command.toString (), success);
            try {
                file.write (java.nio.ByteBuffer.wrap (log.getBytes ()));
            } catch (IOException e) {
                LOGGER.error ("Failed to write " + log + " to log file");
            }
        }

    }


    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.MODEL;
    }

    @Override
    protected void doTerminate () {
        for (FileChannel c : m_modelLogs.values ()) {
            try {
                c.close ();
            } catch (IOException e) {
            }
        }
        super.doTerminate ();
    }

}
