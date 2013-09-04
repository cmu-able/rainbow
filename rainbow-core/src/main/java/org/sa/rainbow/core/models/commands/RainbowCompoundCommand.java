package org.sa.rainbow.core.models.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ports.IRainbowMessageFactory;

// NOTE: THIS CLASS SHOULD NOT BE USED YET.
public class RainbowCompoundCommand<Model> extends AbstractRainbowModelCommand<List<Object>, Model> implements
IRainbowModelCompoundCommand<Model> {

    enum CommandState {
        CAN_EXECUTE, CAN_UNDO, CAN_REDO, ERROR
    };

    List<AbstractRainbowModelCommand<?, Model>> m_commands = new ArrayList<> ();
    CommandState                                m_state    = CommandState.CAN_EXECUTE;
    List<Object>                                m_results  = Collections.emptyList ();

    public RainbowCompoundCommand (List<AbstractRainbowModelCommand<?, Model>> commands) {
        // TODO: Do we need this?
        super ("compound", null, null);
        if (commands == null || commands.size () == 0)
            throw new IllegalArgumentException (
                    "The argument passed to the constructor for RainbowCompoundCommand cannot be null or empty.");
        for (AbstractRainbowModelCommand<?, Model> c : commands) {
            c.setCompoundCommand (this);
            if (c.m_executionState != ExecutionState.NOT_YET_DONE)
                throw new IllegalStateException (MessageFormat.format (
                        "The command {0} in the compound command has already been executed", c.getLabel ()));
            m_commands.add (c);
        }
    }

    @Override
    public boolean canExecute () {
        return (m_state == CommandState.CAN_EXECUTE);
    }

    @Override
    public boolean canRedo () {
        return (m_state == CommandState.CAN_REDO);
    }

    @Override
    public boolean canUndo () {
        return (m_state == CommandState.CAN_UNDO);
    }

    @Override
    protected void subExecute () throws RainbowException {
        if (!canExecute ())
            throw new IllegalStateException (MessageFormat.format (
                    "Cannot execute because this compound command is not in a legal state: {0}", m_state.name ()));
        List<Object> result = new ArrayList<> (m_commands.size ());
        int position = 0;
        try {
            for (; position < m_commands.size (); position++) {
                AbstractRainbowModelCommand<?, Model> command = m_commands.get (position);
                command.subExecute ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
            this.m_results = result;
        }
        catch (RainbowDelegationException | RuntimeException re) {
            // unwind this command, undoing commands already done
            position--;
            for (; position >= 0; position--) {
                try {
                    m_commands.get (position).undo ();
                }
                catch (Exception e2) {
                }
            }
            m_state = CommandState.ERROR;
            throw re;
        }
        m_state = CommandState.CAN_UNDO;

    }

    @Override
    protected void subRedo () throws RainbowException {
        if (!canRedo ())
            throw new IllegalStateException (MessageFormat.format (
                    "Cannot redo because this compound command is not in a legal state: {0}", m_state.name ()));
        List<Object> result = new ArrayList<> (m_commands.size ());
        int position = 0;
        try {
            for (; position < m_commands.size (); position++) {
                AbstractRainbowModelCommand<?, Model> command = m_commands.get (position);
                command.subRedo ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
        }
        catch (RainbowDelegationException | RuntimeException re) {
            // unwind this command, undoing commands already done
            position--;
            for (; position >= 0; position--) {
                try {
                    m_commands.get (position).undo ();
                }
                catch (Exception e2) {
                }
            }
            m_state = CommandState.ERROR;
            throw re;
        }
        m_state = CommandState.CAN_UNDO;
    }

    @Override
    protected void subUndo () throws RainbowException {
        if (!canUndo ()) throw new IllegalStateException ("This AcmeCompoundCommand is not in a legal state.");

        List<Object> result = new ArrayList<Object> (m_commands.size ());
        try {
            for (int i = m_commands.size () - 1; i >= 0; i--) {
                AbstractRainbowModelCommand<?, Model> command = m_commands.get (i);
                command.subUndo ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
        }
        catch (RainbowDelegationException | RuntimeException e) {
            m_state = CommandState.ERROR;
            throw e;
        }
        m_state = CommandState.CAN_REDO;
    }

    @Override
    public List<Object> getResults () {
        return m_results;
    }

    @Override
    public List<Object> getResult () {
        return getResults ();
    }

    @Override
    public List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModelName () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModelType () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean checkModelValidForCommand (Model model) {
        boolean ok = true;
        for (AbstractRainbowModelCommand<?, Model> cmd : m_commands) {
            ok &= cmd.checkModelValidForCommand (model);
        }
        return ok;
    }

}
