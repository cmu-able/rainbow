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

import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RainbowCompoundOperation<Model> extends AbstractRainbowModelOperation<List<Object>, Model> implements
IRainbowModelCompoundCommand<Model> {

    enum CommandState {
        CAN_EXECUTE, CAN_UNDO, CAN_REDO, ERROR
    }

    final List<AbstractRainbowModelOperation<?, Model>> m_commands = new ArrayList<> ();
    CommandState                                m_state    = CommandState.CAN_EXECUTE;
    List<Object>                                m_results  = Collections.emptyList ();

    public RainbowCompoundOperation (List<AbstractRainbowModelOperation<?, Model>> commands) {
        // TODO: Do we need this?
        super ("compound", null, null);
        if (commands == null || commands.size () == 0)
            throw new IllegalArgumentException (
                    "The argument passed to the constructor for RainbowCompoundOperation cannot be null or empty.");
        for (AbstractRainbowModelOperation<?, Model> c : commands) {
            c.setCompoundCommand (this);
            if (c.m_executionState != ExecutionState.NOT_YET_DONE)
                throw new IllegalStateException (MessageFormat.format (
                        "The command {0} in the compound operation has already been executed", c.getName ()));
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
                    "Cannot execute because this compound operation is not in a legal state: {0}", m_state.name ()));
        List<Object> result = new ArrayList<> (m_commands.size ());
        int position = 0;
        try {
            for (; position < m_commands.size (); position++) {
                AbstractRainbowModelOperation<?, Model> command = m_commands.get (position);
                command.subExecute ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
            this.m_results = result;
        } catch (RainbowDelegationException | RuntimeException re) {
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
                AbstractRainbowModelOperation<?, Model> command = m_commands.get (position);
                command.subRedo ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
        } catch (RainbowDelegationException | RuntimeException re) {
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

        List<Object> result = new ArrayList<> (m_commands.size ());
        try {
            for (int i = m_commands.size () - 1; i >= 0; i--) {
                AbstractRainbowModelOperation<?, Model> command = m_commands.get (i);
                command.subUndo ();
                Object o = command.getResult ();
                if (o != null) {
                    result.add (o);
                }
            }
            m_results = result;
        } catch (RainbowDelegationException | RuntimeException e) {
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
    protected boolean checkModelValidForCommand (Model model) {
        boolean ok = true;
        for (AbstractRainbowModelOperation<?, Model> cmd : m_commands) {
            ok &= cmd.checkModelValidForCommand (model);
        }
        return ok;
    }

    @Override
    public String getOrigin () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return null;
    }

}
