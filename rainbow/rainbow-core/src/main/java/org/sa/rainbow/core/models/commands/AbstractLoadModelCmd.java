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
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractLoadModelCmd<Type> extends AbstractRainbowModelOperation<IModelInstance<Type>, Object> {

    protected final IModelsManager m_modelsManager;
    private final InputStream m_is;
    private final String m_source;

    public AbstractLoadModelCmd (String commandName, IModelsManager mm, String resource, InputStream is, String source) {
        super (commandName, null, resource, source);
        m_is = is;
        m_modelsManager = mm;
        m_source = source;
    }

    protected void doPostExecute () throws RainbowModelException {

        if (m_modelsManager != null) {
            m_modelsManager.registerModelType (getModelReference ().getModelType ());
            getResult ().setOriginalSource (m_source);
            m_modelsManager.registerModel (getModelReference (), getResult ());
        }
    }

    protected String getOriginalSource () {
        return m_source;
    }

    protected void doPostUndo () throws RainbowModelException {
        if (m_modelsManager != null) {
            m_modelsManager.unregisterModel (getResult ());
        }
    }


    @Override
    public List<? extends IRainbowMessage> execute (IModelInstance<Object> context,
                                                    IRainbowMessageFactory messageFactory) throws
                                                                                           IllegalStateException,
                                                                                           RainbowException {
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
        List<IRainbowMessage> msgs = new LinkedList<> ();
        try {
            IRainbowMessage msg = messageFactory.createMessage ();
            msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, "LOAD_MODEL");
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "LOAD_MODEL");
            msg.setProperty (IModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IModelChangeBusPort.MODEL_NAME_PROP, getModelReference ().getModelName ());
            msg.setProperty (IModelChangeBusPort.COMMAND_PROP, getName ());
            msg.setProperty (IModelChangeBusPort.TARGET_PROP, getTarget ());
            for (int i = 0; i < getParameters ().length; i++) {
                String prop = getParameters ()[i];
                if (prop != null) {
                    msg.setProperty (IModelChangeBusPort.PARAMETER_PROP + i, prop);
                }
            }
            msgs.add (msg);
        }
        catch (RainbowException e) {
            // Should never happen - only adding strings
            e.printStackTrace ();
        }
        return msgs;
    }

    public abstract ModelReference getModelReference ();

}
