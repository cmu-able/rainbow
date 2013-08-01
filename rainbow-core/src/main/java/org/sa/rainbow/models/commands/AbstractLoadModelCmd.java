package org.sa.rainbow.models.commands;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.IModelsManager;

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

}
