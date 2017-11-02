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
package org.sa.rainbow.core.models.commands;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

public abstract class AbstractRainbowModelOperation<Type, Model> implements IRainbowModelOperation<Type, Model> {

    public static final String COMPOUND_COMMAND    = "compoundCommand";

    public static final String IN_COMPOUND_COMMAND = "inCompoundCommand";

    public enum ExecutionState {
        NOT_YET_DONE, DONE, UNDONE, ERROR, DISPOSED
    }

    ExecutionState m_executionState = ExecutionState.NOT_YET_DONE;

    boolean inCompoundCommand = false;

    private RainbowCompoundOperation<Model> m_parentCommand   = null;

    protected IRainbowMessageFactory      m_messageFactory;

    protected IModelInstance<Model>       m_modelContext;

    private final String m_target;

    private final String[] m_parameters;

    private final String                  m_commandName;

    private String                          m_origin;

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
    public String getOrigin () {
        return m_origin;
    }

    @Override
    public void setOrigin (String o) {
        m_origin = o;
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
        catch (Throwable e) {
            m_executionState = ExecutionState.ERROR;
            throw new RainbowDelegationException (e);
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

        try {
            subRedo ();
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
        return MessageFormat.format ("O[{0}:{1}/{2}.{3}({4})]{5}", getModelReference ().getModelName (),
                getModelReference ().getModelType (), getName (),
                m_target, m_parameters == null ? "" : Arrays.toString (m_parameters), m_origin == null ? ""
                        : ("<" + m_origin));
    }

    protected IModelInstance<Model> getModelContext () {
        return m_modelContext;
    }


    protected List<? extends IRainbowMessage> generateEvents (IRainbowMessageFactory messageFactory, String eventType) {
        try {
            IRainbowMessage msg = messageFactory.createMessage ();
            msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, eventType);
            msg.setProperty (IModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IModelChangeBusPort.COMMAND_PROP, getName ());
            msg.setProperty (IModelChangeBusPort.TARGET_PROP, getTarget ());
            msg.setProperty (IModelChangeBusPort.MODEL_NAME_PROP, getModelReference ().getModelName ());
            msg.setProperty (IModelChangeBusPort.MODEL_TYPE_PROP, getModelReference ().getModelType ());
            for (int i = 0; i < getParameters ().length; i++) {
                msg.setProperty (IModelChangeBusPort.PARAMETER_PROP + Integer.toString (i), getParameters ()[i]);
            }
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "MODEL_CHANGE");
            List<IRainbowMessage> events = new LinkedList<> ();
            events.add (msg);
            return events;
        }
        catch (RainbowException e) {
        }
        return null;
    }


    @Override
    public ModelReference getModelReference () {
        return new ModelReference (getModelContext ().getModelName (), getModelContext ().getModelType ());
    }


}
