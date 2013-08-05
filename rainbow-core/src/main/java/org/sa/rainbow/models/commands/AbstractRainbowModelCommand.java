package org.sa.rainbow.models.commands;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.ports.IRainbowModelChangeBusPort;

public abstract class AbstractRainbowModelCommand<Type, Model> implements IRainbowModelCommand<Type, Model> {

    public static final String COMPOUND_COMMAND    = "compoundCommand";

    public static final String IN_COMPOUND_COMMAND = "inCompoundCommand";

    public static enum ExecutionState {
        NOT_YET_DONE, DONE, UNDONE, ERROR, DISPOSED
    };

    protected Model                       m_model;

    protected String                      m_label           = getClass ().getCanonicalName ();

    protected ExecutionState              m_executionState  = ExecutionState.NOT_YET_DONE;

    protected boolean                     inCompoundCommand = false;

    private RainbowCompoundCommand<Model> m_parentCommand   = null;

    private IRainbowModelChangeBusPort            m_announcePort;

    public IRainbowModelChangeBusPort getAnnouncePort () {
        return m_announcePort;
    }

    private String                        m_target;

    private String[]                      m_parameters;

    private final String                  m_commandName;

    public AbstractRainbowModelCommand (String commandName, Model model, String target, String... parameters) {
        m_target = target;
        m_parameters = parameters;
        m_commandName = commandName;
        setModel (model);
    }


    public Model getModel () {
        return m_model;
    }

    @Override
    public void setModel (Model m) {
        if (checkModelValidForCommand (m)) {
            m_model = m;
        }
    }

    protected abstract boolean checkModelValidForCommand (Model model);


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
        return (m_executionState == ExecutionState.NOT_YET_DONE && m_model != null);
    }

    @Override
    public boolean canUndo () {
        return (m_executionState == ExecutionState.DONE && m_model != null);
    }

    @Override
    public boolean canRedo () {
        return (m_executionState == ExecutionState.UNDONE && m_model != null);
    }

    @Override
    public Type execute (IModelInstance<Model> context) throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call execute() on a compounded command -- it must be called on the parent");
        if (!canExecute ()) throw new IllegalStateException ("This command cannot currently be executed");
        if ((m_model == null && context == null) || m_model != context.getModelInstance ())
            throw new IllegalStateException ("Trying to execute in a different context is not allowed.");
        Type t = null;
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

    @Override
    public Type redo () throws IllegalStateException, RainbowException {
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
        return t;
    }

    @Override
    public Type undo () throws IllegalStateException, RainbowException {
        if (inCompoundCommand)
            throw new IllegalStateException (
                    "Cannot call undo() on a compounded command -- it must be called on the parent");
        if (!canUndo ()) throw new IllegalStateException ("This command cannot currently be undone");

        Type t = null;
        try {
            subUndo ();
            t = getResult ();
        }
        catch (RainbowDelegationException rde) {
            m_executionState = ExecutionState.ERROR;
            throw rde;
        }
        return t;
    }

    @Override
    public void setEventAnnouncePort (IRainbowModelChangeBusPort announcePort) {
        m_announcePort = announcePort;
    }

    @Override
    public String getLabel () {
        return m_label;
    }

    protected void setLabel (String label) {
        m_label = label;
    }

    @Override
    public String getCommandName () {
        return m_commandName;
    }

    boolean isInCompoundCommand () {
        return inCompoundCommand;
    }

    void setCompoundCommand (RainbowCompoundCommand<Model> parent) {
        inCompoundCommand = true;
        m_parentCommand = parent;
    }

    RainbowCompoundCommand<Model> getParentCompound () {
        return m_parentCommand;
    }

    protected abstract void subExecute () throws RainbowException;

    protected abstract void subRedo () throws RainbowException;

    protected abstract void subUndo () throws RainbowException;

    protected abstract Type getResult ();

}
