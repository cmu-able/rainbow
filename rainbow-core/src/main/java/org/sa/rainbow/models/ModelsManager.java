package org.sa.rainbow.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.models.commands.IRainbowModelCommand;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;
import org.sa.rainbow.models.ports.eseb.ESEBChangeBusAnnouncePort;
import org.sa.rainbow.models.ports.eseb.ESEBModelManagerModelUpdatePort;
import org.sa.rainbow.util.Util;

/**
 * The Models manager is a component that provides access to the models in Rainbow. It should also contain: - a
 * reference to the model update US bus, as a subscriber to events to get information - a reference to the model change
 * bus, as a publisher - all executed commands should publish events to this bus
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class ModelsManager implements IModelsManager {
    static Logger                                      LOGGER     = Logger.getLogger (ModelsManager.class);

    protected IRainbowModelChangeBusPort                    m_changeBusPort;
    protected IRainbowModelUSBusPort                   m_upstreamBusPort;

    /** Contains all the models -- keyed by Type then name **/
    protected Map<String, Map<String, IModelInstance>> m_modelMap = new HashMap<> ();

    public ModelsManager () {
    }

    public void initialize () throws IOException {
        initializeConnections ();
        initializeModels ();
    }

    private void initializeModels () {
        String numberOfModelsStr = Rainbow.properties ().getProperty (Rainbow.PROPKEY_MODEL_NUMBER, "0");
        int numberOfModels = Integer.parseInt (numberOfModelsStr);
        for (int modelNum = 0; modelNum < numberOfModels; modelNum++) {
            String factoryClassName = Rainbow.properties ().getProperty (
                    Rainbow.PROPKEY_MODEL_LOAD_CLASS_PREFIX + modelNum);
            String modelName = Rainbow.properties ().getProperty (Rainbow.PROPKEY_MODEL_NAME_PREFIX + modelNum);
            String path = Rainbow.properties ().getProperty (Rainbow.PROPKEY_MODEL_PATH_PREFIX + modelNum);
            File modelPath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (), path);
            try {
                Class loadClass = Class.forName (factoryClassName);
                Method method = loadClass.getMethod ("loadCommand", ModelsManager.class, String.class,
                        InputStream.class, String.class);
                if (!Modifier.isStatic (method.getModifiers ()) || method.getDeclaringClass () != loadClass) 
                    throw new UnsupportedOperationException (MessageFormat
                            .format (
                                    "The class {0} does not implement a static method loadCommand, used to generate modelInstances",
                                    method.getDeclaringClass ().getCanonicalName ()));
                AbstractLoadModelCmd load = (AbstractLoadModelCmd )method.invoke (null, this, modelName,
                        new FileInputStream (modelPath), modelPath.getAbsolutePath ());
                IModelInstance instance = load.execute (null);
                LOGGER.info ("Successfully loaded and registered " + instance.getModelName () + ":"
                        + instance.getModelType ());
                // m_changePort.executedCommand (load);
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

    private void initializeConnections () throws IOException {
        // This needs to be done via a factory
//        m_changeBusPort = RainbowManagementPortFactory.createChangeBusPort (this);
        m_changeBusPort = new ESEBChangeBusAnnouncePort ();
        m_upstreamBusPort = new ESEBModelManagerModelUpdatePort (this);
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
    public <T> IModelInstance<T> copyInstance (String modelType, String modelName, String copyName)
            throws RainbowModelException {
        Map<String, IModelInstance> models = m_modelMap.get (modelType);
        if (models != null) {
            IModelInstance<T> model = models.get (modelName);
            if (model != null) {
                if (models.get (copyName) == null) {
                    try {
                        IModelInstance<T> copy = model.copyModelInstance (copyName);
                        registerModel (modelType, copyName, copy);
                        return copy;
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
            models.put (modelName, model);
//            model.setChangePort (m_changeBusPort);
            // TODO: attach the change bus port to the model
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
            success = unregisterModel (models, model);
        }
        else {
            // do it the slow way
            for (Map<String, IModelInstance> m : m_modelMap.values ()) {
                success = unregisterModel (m, model);
                if (success) {
                    break;
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
                deleted = true;
            }
        }
        return deleted;
    }

    @Override
    public void requestModelUpdate (IRainbowModelCommandRepresentation command) throws IllegalStateException,
            RainbowException {
        IModelInstance<?> modelInstance = getModelInstance (command.getModelType (), command.getModelName ());
        if (!(command instanceof IRainbowModelCommand))
            throw new RainbowException (MessageFormat.format ("The command {0} is not an executable command.", command.getCommandName ()));
        IRainbowModelCommand cmd = (IRainbowModelCommand )command;
        cmd.setModel (modelInstance.getModelInstance ());
        cmd.setEventAnnouncePort (m_changeBusPort);
        cmd.execute (modelInstance);
        if (cmd.canUndo ()) {
            m_changeBusPort.announce (cmd.getGeneratedEvents ());
        }
    }

    @Override
    public void requestModelUpdate (List<IRainbowModelCommandRepresentation> commands, boolean transaction) {
        Stack<IRainbowModelCommand> executedCommands = new Stack<> ();
        boolean complete = true;
        for (IRainbowModelCommandRepresentation cmd : commands) {
            try {
                if (!(cmd instanceof IRainbowModelCommand))
                    throw new RainbowException (MessageFormat.format ("The command {0} is not an executable command.", cmd.getCommandName ()));
                requestModelUpdate (cmd);
                executedCommands.push ((IRainbowModelCommand )cmd);
            }
            catch (IllegalStateException | RainbowException e) {
                complete = false;
                break;
            }
        }
        if (!complete && transaction && !executedCommands.isEmpty ()) {
            IRainbowModelCommand cmd = null;
            while (!executedCommands.isEmpty ()) {
                try {
                    cmd = executedCommands.pop ();
                    cmd.undo ();
                    m_changeBusPort.announce (cmd.getGeneratedEvents ());
                }
                catch (IllegalStateException | RainbowException e) {
                    LOGGER.error ("Could not undo the commands.", e);
                }
            }
        }
       
    }

}
