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

import org.acmestudio.acme.core.IAcmeNamedObject;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementTypeRef;
import org.acmestudio.acme.model.event.*;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

import java.util.*;

/**
 * Serializes Acme events as Rainbow events.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
class AcmeEventSerializer {
    private void serialize (AcmeEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
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
        if (event instanceof AcmeRainbowOperationEvent) {
            serialize ((AcmeRainbowOperationEvent )event, msg, parent);
        }

    }

    private void serialize ( AcmeRainbowOperationEvent event,  IRainbowMessage msg, IRainbowMessage parent) {

        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, event.getEventType ().name ());
            msg.setProperty (IModelChangeBusPort.ID_PROP, UUID.randomUUID ().toString ());
            msg.setProperty (IModelChangeBusPort.COMMAND_PROP, event.getCommand ().getName ());
            msg.setProperty (IModelChangeBusPort.TARGET_PROP, event.getCommand ().getTarget ());
            msg.setProperty (IModelChangeBusPort.MODEL_NAME_PROP, event.getCommand ().getModelReference ()
                    .getModelName ());
            msg.setProperty (IModelChangeBusPort.MODEL_TYPE_PROP, event.getCommand ().getModelReference ()
                    .getModelType ());
            for (int i = 0; i < event.getCommand ().getParameters ().length; i++) {
                msg.setProperty (IModelChangeBusPort.PARAMETER_PROP + i, event.getCommand ().getParameters ()[i]);
            }
        }
        catch (RainbowException e) {
            // Should never happen
        }
    }

    private void addCommonProperties ( AcmeEvent event,  IRainbowMessage msg,  IRainbowMessage parent)
            throws RainbowException {
        msg.setProperty (IModelChangeBusPort.EVENT_TYPE_PROP, event.getType ().name ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, "MODEL_CHANGE");
        if (parent != null) {
            msg.setProperty (IModelChangeBusPort.PARENT_ID_PROP, parent.getProperty (IModelChangeBusPort.ID_PROP));
            msg.setProperty (IModelChangeBusPort.MODEL_NAME_PROP,
                    parent.getProperty (IModelChangeBusPort.MODEL_NAME_PROP));
            msg.setProperty (IModelChangeBusPort.MODEL_TYPE_PROP,
                    parent.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP));
        }
    }

    private void serialize ( AcmeAttachmentEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.PORT_PROP, event.getAttachment ().getReferencedPortName ());
            msg.setProperty (AcmeModelOperation.ROLE_PROP, event.getAttachment ().getReferencedRoleName ());
            msg.setProperty (AcmeModelOperation.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    private void serialize ( AcmeBindingEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.OUTER_PROP, event.getBinding ().getOuterReference ().getReferencedName ());
            msg.setProperty (AcmeModelOperation.INNER_PROP, event.getBinding ().getInnerReference ().getReferencedName ());
            msg.setProperty (AcmeModelOperation.REPRESENTATION_PROP, event.getRepresentation ().getQualifiedName ());
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }

    }

    private void serialize ( AcmeComponentEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.COMPONENT_PROP, event.getComponent ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelOperation.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event.getComponent (), msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace();
        }
    }

    private void serialize ( AcmeConnectorEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.CONNECTOR_PROP, event.getConnector ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelOperation.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event.getConnector (), msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    private void serialize ( AcmeGroupEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.GROUP_PROP, event.getGroup ().getQualifiedName ());
            if (event.getSystem () != null) {
                msg.setProperty (AcmeModelOperation.SYSTEM_PROP, event.getSystem ().getQualifiedName ());
            }
            addTypeInformation (event.getGroup (), msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    private void serialize ( AcmePortEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.PORT_PROP, event.getPort ().getQualifiedName ());
            if (event.getComponent () != null) {
                msg.setProperty (AcmeModelOperation.COMPONENT_PROP, event.getComponent ().getQualifiedName ());
            }
            addTypeInformation (event.getPort (), msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    private void serialize ( AcmePropertyEvent event,  IRainbowMessage msg, IRainbowMessage parent) {
        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.PROPERTY_PROP, event.getProperty ().getQualifiedName ());
            if (event.getPropertyBearer () instanceof IAcmeNamedObject) {
                msg.setProperty (AcmeModelOperation.BEARER_PROP,
                        ((IAcmeNamedObject )event.getPropertyBearer ()).getQualifiedName ());
            }

            switch (event.getType ()) {
            case SET_PROPERTY_VALUE:
                try {
                    msg.setProperty (AcmeModelOperation.VALUE_PROP, StandaloneLanguagePackHelper.defaultLanguageHelper ()
                            .propertyValueToString (event.getProperty ().getValue (), new RegionManager ()));

                }
                catch (Exception e) {
                }
            case ADD_PROPERTY:
                if (event.getProperty ().getValue () != null) {
                    try {
                        msg.setProperty (AcmeModelOperation.VALUE_PROP,
                                StandaloneLanguagePackHelper.defaultLanguageHelper ().propertyValueToString (
                                        event.getProperty ().getValue (), new RegionManager ()));
                    }
                    catch (Exception e) {
                    }

                }
            default:

                break;
            }
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }


    private void serialize ( AcmeRoleEvent event,  IRainbowMessage msg, IRainbowMessage parent) {

        try {
            addCommonProperties (event, msg, parent);
            msg.setProperty (AcmeModelOperation.ROLE_PROP, event.getRole ().getQualifiedName ());
            if (event.getConnector () != null) {
                msg.setProperty (AcmeModelOperation.CONNECTOR_PROP, event.getConnector ().getQualifiedName ());
            }
            addTypeInformation (event.getRole (), msg);
        }
        catch (RainbowException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    private void addTypeInformation ( IAcmeElementInstance<?, ?> instance,  IRainbowMessage msg) throws RainbowException {
        Set<? extends IAcmeElementTypeRef<?>> dt = instance.getDeclaredTypes ();
        StringBuffer declaredTypes = new StringBuffer ();
        for (IAcmeElementTypeRef<?> ref : dt) {
            declaredTypes.append (ref.getReferencedName ());
            declaredTypes.append (",");
        }
        if (declaredTypes.length () > 0) {
            declaredTypes.deleteCharAt (declaredTypes.length () - 1);
            msg.setProperty (AcmeModelOperation.DECLARED_TYPES_PROP, declaredTypes.toString ());
        }
        dt = instance.getInstantiatedTypes ();
        declaredTypes = new StringBuffer ();
        for (IAcmeElementTypeRef<?> ref : dt) {
            declaredTypes.append (ref.getReferencedName ());
            declaredTypes.append (",");
        }
        if (declaredTypes.length () > 0) {

            declaredTypes.deleteCharAt (declaredTypes.length () - 1);
            msg.setProperty (AcmeModelOperation.INSTANTIATED_TYPES_PROP, declaredTypes.toString ());
        }
    }


    public List<IRainbowMessage> serialize ( List<? extends AcmeEvent> events,  IRainbowMessageFactory port) {
        try {
            List<IRainbowMessage> msgs = new LinkedList<> ();
            IRainbowMessage parent = null;
            Iterator<? extends AcmeEvent> iterator = events.iterator ();
            if (events.size () == 0) return msgs;
            if (events.get (0) instanceof AcmeRainbowOperationEvent) {
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
