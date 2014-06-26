package org.sa.rainbow.core.models.commands;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public abstract class AbstractSaveModelCmd<Type> extends AbstractRainbowModelOperation<Object, Type> {

    private OutputStream   m_os;
    private IModelsManager m_modelsManager;
    private String         m_source;

    public AbstractSaveModelCmd (String commandName, IModelsManager mm, String resource, OutputStream os, String source) {
        super (commandName, null, resource, source);
        m_modelsManager = mm;
        m_os = os;
        m_source = source;
    }

    @Override
    public List<? extends IRainbowMessage>
    execute (IModelInstance<Type> context,
            IRainbowMessageFactory messageFactory) throws IllegalStateException, RainbowException {
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
        if (messageFactory == null) return Collections.<IRainbowMessage> emptyList ();
        return getGeneratedEvents (m_messageFactory);
    }

    public String getOriginalSource () {
        return m_source;
    }

    public OutputStream getStream () {
        return m_os;
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
        return Collections.<IRainbowMessage> emptyList ();
    }
}
