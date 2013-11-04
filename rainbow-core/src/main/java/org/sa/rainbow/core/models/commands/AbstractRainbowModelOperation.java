package org.sa.rainbow.core.models.commands;

import java.text.MessageFormat;
import java.util.List;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public abstract class AbstractRainbowModelOperation<Type, Model> implements IRainbowModelOperation<Type, Model> {

    public static final String COMPOUND_COMMAND    = "compoundCommand";

    public static final String IN_COMPOUND_COMMAND = "inCompoundCommand";

    public static enum ExecutionState {
        NOT_YET_DONE, DONE, UNDONE, ERROR, DISPOSED
    };

    protected ExecutionState              m_executionState  = ExecutionState.NOT_YET_DONE;

    protected boolean                     inCompoundCommand = false;

    private RainbowCompoundOperation<Model> m_parentCommand   = null;

    protected IRainbowMessageFactory      m_messageFactory;

    protected IModelInstance<Model>       m_modelContext;

    private String                        m_target;

    private String[]                      m_parameters;

    private final String                  m_commandName;

    public AbstractRainbowModelOperation (String commandName, IModelInstance<Model> model, String target,
            String... parameters) {
        m_target = target;
        m_parameters = parameters;
        m_commandName = commandName;
        m_modelContext = model;
    }


    @Override
    public String getTarget () {
        return m_target;
    }

    @Override
    public String[] getParameters () {
        return m_parameters;
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
    public List<? extends IRainbowMessage>
    execute (IModelInstance<Model> context, IRainbowMessageFactory messageFactory)
            throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call execute() on a compounded command -- it must be called on the parent");
        if (!canExecute ()) throw new IllegalStateException ("This command cannot currently be executed");
        if (context == null) throw new IllegalStateException ("Trying to execute un a null model is not allowed.");
        if (messageFactory == null)
            throw new IllegalStateException ("Cannot execute a command with a null message factory");
        if (m_modelContext != null && context != m_modelContext)
            throw new IllegalStateException ("Cannot execute a command on a model that is a different context");
        if (!checkModelValidForCommand (context.getModelInstance ()))
            throw new RainbowException (MessageFormat.format ("The model is not valid for the command {0}",
                    this.toString ()));
        m_messageFactory = messageFactory;
        m_modelContext = context;
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

    @Override
    public List<? extends IRainbowMessage> redo () throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call redo() on a compounded command -- it must be called on the parent");
        if (!canRedo ()) throw new IllegalStateException ("This command cannot currently be redone");

        Type t = null;
        try {
            subRedo ();
            t = getResult ();
            m_executionState = ExecutionState.DONE;
        }
        catch (RainbowDelegationException rde) {
            m_executionState = ExecutionState.ERROR;
            throw rde;
        }
        return getGeneratedEvents (m_messageFactory);
    }

    protected abstract List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory);

    @Override
    public List<? extends IRainbowMessage> undo () throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call undo() on a compounded command -- it must be called on the parent");
        if (!canUndo ()) throw new IllegalStateException ("This command cannot currently be undone");

        try {
            subUndo ();
        }
        catch (RainbowDelegationException rde) {
            m_executionState = ExecutionState.ERROR;
            throw rde;
        }
        return getGeneratedEvents (m_messageFactory);
    }


    @Override
    public String getName () {
        return m_commandName;
    }

    boolean isInCompoundCommand () {
        return inCompoundCommand;
    }

    void setCompoundCommand (RainbowCompoundOperation<Model> parent) {
        inCompoundCommand = true;
        m_parentCommand = parent;
    }

    RainbowCompoundOperation<Model> getParentCompound () {
        return m_parentCommand;
    }

    protected abstract void subExecute () throws RainbowException;

    protected abstract void subRedo () throws RainbowException;

    protected abstract void subUndo () throws RainbowException;


    protected abstract boolean checkModelValidForCommand (Model model);

    @Override
    public String toString () {
        return MessageFormat.format ("Command<{0}: {1}, {2}>", getName (), getTarget (), getParameters ()
                .toString ());
    }

    protected IModelInstance<Model> getModelContext () {
        return m_modelContext;
    }

}
