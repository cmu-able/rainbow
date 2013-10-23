package org.sa.rainbow.model.acme;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.acmestudio.acme.core.IAcmeNamedObject;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.element.IAcmeReference;
import org.acmestudio.acme.model.event.AcmeAttachmentEvent;
import org.acmestudio.acme.model.event.AcmeBindingEvent;
import org.acmestudio.acme.model.event.AcmeComponentEvent;
import org.acmestudio.acme.model.event.AcmeConnectorEvent;
import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeGroupEvent;
import org.acmestudio.acme.model.event.AcmePortEvent;
import org.acmestudio.acme.model.event.AcmePropertyEvent;
import org.acmestudio.acme.model.event.AcmeRoleEvent;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

/**
 * Serializes Acme events as Rainbow events.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class AcmeEventSerializer {
    public void serialize (AcmeEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        if (event instanceof AcmeAttachmentEvent) {
            serialize ((AcmeAttachmentEvent )event, msg, parent);
        }
        if (event instanceof AcmeBindingEvent) {
            serialize ((AcmeBindingEvent )event, msg, parent);
        }
        if (event instanceof AcmeComponentEvent) {
            serialize ((AcmeComponentEvent )event, msg, parent);
        }
        if (event instanceof AcmeConnectorEvent) {
            serialize ((AcmeConnectorEvent )event, msg, parent);
        }
        if (event instanceof AcmeGroupEvent) {
            serialize ((AcmeGroupEvent )event, msg, parent);
        }
        if (event instanceof AcmePortEvent) {
            serialize ((AcmePortEvent )event, msg, parent);
        }
        if (event instanceof AcmePropertyEvent) {
            serialize ((AcmePropertyEvent )event, msg, parent);
        }
        if (event instanceof AcmeRoleEvent) {
            serialize ((AcmeRoleEvent )event, msg, parent);
        }
        if (event instanceof AcmeRainbowCommandEvent) {
            serialize ((AcmeRainbowCommandEvent )event, msg, parent);
        }

    }

    public void serialize (AcmeRainbowCommandEvent event, IRainbowMessage msg, IRainbowMessage parent) {

        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, event.getEventType ().name ());
            msg.setProperty (IModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IModelChangeBusPort.COMMAND_PROP, event.getCommand ().getCommandName ());
            msg.setProperty (IModelChangeBusPort.TARGET_PROP, event.getCommand ().getTarget ());
            msg.setProperty (IModelChangeBusPort.MODEL_NAME_PROP, event.getCommand ().getModelName ());
            msg.setProperty (IModelChangeBusPort.MODEL_TYPE_PROP, event.getCommand ().getModelType ());
            for (int i = 0; i < event.getCommand ().getParameters ().length; i++) {
                msg.setProperty (IModelChangeBusPort.PARAMETER_PROP + i, event.getCommand ().getParameters ()[i]);
            }
        }
        catch (RainbowException e) {
            // Should never happen
        }
    }

    private void addCommonProperties (AcmeEvent event, IRainbowMessage msg, IRainbowMessage parent)
            throws RainbowException {
        msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, event.getType ().name ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "MODEL_CHANGE");
        if (parent != null) {
            msg.setProperty (IModelChangeBusPort.PARENT_ID_PROP, parent.getProperty (IModelChangeBusPort.ID_PROP));
        }
    }

    public void serialize (AcmeAttachmentEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.PORT_PROP, event.getAttachment ().getReferencedPortName ());
            msg.setProperty (AcmeModelCommand.ROLE_PROP, event.getAttachment ().getReferencedRoleName ());
            msg.setProperty (AcmeModelCommand.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    public void serialize (AcmeBindingEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.OUTER_PROP, event.getBinding ().getOuterReference ().getReferencedName ());
            msg.setProperty (AcmeModelCommand.INNER_PROP, event.getBinding ().getInnerReference ().getReferencedName ());
            msg.setProperty (AcmeModelCommand.REPRESENTATION_PROP, event.getRepresentation ().getQualifiedName ());
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }

    }

    public void serialize (AcmeComponentEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.COMPONENT_PROP, event.getComponent ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelCommand.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event, msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    public void serialize (AcmeConnectorEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.CONNECTOR_PROP, event.getConnector ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelCommand.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event, msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    public void serialize (AcmeGroupEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.GROUP_PROP, event.getGroup ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelCommand.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event, msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    public void serialize (AcmePortEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.PORT_PROP, event.getPort ().getQualifiedName ());
            if (event.getComponent () != null) {
                msg.setProperty (AcmeModelCommand.COMPONENT_PROP, event.getComponent ().getQualifiedName ());
            }
            addTypeInformation (event, msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    public void serialize (AcmePropertyEvent event, IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.PROPERTY_PROP, event.getProperty ().getQualifiedName ());
            if (event.getPropertyBearer () instanceof IAcmeNamedObject) {
                msg.setProperty (AcmeModelCommand.BEARER_PROP,
                        ((IAcmeNamedObject )event.getPropertyBearer ()).getQualifiedName ());
            }

            switch (event.getType ()) {
            case SET_PROPERTY_VALUE:
                try {
                    msg.setProperty (AcmeModelCommand.VALUE_PROP, StandaloneLanguagePackHelper.defaultLanguageHelper ()
                            .propertyValueToString (event.getProperty ().getValue (), new RegionManager ()));

                }
                catch (Exception e) {
                }
            }
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }


    public void serialize (AcmeRoleEvent event, IRainbowMessage msg, IRainbowMessage parent) {

        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelCommand.ROLE_PROP, event.getRole ().getQualifiedName ());
            if (event.getConnector () != null) {
                msg.setProperty (AcmeModelCommand.CONNECTOR_PROP, event.getConnector ().getQualifiedName ());
            }
            addTypeInformation (event, msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    private void addTypeInformation (AcmeEvent event, IRainbowMessage msg) throws RainbowException {
        switch (event.getType ()) {
        case ASSIGN_DECLARED_TYPE:
        case ASSIGN_INSTANTIATED_TYPE:
        case REMOVE_DECLARED_TYPE:
        case REMOVE_INSTANTIATED_TYPE:
            if (event.getData (event.getType ()) != null) {
                msg.setProperty (AcmeModelCommand.TYPE_PROP, (((IAcmeReference )event.getData (event.getType ()))).getReferencedName ());
            }

        }
    }

    public List<IRainbowMessage> serialize (List<? extends AcmeEvent> events, IRainbowMessageFactory port) {
        try {
            List<IRainbowMessage> msgs = new LinkedList<> ();
            IRainbowMessage parent = null;
            Iterator<? extends AcmeEvent> iterator = events.iterator ();
            if (events.size () == 0) return msgs;
            if (events.get (0) instanceof AcmeRainbowCommandEvent) {
                parent = port.createMessage ();
                serialize (iterator.next (), parent, null);
                msgs.add (parent);
            }
            while (iterator.hasNext ()) {
                AcmeEvent e = iterator.next ();
                IRainbowMessage msg = port.createMessage ();
                serialize (e, msg, parent);
                if (msg.getPropertyNames ().contains (ESEBConstants.MSG_TYPE_KEY)) {
                    msgs.add (msg);
                }
            }
            return msgs;
        }catch (ConcurrentModificationException e) {
            // There could have been some stray events that got added to the list, so let's just try processing again
            return serialize (events, port);
        }
    }

}
