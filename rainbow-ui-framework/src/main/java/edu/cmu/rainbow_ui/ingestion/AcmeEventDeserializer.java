/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
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
package edu.cmu.rainbow_ui.ingestion;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeGroup;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentCreateCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorCreateCommand;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.model.acme.AcmeModelOperation;

/**
 * Acme Events Deserializer class for Rainbow UI Framework.
 * 
 * <p>
 * Converts a Rainbow event message into a sequence of Acme commands for a
 * particular Acme model instance.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeEventDeserializer {

    public IAcmeCommand<?> deserialize(List<IRainbowMessage> events,
            IAcmeSystem model) throws RainbowDeserializationException {

        List<Pair<String, IAcmeCommand<?>>> commands = new LinkedList<>();

        for (IRainbowMessage msg : events) {
            deserialize(msg, model, commands);
        }

        List<IAcmeCommand<?>> cmds = new LinkedList<>();
        for (Pair<String, IAcmeCommand<?>> p : commands) {
            cmds.add(p.secondValue());
        }
        return model.getCommandFactory().compoundCommand(cmds);
    }

    /**
     * Takes an event and turns it into an Acme command.
     * 
     * @param event - Rainbow message to deserialize
     * @param model - Acme system model
     * @return Acme command
     * @throws edu.cmu.rainbow_ui.ingestion.RainbowDeserializationException
     */
    public IAcmeCommand<?> deserialize(IRainbowMessage event, IAcmeSystem model)
            throws RainbowDeserializationException {
        List<Pair<String, IAcmeCommand<?>>> commands = new LinkedList<>();
        deserialize(event, model, commands);
        return commands.get(0).secondValue();
    }

    private void deserialize(IRainbowMessage msg, IAcmeSystem model,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String eventType = (String) msg
                .getProperty(IRainbowMessageFactory.EVENT_TYPE_PROP);
        AcmeModelEventType et = AcmeModelEventType.valueOf(eventType);
        switch (et) {
        case ADD_ATTACHMENT:
        case REMOVE_ATTACHMENT:
            deserializeAcmeAttachmentEvent(msg, model, et, commands);
            break;
        /*
         * case ADD_BINDING: case REMOVE_BINDING: return
         * deserializeAcmeBindingEvent(msg, model, et);
         */
        case ADD_COMPONENT:
        case REMOVE_COMPONENT:
            deserializeAcmeComponentEvent(msg, model, et, commands);
            break;
        case ADD_CONNECTOR:
        case REMOVE_CONNECTOR:
            deserializeAcmeConnectorEvent(msg, model, et, commands);
            break;
        case ADD_GROUP:
        case REMOVE_GROUP:
            deserializeAcmeGroupEvent(msg, model, et, commands);
            break;
        /*
         * case ADD_MEMBER: case REMOVE_MEMBER:
         */
        case ADD_PORT:
        case REMOVE_PORT:
            deserializeAcmePortEvent(msg, model, et, commands);
            break;
        case ADD_ROLE:
        case REMOVE_ROLE:
            deserializeAcmeRoleEvent(msg, model, et, commands);
            break;
        case ADD_PROPERTY:
        case REMOVE_PROPERTY:
        case SET_PROPERTY_VALUE:
            deserializeAcmePropertyEvent(msg, model, et, commands);
            break;
        default:
            throw new RainbowDeserializationException(MessageFormat.format(
                    "Do not know how to deserialize an event of type ''{0}''.",
                    et.name()));
        }
    }

    private void deserializeAcmePropertyEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String propName = (String) msg
                .getProperty(AcmeModelOperation.PROPERTY_PROP);
        switch (et) {
        case SET_PROPERTY_VALUE:
            IAcmeProperty prop = this.<IAcmeProperty> resolveInModel(propName,
                    model, IAcmeProperty.class);
            String value = (String) msg
                    .getProperty(AcmeModelOperation.VALUE_PROP);
            try {
                IAcmePropertyValue val = StandaloneLanguagePackHelper
                        .defaultLanguageHelper().propertyValueFromString(value,
                                new RegionManager());
                commands.add(new Pair<String, IAcmeCommand<?>>(propName, model
                        .getCommandFactory().propertyValueSetCommand(prop, val)));
                return;
            } catch (Exception e) {
                throw new RainbowDeserializationException(
                        MessageFormat
                                .format("Could not convert ''{0}'' to a property value.",
                                        value));
            }
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    private void deserializeAcmeRoleEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String roleName = (String) msg
                .getProperty(AcmeModelOperation.ROLE_PROP);
        String connName = (String) msg
                .getProperty(AcmeModelOperation.CONNECTOR_PROP);

        model = getSystemPropertyIfExists(msg, model);
        if (connName == null) {
            connName = roleName.substring(0, roleName.lastIndexOf(".") - 1);
        }

        switch (et) {
        case ADD_ROLE:
            IAcmeConnector conn = this.<IAcmeConnector> resolveInModel(
                    connName, model, IAcmeConnector.class);
            List<String> dts = getTypeList(msg,
                    AcmeModelOperation.DECLARED_TYPES_PROP);
            List<String> its = getTypeList(msg,
                    AcmeModelOperation.INSTANTIATED_TYPES_PROP);
            roleName = roleName.substring(roleName.lastIndexOf(".") + 1);
            if (conn != null) {
                commands.add(new Pair<String, IAcmeCommand<?>>(roleName, model
                        .getCommandFactory().roleCreateCommand(conn, roleName,
                                dts, its)));
                return;
            } else {
                IAcmeConnectorCreateCommand cmd = this
                        .<IAcmeConnectorCreateCommand> findKeyedCommand(
                                connName, commands,
                                IAcmeConnectorCreateCommand.class);
                if (cmd != null) {
                    commands.add(new Pair<String, IAcmeCommand<?>>(roleName,
                            model.getCommandFactory().roleCreateCommand(cmd,
                                    roleName, dts, its)));
                    return;
                } else {
                    throw new RainbowDeserializationException(
                            MessageFormat
                                    .format("Could not find a connector or a command associated with the connector ''{0}''.",
                                            connName));
                }
            }
        case REMOVE_ROLE:
            IAcmeRole role = this.<IAcmeRole> resolveInModel(roleName, model,
                    IAcmeRole.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(roleName, model
                    .getCommandFactory().roleDeleteCommand(role)));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    private <T> T findKeyedCommand(String name,
            List<Pair<String, IAcmeCommand<?>>> commands, Class<T> cls) {
        T t = null;
        for (int i = commands.size() - 1; i >= 0 && t == null; i--) {
            Pair<String, IAcmeCommand<?>> pair = commands.get(i);
            if (pair.firstValue().equals(name)
                    && pair.secondValue().getClass().isAssignableFrom(cls)) {
                t = (T) pair.secondValue();
            }
        }
        return t;
    }

    private void deserializeAcmePortEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String portName = (String) msg
                .getProperty(AcmeModelOperation.PORT_PROP);
        String compName = (String) msg
                .getProperty(AcmeModelOperation.COMPONENT_PROP);

        model = getSystemPropertyIfExists(msg, model);
        IAcmeComponent comp = null;
        if (compName == null) {
            compName = portName.substring(0, portName.lastIndexOf(".") - 1);
        }
        comp = this.<IAcmeComponent> resolveInModel(compName, model,
                IAcmeComponent.class);

        switch (et) {
        case ADD_PORT:
            List<String> dts = getTypeList(msg,
                    AcmeModelOperation.DECLARED_TYPES_PROP);
            List<String> its = getTypeList(msg,
                    AcmeModelOperation.INSTANTIATED_TYPES_PROP);
            portName = portName.substring(portName.lastIndexOf(".") + 1);
            if (comp != null) {
                commands.add(new Pair<String, IAcmeCommand<?>>(portName, model
                        .getCommandFactory().portCreateCommand(comp, portName,
                                dts, its)));
                return;
            } else {
                IAcmeComponentCreateCommand cmd = this
                        .<IAcmeComponentCreateCommand> findKeyedCommand(
                                compName, commands,
                                IAcmeComponentCreateCommand.class);
                if (cmd != null) {
                    commands.add(new Pair<String, IAcmeCommand<?>>(portName,
                            model.getCommandFactory().portCreateCommand(cmd,
                                    portName, dts, its)));
                    return;
                } else {
                    throw new RainbowDeserializationException(
                            MessageFormat
                                    .format("Could not find a connector or a command associated with the connector ''{0}''.",
                                            compName));
                }
            }
        case REMOVE_PORT:
            IAcmePort port = this.<IAcmePort> resolveInModel(portName, model,
                    IAcmePort.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(portName, model
                    .getCommandFactory().portDeleteCommand(port)));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    private void deserializeAcmeGroupEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String groupName = (String) msg
                .getProperty(AcmeModelOperation.GROUP_PROP);

        model = getSystemPropertyIfExists(msg, model);

        switch (et) {
        case ADD_GROUP:
            List<String> dts = getTypeList(msg,
                    AcmeModelOperation.DECLARED_TYPES_PROP);
            List<String> its = getTypeList(msg,
                    AcmeModelOperation.INSTANTIATED_TYPES_PROP);
            commands.add(new Pair<String, IAcmeCommand<?>>(groupName, model
                    .getCommandFactory().groupCreateCommand(model, groupName,
                            dts, its)));
            return;
        case REMOVE_GROUP:
            IAcmeGroup comp = this.<IAcmeGroup> resolveInModel(groupName,
                    model, IAcmeGroup.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(groupName, model
                    .getCommandFactory().groupDeleteCommand(comp)));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    private void deserializeAcmeConnectorEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String connName = (String) msg
                .getProperty(AcmeModelOperation.CONNECTOR_PROP);

        model = getSystemPropertyIfExists(msg, model);

        switch (et) {
        case ADD_CONNECTOR:
            List<String> dts = getTypeList(msg,
                    AcmeModelOperation.DECLARED_TYPES_PROP);
            List<String> its = getTypeList(msg,
                    AcmeModelOperation.INSTANTIATED_TYPES_PROP);
            commands.add(new Pair<String, IAcmeCommand<?>>(connName, model
                    .getCommandFactory().connectorCreateCommand(model,
                            connName, dts, its)));
            return;
        case REMOVE_CONNECTOR:
            IAcmeConnector comp = this.<IAcmeConnector> resolveInModel(
                    connName, model, IAcmeConnector.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(connName, model
                    .getCommandFactory().connectorDeleteCommand(comp)));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    private void deserializeAcmeComponentEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String compName = (String) msg
                .getProperty(AcmeModelOperation.COMPONENT_PROP);

        model = getSystemPropertyIfExists(msg, model);

        switch (et) {
        case ADD_COMPONENT:
            List<String> dts = getTypeList(msg,
                    AcmeModelOperation.DECLARED_TYPES_PROP);
            List<String> its = getTypeList(msg,
                    AcmeModelOperation.INSTANTIATED_TYPES_PROP);
            commands.add(new Pair<String, IAcmeCommand<?>>(compName, model
                    .getCommandFactory().componentCreateCommand(model,
                            compName, dts, its)));
            return;
        case REMOVE_COMPONENT:
            IAcmeComponent comp = this.<IAcmeComponent> resolveInModel(
                    compName, model, IAcmeComponent.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(compName, model
                    .getCommandFactory().componentDeleteCommand(comp)));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));

    }

    protected List<String> getTypeList(IRainbowMessage msg, String prop) {
        String types = (String) msg.getProperty(prop);
        List<String> dts = Collections.<String> emptyList();
        if (types != null && !types.isEmpty()) {
            dts = Arrays.asList(types.split(","));
        }
        return dts;
    }

    private void deserializeAcmeAttachmentEvent(IRainbowMessage msg,
            IAcmeSystem model, AcmeModelEventType et,
            List<Pair<String, IAcmeCommand<?>>> commands)
            throws RainbowDeserializationException {
        String portName = (String) msg
                .getProperty(AcmeModelOperation.PORT_PROP);
        String roleName = (String) msg
                .getProperty(AcmeModelOperation.ROLE_PROP);
        model = getSystemPropertyIfExists(msg, model);

        switch (et) {
        case ADD_ATTACHMENT:
            commands.add(new Pair<String, IAcmeCommand<?>>(portName + " to "
                    + roleName, model.getCommandFactory()
                    .attachmentCreateCommand(model, portName, roleName)));
            return;
        case REMOVE_ATTACHMENT:
            IAcmePort p = this.<IAcmePort> resolveInModel(portName, model,
                    IAcmePort.class);
            IAcmeRole r = this.<IAcmeRole> resolveInModel(roleName, model,
                    IAcmeRole.class);
            commands.add(new Pair<String, IAcmeCommand<?>>(portName + " to "
                    + roleName, model.getCommandFactory()
                    .attachmentDeleteCommand(model.getAttachment(p, r))));
            return;
        }
        throw new RainbowDeserializationException(MessageFormat.format(
                "Do not know how to deserialize an event of type ''{0}''.",
                et.name()));
    }

    protected IAcmeSystem getSystemPropertyIfExists(IRainbowMessage msg,
            IAcmeSystem model) throws RainbowDeserializationException {
        String systemName = (String) msg
                .getProperty(AcmeModelOperation.SYSTEM_PROP);

        if (systemName != null && !model.getName().equals(systemName)) {
            Object o = model.lookupName(systemName);
            if (o instanceof IAcmeSystem) {
                model = (IAcmeSystem) o;
            } else {
                throw new RainbowDeserializationException(MessageFormat.format(
                        "The system ''{0}'' could not be found in {1}",
                        systemName, model.getName()));
            }
        }
        return model;
    }

    private <T> T resolveInModel(String qualifiedName, IAcmeSystem model,
            Class<T> cls) throws RainbowDeserializationException {
        Object o = model.getContext().getModel().lookupName(qualifiedName);
        if (o == null || !cls.isAssignableFrom(o.getClass())) {

            throw new RainbowDeserializationException(MessageFormat.format(
                    "Could not resolve ''{0}'' as a {1} in {2}", qualifiedName,
                    cls.getSimpleName(), model.getQualifiedName()));
        }
        T t = (T) o;
        return t;
    }
}
