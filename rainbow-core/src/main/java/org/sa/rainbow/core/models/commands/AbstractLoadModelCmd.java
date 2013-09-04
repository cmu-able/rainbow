package org.sa.rainbow.core.models.commands;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.models.ports.IRainbowModelChangeBusPort;

public abstract class AbstractLoadModelCmd<Type> extends AbstractRainbowModelCommand<IModelInstance<Type>, Object> {

    private IModelsManager m_modelsManager;
    private InputStream    m_is;

    public AbstractLoadModelCmd (String commandName, IModelsManager mm, String resource, InputStream is, String source) {
        super (commandName, null, resource, source);
        m_is = is;
        m_modelsManager = mm;
    }

    protected void doPostExecute () throws RainbowModelException {
        if (m_modelsManager != null) {
            m_modelsManager.registerModelType (getModelType ());
            m_modelsManager.registerModel (getModelType (), getModelName (), getResult ());
        }
    }

    protected void doPostUndo () throws RainbowModelException {
        if (m_modelsManager != null) {
            m_modelsManager.unregisterModel (getResult ());
        }
    }

    @Override
    public List<? extends IRainbowMessage> execute (IModelInstance context, IRainbowMessageFactory messageFactory) throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call execute() on a compounded command -- it must be called on the parent");
        if (!canExecute ()) throw new IllegalStateException ("This command cannot currently be executed");
        m_modelContext = context;
        m_messageFactory = messageFactory;
        try {
            subExecute ();
        }
        catch (RainbowDelegationException rde) {
            m_executionState = ExecutionState.ERROR;
            throw rde;
        }
        m_executionState = ExecutionState.DONE;
        return getGeneratedEvents (m_messageFactory);
    }

    public InputStream getStream () {
        return m_is;
    }

    @Override
    public boolean canExecute () {
        return (m_executionState == ExecutionState.NOT_YET_DONE);
    }

    @Override
    public boolean canUndo () {
        return (m_executionState == ExecutionState.DONE);
    }

    @Override
    public boolean canRedo () {
        return (m_executionState == ExecutionState.UNDONE);
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        List<IRainbowMessage> msgs = new LinkedList<IRainbowMessage> ();
        try {
            IRainbowMessage msg = messageFactory.createMessage ();
            msg.setProperty (IRainbowModelChangeBusPort.EVENT_TYPE_PROP, "LOAD_MODEL");
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "LOAD_MODEL");
            msg.setProperty (IRainbowModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IRainbowModelChangeBusPort.MODEL_NAME_PROP, getModelName ());
            msg.setProperty (IRainbowModelChangeBusPort.COMMAND_PROP, getCommandName ());
            msg.setProperty (IRainbowModelChangeBusPort.TARGET_PROP, getTarget ());
            for (int i = 0; i < getParameters ().length; i++) {
                msg.setProperty (IRainbowModelChangeBusPort.PARAMETER_PROP + i, getParameters ()[i]);
            }
            msgs.add (msg);
        }
        catch (RainbowException e) {
            // Should never happen - only adding strings
            e.printStackTrace ();
        }
        return msgs;
    }

}
