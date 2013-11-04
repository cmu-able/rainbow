package org.sa.rainbow.core.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
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
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.util.Util;

/**
 * The Models manager is a component that provides access to the models in Rainbow. It should also contain: - a
 * reference to the model update US bus, as a subscriber to events to get information - a reference to the model change
 * bus, as a publisher - all executed commands should publish events to this bus
 * 
 * 
 * Question: Should this be a thread?
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class ModelsManager extends AbstractRainbowRunnable implements IModelsManager {
    static Logger                                      LOGGER     = Logger.getLogger (ModelsManager.class);

    /** The bus on which to announce model change events **/
    protected IModelChangeBusPort                    m_changeBusPort;

    /** The bus on which requests to change models are made by analyses and gauges **/
    protected IModelUSBusPort                   m_upstreamBusPort;

    /** Contains all the models -- keyed by Type then name **/
    protected Map<String, Map<String, IModelInstance>> m_modelMap = new HashMap<> ();

    /** Contains the queue of commands waiting to be executed on the models **/
    protected BlockingQueue                            commandQ   = new LinkedBlockingQueue<> ();

    public ModelsManager () {
        super ("Models Manager");
        m_reportingPort = new DisconnectedRainbowDelegateConnectionPort ();
    }


    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        initializeModels ();
    }

    public void initializeModels () {
        String numberOfModelsStr = Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_NUMBER, "0");
        int numberOfModels = Integer.parseInt (numberOfModelsStr);
        for (int modelNum = 0; modelNum < numberOfModels; modelNum++) {
            String factoryClassName = Rainbow.getProperty (
                    RainbowConstants.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum);
            String modelName = Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_NAME_PREFIX + modelNum);
            String path = Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_PATH_PREFIX + modelNum);
            File modelPath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), path);
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
                                    "The class {0} does not implement a static method loadCommand, used to generate modelInstances",
                                    method.getDeclaringClass ().getCanonicalName ()));
                AbstractLoadModelCmd load = (AbstractLoadModelCmd )method.invoke (null, this, modelName,
                        modelPath == null ? null : new FileInputStream (modelPath), modelPath.getAbsolutePath ());
                List<? extends IRainbowMessage> events = load.execute (null, m_changeBusPort);
                // Announce the loading on the change bus.
                // Q: should this be done in clients or in the commands themselves?
                if (m_changeBusPort != null) {
                    m_changeBusPort.announce (events);
                }
                IModelInstance instance = (IModelInstance )load.getResult ();
                LOGGER.info ("Successfully loaded and registered " + instance.getModelName () + ":"
                        + instance.getModelType ());
            }
            catch (ClassNotFoundException e) {
                LOGGER.error (MessageFormat.format (
                        "Could not locate the class ''{0}'' to load the model. ''{1}'' not loaded.", factoryClassName,
                        modelPath), e);
            }
            catch (IllegalAccessException e) {
                LOGGER.error (e);
            }
            catch (FileNotFoundException e) {
                LOGGER.error (MessageFormat.format (
                        "Could not load the model file ''{0}''. It was resolved to the path ''{1}''.", path,
                        modelPath.getAbsolutePath ()), e);
            }
            catch (IllegalStateException | RainbowException e) {
                LOGGER.error (MessageFormat.format (
                        "Could not execute the load command for the model ''{0}''. ''{1}'' not loaded.", modelName,
                        modelPath), e);
            }
            catch (NoSuchMethodException | SecurityException e) {
                LOGGER.error (MessageFormat.format (
                        "Could not access static method loadCommand in ''{0}''. ''{1}'' not loaded.", factoryClassName,
                        modelPath));
            }
            catch (UnsupportedOperationException e) {
                LOGGER.error (e.getMessage (), e);
            }
            catch (IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error (MessageFormat.format ("Error calling loadCommand in {0}. {1} not loaded.",
                        factoryClassName, modelPath));
            }
        }
    }

    private void initializeConnections () {
        try {
            // Publish to change bus
            m_changeBusPort = RainbowPortFactory.createChangeBusAnnouncePort ();
            // Listen to upstream messages
            m_upstreamBusPort = RainbowPortFactory.createModelsManagerUSPort (this);
        }
        catch (RainbowConnectionException e) {
            LOGGER.error ("Could not connect the appropriate ports", e);
        }
    }

    @Override
    public void registerModelType (String typeName) {
        if (!m_modelMap.containsKey (typeName)) {
            m_modelMap.put (typeName, new HashMap<String, IModelInstance> ());
        }
    }

    @Override
    public Collection<? extends String> getRegisteredModelTypes () {
        return m_modelMap.keySet ();
    }

    @Override
    public Collection<? extends IModelInstance> getModelsOfType (String modelType) {
        Map<String, IModelInstance> map = m_modelMap.get (modelType);
        if (map != null) return map.values ();
        return Collections.<IModelInstance> emptySet ();
    }

    @Override
    public <T> IModelInstance<T> getModelInstance (String modelType, String modelName) {
        Map<String, IModelInstance> models = m_modelMap.get (modelType);
        if (models != null) return models.get (modelName);
        return null;
    }

    @Override
    public <T> IModelInstance<T> getModelInstanceByResource (String resource) {
        Collection<Map<String, IModelInstance>> values = m_modelMap.values ();
        IModelInstance<T> foundModel = null;
        for (Iterator iterator = values.iterator (); iterator.hasNext () && foundModel == null;) {
            Map<String, IModelInstance> map = (Map<String, IModelInstance> )iterator.next ();
            Collection<IModelInstance> values2 = map.values ();
            for (Iterator it2 = values2.iterator (); it2.hasNext () && foundModel == null;) {
                IModelInstance model = (IModelInstance )it2.next ();
                if (resource.equals (model.getOriginalSource ())) {
                    foundModel = model;
                }
            }
        }
        return foundModel;
    }

    @Override
    public <T> IModelInstance<T> copyInstance (String modelType, String modelName, String copyName)
            throws RainbowModelException {
        Map<String, IModelInstance> models = m_modelMap.get (modelType);
        if (models != null) {
            IModelInstance<T> model = models.get (modelName);
            if (model != null) {
                if (models.get (copyName) == null) {
                    try {
                        synchronized (model.getModelInstance ()) {
                            IModelInstance<T> copy = model.copyModelInstance (copyName);
                            registerModel (modelType, copyName, copy);
                            return copy;
                        }
                    }
                    catch (RainbowCopyException e) {
                        throw new RainbowModelException (e);
                    }
                }
                else
                    throw new RainbowModelException (MessageFormat.format (
                            "A model with the name ''{0}'' of the type ''{1}'' already exists!", copyName, modelType));
            }
            else
                throw new RainbowModelException (MessageFormat.format (
                        "No model of type ''{0}'' exists with name ''{1}''!", modelType, modelName));
        }
        throw new RainbowModelException (MessageFormat.format ("The type ''{0}'' is not a registered model type.",
                modelType));
    }

    @Override
    public void registerModel (String modelType, String modelName, IModelInstance<?> model)
            throws RainbowModelException {
        Map<String, IModelInstance> models = m_modelMap.get (modelType);
        if (models != null) {
            // Should I check if the instance is already registered?
            IModelInstance existingModel = models.get (modelName);
            if (existingModel != null) {
                synchronized (existingModel) {
                    models.put (modelName, model);
                }
            }
            else {
                models.put (modelName, model);
//            model.setChangePort (m_changeBusPort);
                // TODO: attach the change bus port to the model
            }
        }
        else
            throw new RainbowModelException (MessageFormat.format ("The type ''{0}'' is not a registered model type.",
                    modelType));
    }

    @Override
    public void unregisterModel (IModelInstance<?> model) throws RainbowModelException {
        Map<String, IModelInstance> models = m_modelMap.get (model.getModelType ());
        boolean success = false;
        if (models != null) {
            synchronized (model.getModelInstance ()) {
                success = unregisterModel (models, model);
            }
        }
        else {
            // do it the slow way
            synchronized (model.getModelInstance ()) {
                for (Map<String, IModelInstance> m : m_modelMap.values ()) {
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

    private boolean unregisterModel (Map<String, IModelInstance> models, IModelInstance<?> model) {
        Iterator<Entry<String, IModelInstance>> it = models.entrySet ().iterator ();
        boolean deleted = false;
        while (it.hasNext () && !deleted) {
            Entry<String, IModelInstance> e = it.next ();
            if (e.getValue () == model) {
                it.remove ();
                try {
                    model.dispose ();
                }
                catch (RainbowException e1) {
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
     * @param command
     *            The command to add
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
        IRainbowModelOperation rcmd = null;
        if (!(command instanceof IRainbowModelOperation)) {
            // Try to use the model instanceo to turn it into an executable command
            String[] cargs = new String[1 + command.getParameters ().length];
            cargs[0] = command.getTarget ();
            for (int i = 0; i < command.getParameters ().length; i++) {
                cargs[1 + i] = command.getParameters ()[i];
            }
            rcmd = modelInstance.getCommandFactory ().generateCommand (command.getName (), cargs);
        }
        else {
            rcmd = (IRainbowModelOperation )command;
        }
//        if (!(command instanceof IRainbowModelCommand)) throw new RainbowException (MessageFormat.format ("The command {0} is not an executable command.",
//                command.getCommandName ()));
        return rcmd;
    }

    @Override
    public void requestModelUpdate (List<IRainbowOperation> commands, boolean transaction) {
        LOGGER.info (MessageFormat.format ("Updating the model with {0} commands, transaction = {1}", commands.size (),
                transaction));
        // If the command is to be executed transactionally, then add the list to the queue, otherwise add each command individually
        if (transaction) {
            commandQ.offer (commands);
        }
        else {
            for (IRainbowOperation command : commands) {
                commandQ.offer (command);
            }
        }

    }

    @Override
    public void dispose () {

    }

    @Override
    protected void log (String txt) {

    }

    @Override
    protected void runAction () {
        Object poll = commandQ.poll ();
        if (poll instanceof IRainbowOperation) {
            try {
                IRainbowOperation command = (IRainbowOperation )poll;
                IModelInstance<?> modelInstance = getModelInstance (command.getModelType (), command.getModelName ());
                m_reportingPort.info (RainbowComponentT.MODEL, MessageFormat.format (
                        "Updating model {0}::{1} through command: {2}",
                        command.getModelName (), command.getModelType (), command.getName ()));
                IRainbowModelOperation cmd = setupCommand (command, modelInstance);
                List<? extends IRainbowMessage> events;
                synchronized (modelInstance.getModelInstance ()) {
                    events = cmd.execute (modelInstance, m_changeBusPort);
                }
                if (cmd.canUndo () && events.size () > 0) {
                    // The command executed correctly if we can undo it.
                    // Announce all the changes on the the change bus
                    m_reportingPort.info (RainbowComponentT.MODEL,
                            MessageFormat.format ("Announcing {0} events on the change bus", events.size ()));
                    m_changeBusPort.announce (events);
                }
            }
            catch (IllegalStateException | RainbowException e) {
                // TODO Auto-generated catch block
                e.printStackTrace ();
            }
        }
        else if (poll instanceof List) {
            List<IRainbowOperation> commands = (List )poll;
            boolean transaction = true;
            // Keep track of successfully executed commands in case we need to undo 
            Stack<IRainbowModelOperation> executedCommands = new Stack<> ();
            // Stores the events that will be reported to the change bus
            List<IRainbowMessage> events = new LinkedList<IRainbowMessage> ();
            // Indicates whether all the commands have been executed successfully so far
            boolean complete = true;
            if (!commands.isEmpty ()) {
                IRainbowOperation c = commands.iterator ().next ();
                // The model being updated should be the same for all commands, so just grab the first one
                IModelInstance<?> modelInstance = getModelInstance (c.getModelType (), c.getModelName ());
                synchronized (modelInstance.getModelInstance ()) {

                    for (IRainbowOperation cmd : commands) {
                        try {
                            // Make sure that the model is the same 
                            IModelInstance mi = getModelInstance (cmd.getModelType (), cmd.getModelName ());
                            if (mi != modelInstance) {
                                if (transaction) {
                                    // If not the same, this is an error so log it as such an mark as incomplete
                                    complete = false;
                                    m_reportingPort.error (RainbowComponentT.MODEL,
                                            "All commands in a transaction must be on the same model.");
                                    break;
                                }
                                else {
                                    // Otherwise, just log a warning
                                    m_reportingPort.warn (RainbowComponentT.MODEL,
                                            "All commands in the transaction should be on the same model.");
                                }
                            }
                            // Make sure the command is executable, and add the ancilliary execution information
                            cmd = setupCommand (cmd, mi);
                            // If it is not executable, the throw
                            if (!(cmd instanceof IRainbowModelOperation))
                                throw new RainbowException (MessageFormat.format (
                                        "The command {0} is not an executable command.", cmd.getName ()));
                            IRainbowModelOperation mcmd = (IRainbowModelOperation )cmd;
                            // Execute the command
                            List<? extends IRainbowMessage> cmdEvents = mcmd.execute (mi, m_changeBusPort);
                            // Store all the generated events to announce later
                            events.addAll (cmdEvents);
                            // Recall what we executed in case we need to rollback
                            executedCommands.push (mcmd);
                        }
                        catch (IllegalStateException | RainbowException e) {
                            complete = false;
                            break;
                        }
                    }

                    if (!complete && transaction && !executedCommands.isEmpty ()) {
                        // Undo executed commands if we didn't complete.
                        m_reportingPort.warn (RainbowComponentT.MODEL, MessageFormat.format (
                                "Not all of the commands completed successfully. {0} did, so undoing them.",
                                executedCommands.size ()));
                        IRainbowModelOperation cmd = null;
                        while (!executedCommands.isEmpty ()) {
                            try {
                                cmd = executedCommands.pop ();
                                cmd.undo ();
                            }
                            catch (IllegalStateException | RainbowException e) {
                                LOGGER.error ("Could not undo the commands. Model could be in an inconsistent state", e);
                            }
                        }
                    }
                    else {
                        // Announce the changes
                        m_changeBusPort.announce (events);
                    }
                }
            }
        }
    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.MODEL;
    }


}
