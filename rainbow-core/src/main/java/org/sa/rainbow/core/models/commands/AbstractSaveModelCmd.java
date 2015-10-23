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
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSaveModelCmd<Type> extends AbstractRainbowModelOperation<Object, Type> {

    private final OutputStream m_os;
    private final String m_source;

    public AbstractSaveModelCmd (String commandName, IModelsManager mm, String resource, OutputStream os, String source) {
        super (commandName, null, resource, source);
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
