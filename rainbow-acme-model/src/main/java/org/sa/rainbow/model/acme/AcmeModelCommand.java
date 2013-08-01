package org.sa.rainbow.model.acme;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.acme.util.EventUpdateAdapter;
import org.acmestudio.acme.util.IUpdate;
import org.sa.rainbow.core.error.RainbowDelegationException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.model.acme.AcmeRainbowCommandEvent.CommandEventT;
import org.sa.rainbow.models.commands.AbstractRainbowModelCommand;
import org.sa.rainbow.models.commands.IRainbowModelCommand;

public abstract class AcmeModelCommand<T> extends AbstractRainbowModelCommand<T, IAcmeSystem> implements
IRainbowModelCommand<T, IAcmeSystem> {

    public static final String PORT_PROP           = "ACME_PORT";
    public static final String ROLE_PROP           = "ACME_ROLE";
    public static final String SYSTEM_PROP         = "ACME_SYSTEM";
    public static final String OUTER_PROP          = "ACME_OUTER";
    public static final String INNER_PROP          = "ACME_INNER";
    public static final String REPRESENTATION_PROP = "ACME_REPRESENTATION";
    public static final String COMPONENT_PROP      = "ACME_COMPONENT";
    public static final String CONNECTOR_PROP      = "ACME_CONNECTOR";
    public static final String GROUP_PROP          = "ACME_GROUP";
    public static final String PROPERTY_PROP       = "ACME_PROPERTY";
    public static final String BEARER_PROP         = "ACME_BEARER";
    public static final String VALUE_PROP          = "ACME_VALUE";
    public static final String TYPE_PROP           = "ACME_TYPE";
    protected IAcmeCommand<?>  m_command;
    private IUpdate            m_eventUpdater = new IUpdate () {

        @Override
        public void update (AcmeEvent event) {
            if (m_events.get (m_events.size () - 1) instanceof AcmeRainbowCommandEvent
                    && ((AcmeRainbowCommandEvent )m_events.get (m_events
                            .size () - 1)).getEventType ().isEnd ()) {
                m_events.add (m_events.size () - 1, event);
            }
            else {
                m_events.add (event);
            }
        }
    };
    private EventUpdateAdapter m_eventListener;
    List<AcmeEvent>            m_events       = Collections.synchronizedList (new LinkedList<AcmeEvent> ());

    public AcmeModelCommand (String commandName, IAcmeSystem model, String target, String... parameters) {
        super (commandName, model, target, parameters);
    }

    protected void setUpEventListeners () {
        m_events.clear ();
        m_eventListener = new EventUpdateAdapter (m_eventUpdater);
        m_eventListener.addListenedTypes (EnumSet.allOf (AcmeModelEventType.class));
        getModel ().getContext ().getModel ().addEventListener (m_eventListener);
    }

    protected void removeEventListener () {
        getModel ().removeEventListener (m_eventListener);
    }

    @Override
    protected void subExecute () throws RainbowException {
        doConstructCommand ();
        synchronized (m_model) {
            try {
                setUpEventListeners ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.START_COMMAND, this));
                m_command.execute ();
                removeEventListener ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.FINISH_COMMAND, this));
            }
            catch (IllegalStateException | AcmeException e) {
                m_events.clear ();
                // Need to work out how to undo partially complete commands, in a transactional way
                // Maybe look at the events that have been done so far, and undo them...?
                throw new RainbowDelegationException (e);
            }
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        try {
            synchronized (m_model) {
                setUpEventListeners ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.START_COMMAND, this));
                m_command.redo ();
                removeEventListener ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.FINISH_COMMAND, this));
            }
        }
        catch (IllegalStateException | AcmeException e) {
            throw new RainbowDelegationException (e);
        }
    }

    @Override
    protected void subUndo () throws RainbowException {
        try {
            synchronized (m_model) {
                setUpEventListeners ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.START_UNDO_COMMAND, this));
                m_command.undo ();
                removeEventListener ();
                m_events.add (new AcmeRainbowCommandEvent (CommandEventT.FINISH_UNDO_COMMAND, this));

            }
        }
        catch (IllegalStateException | AcmeException e) {

            throw new RainbowDelegationException (e);
        }
    }

    protected abstract void doConstructCommand () throws RainbowModelException;

    @Override
    public String getModelName () {
        return m_model.getName ();
    }

    @Override
    public String getModelType () {
        return "Acme";
    }

    @Override
    public List<? extends IRainbowMessage> getGeneratedEvents () {
//        LinkedList<IRainbowMessage> msgs = new LinkedList<> ();
//        for (AcmeEvent event : m_events) {
//            IRainbowMessage msg = getAnnouncePort ().createMessage ();
//            AcmeEventSerializer ser = 
//        }
        AcmeEventSerializer ser = new AcmeEventSerializer ();
        return ser.serialize (m_events, getAnnouncePort ());
    }

    protected <T> T resolveInModel (String qname, Class<T> clazz) throws RainbowModelException {
        // The model is an Acme System, but the qname could include the Acme system.
        String[] names = qname.split ("\\.");
        if (names[0].equals (getModel ().getName ())) {
            qname = qname.substring (qname.indexOf ('.') + 1);
        }
        Object resolve = getModel ().lookupName (qname);
        if (resolve == null || !(clazz.isInstance (resolve)))
            throw new RainbowModelException (MessageFormat.format ("Cannot find the ''{0}'' in the model as a {1}",
                    qname, clazz.getName ()));

        T lb = (T )resolve;
        return lb;
    }
}
