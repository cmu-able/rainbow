package org.sa.rainbow.models.commands;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.IModelsManager;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

public abstract class AbstractLoadModelCmd<Type> extends AbstractRainbowModelCommand<IModelInstance<Type>, Object> {

    private IModelsManager m_modelsManager;
    private InputStream    m_is;

    public AbstractLoadModelCmd (String commandName, IModelsManager mm, String resource, InputStream is, String source) {
        super (commandName, mm, resource, source);
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
    public IModelInstance<Type> execute (IModelInstance context) throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call execute() on a compounded command -- it must be called on the parent");
        if (!canExecute ()) throw new IllegalStateException ("This command cannot currently be executed");
        IModelInstance<Type> t = null;
        try {
            subExecute ();
            t = getResult ();
        }
        catch (RainbowDelegationException rde) {
            m_executionState = ExecutionState.ERROR;
            throw rde;
        }
        m_executionState = ExecutionState.DONE;
        return t;
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
    public List<? extends IRainbowMessage> getGeneratedEvents () {
        List<IRainbowMessage> msgs = new LinkedList<IRainbowMessage> ();
        try {
            IRainbowMessage msg = getAnnouncePort ().createMessage ();
            msg.setProperty (IRainbowModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IRainbowModelChangeBusPort.COMMAND_PROP, getCommandName ());
            msg.setProperty (IRainbowModelChangeBusPort.TARGET_PROP, getTarget ());
            msg.setProperty (IRainbowModelChangeBusPort.MODEL_NAME_PROP, getModelName ());
            for (int i = 0; i < getParameters ().length; i++) {
                msg.setProperty (IRainbowModelChangeBusPort.PARAMETER_PROP + i, getParameters ()[i]);
            }
            msg.setProperty (IRainbowModelChangeBusPort.EVENT_TYPE_PROP, "Load");
            msgs.add (msg);
        }
        catch (RainbowException e) {
            // Should not happen
            e.printStackTrace ();
        }
        return msgs;
    }

}
