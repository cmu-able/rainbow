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
package org.sa.rainbow.model.acme;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.acme.model.event.AcmeSystemEvent;
import org.acmestudio.acme.util.EventUpdateAdapter;
import org.acmestudio.acme.util.IUpdate;
import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.models.commands.IRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of RainbowModelCommands for AcmeModelCommands
 * 
 **/

public abstract class AcmeModelOperation<T> extends AbstractRainbowModelOperation<T, IAcmeSystem> implements
IRainbowModelOperation<T, IAcmeSystem> {

    private static final String SENTINEL_COMMAND_TYPE = "___rainbow_locked";
    public static final String  PORT_PROP             = "ACME_PORT";
    public static final String  ROLE_PROP             = "ACME_ROLE";
    public static final String  SYSTEM_PROP           = "ACME_SYSTEM";
    public static final String  OUTER_PROP            = "ACME_OUTER";
    public static final String  INNER_PROP            = "ACME_INNER";
    public static final String  REPRESENTATION_PROP   = "ACME_REPRESENTATION";
    public static final String  COMPONENT_PROP        = "ACME_COMPONENT";
    public static final String  CONNECTOR_PROP        = "ACME_CONNECTOR";
    public static final String  GROUP_PROP            = "ACME_GROUP";
    public static final String  PROPERTY_PROP         = "ACME_PROPERTY";
    public static final String  BEARER_PROP           = "ACME_BEARER";
    public static final String  VALUE_PROP            = "ACME_VALUE";
    public static final String  TYPE_PROP             = "ACME_TYPE";
    public static final String  DECLARED_TYPES_PROP   = "ACME_DECLARED_TYPES";
    public static final String  INSTANTIATED_TYPES_PROP = "ACME_INSTANTIATED_TYPES";
    protected IAcmeCommand<?>   m_command;
    private final IUpdate m_eventUpdater = new IUpdate () {

        final boolean waitingForSentinel = true;

        void registerFinalEvent () {
            synchronized (this) {
                this.notifyAll ();
            }
        }

        @Override
        public void update (AcmeEvent event) {
            // Look for the sentinel event to know whether this is the last one
            if (event instanceof AcmeSystemEvent) {
                AcmeSystemEvent ase = (AcmeSystemEvent )event;
                // In undoing an "add type" command, the remove event will be sent, and so looking for this remove event
                // will catch both doing and undoing the sequence of commands.
                // Also, it should be the case that the FINISH event is not added until after we've received the
                // sentinel. See subExecute below
                if (isSentinel (ase)) {
                    // Ok, we have all events so notify everyone
                    registerFinalEvent ();
                }

            }

            // If it's not the end command, insert it in before the end command

            m_events.add (event);
            // If it's the last rainbow command event, and we've already seen the sentinel, then signal that we're done
            if (event instanceof AcmeRainbowOperationEvent
                    && !waitingForSentinel) {
                registerFinalEvent ();
            }
        }

        private boolean isSentinel ( AcmeSystemEvent ase) {
            return ase.getType () == AcmeModelEventType.REMOVE_DECLARED_TYPE /*&& ase.getData (ase.getType ()).equals (SENTINEL_COMMAND_TYPE)*/;
        }
    };

    private EventUpdateAdapter  m_eventListener;
    private final List<AcmeEvent> m_events = Collections.synchronizedList (new LinkedList<AcmeEvent> ());

    public AcmeModelOperation (String commandName, AcmeModelInstance model, String target, String... parameters) {
        super (commandName, model, target, parameters);
    }

    private void setUpEventListeners () {
        m_events.clear ();
        m_eventListener = new EventUpdateAdapter (m_eventUpdater);
        m_eventListener.addListenedTypes (EnumSet.allOf (AcmeModelEventType.class));
        getModel ().getContext ().getModel ().addEventListener (m_eventListener);
    }

    protected IAcmeSystem getModel () {
        return m_modelContext.getModelInstance ();
    }

    private void removeEventListener () {
        getModel ().getContext ().getModel ().removeEventListener (m_eventListener);
    }

    @Override
    protected void subExecute () throws RainbowException {
        List<IAcmeCommand<?>> commands = doConstructCommand ();
        if (commands.isEmpty ()) return;
        // Add a sentinel command that can be used to use to work out when all the events associated
        // with executing the actual commands are collected
        commands.add (0,
                getModel ().getCommandFactory ().systemDeclaredTypeAddCommand (getModel (), SENTINEL_COMMAND_TYPE));
        commands.add (getModel ().getCommandFactory ().systemDeclaredTypeRemoveCommand (getModel (),
                SENTINEL_COMMAND_TYPE));
        m_command = getModel ().getCommandFactory ().compoundCommand (commands);
        try {
            setUpEventListeners ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.START_COMMAND, this));

            synchronized (m_eventUpdater) {
                m_command.execute ();
                try {
                    // wait for the sentinel to come through
                    m_eventUpdater.wait ();
                }
                catch (InterruptedException e) {
                }
            }
            removeEventListener ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.FINISH_COMMAND, this));
        }
        catch ( IllegalStateException | AcmeException e) {
            m_events.clear ();
            // Need to work out how to undo partially complete commands, in a transactional way
            // Maybe look at the events that have been done so far, and undo them...?
            throw new RainbowDelegationException (e);
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        try {
            if (m_command == null) return;
            setUpEventListeners ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.START_COMMAND, this));
            synchronized (m_eventUpdater) {
                m_command.redo ();
                try {
                    m_eventUpdater.wait ();
                }
                catch (InterruptedException e) {
                }
            }
            removeEventListener ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.FINISH_COMMAND, this));
        }
        catch ( IllegalStateException | AcmeException e) {
            throw new RainbowDelegationException (e);
        }
    }

    @Override
    protected void subUndo () throws RainbowException {
        try {
            if (m_command == null) return;
            setUpEventListeners ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.START_UNDO_COMMAND, this));
            synchronized (m_eventUpdater) {
                m_command.undo ();
                try {
                    m_eventUpdater.wait ();
                }
                catch (InterruptedException e) {
                }
            }
            removeEventListener ();
            m_events.add (new AcmeRainbowOperationEvent (CommandEventT.FINISH_UNDO_COMMAND, this));

        }
        catch ( IllegalStateException | AcmeException e) {

            throw new RainbowDelegationException (e);
        }
    }

    protected abstract List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException;

    @Override
    public ModelReference getModelReference () {
        return new ModelReference (getModel ().getName (), "Acme");
    }

    @Override
    public List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        AcmeEventSerializer ser = new AcmeEventSerializer ();
        return ser.serialize (m_events, m_messageFactory);
    }



    protected boolean propertyValueChanging ( IAcmeProperty property,  IAcmePropertyValue acmeVal) {
        return !acmeVal.equals (property.getValue ());
    }

    @Override
    protected AcmeModelInstance getModelContext () {
        return (AcmeModelInstance )super.getModelContext ();
    }
}
