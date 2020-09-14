package org.sa.rainbow.brass.model.acme;

import java.io.InputStream;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.IAcmeGenericElementType;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.element.IAcmePortType;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.element.IAcmeRoleType;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class RosModelCommandFactory extends AcmeModelCommandFactory {

    private static final String CREATE_TOPIC_CONNECTOR_CMD = "createTopicConnector";
	private static final String CREATE_TOPIC_ROLE_CMD = "createTopicRole";
	private static final String CREATE_ACTION_CONNECTOR_CMD = "createActionConnector";
	private static final String CREATE_ACTION_ROLE_CMD = "createActionRole";
	private static final String CREATE_SERVICE_PORT_CMD = "createServicePort";
	private static final String CREATE_TOPIC_PORT_CMD = "createTopicPort";
	private static final String CREATE_ACTION_PORT_CMD = "createActionPort";
	private static final String DELETE_CONNECTOR_CMD = "deleteConnector";
	private static final String DELETE_COMPONENT_CMD = "deleteComponent";
	private static final String ATTACH_CMD = "attach";
	private static final String DELETE_ROLE_CMD = "deleteRole";
	private static final String DELETE_PORT_CMD = "deletePort";
	private static final String CREATE_NODE_MANAGER_CMD = "createNodeManager";
	private static final String CREATE_NODE_CMD = "createNode";
	private static final String SET_SYSTEM_CMD = "setSystem";

	public static RosLoadModelCommand
    loadCommand (ModelsManager modelsManager, String modelName, InputStream stream, String source) {
        return new RosLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public RosModelCommandFactory (AcmeModelInstance model) throws RainbowException {
        super (model);
    }

    
    @Operation(name=SET_SYSTEM_CMD)
    public SetSystemCommand setSystem (IAcmeSystem system, String systemSource) {
        return new SetSystemCommand (SET_SYSTEM_CMD, (AcmeModelInstance )m_modelInstance, "", systemSource);
    }

    @Operation(name=CREATE_NODE_CMD)
    public CreateRosNodeCommand createNode (IAcmeSystem system, String nodeName) {
        if (ModelHelper.getAcmeSystem (system) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new CreateRosNodeCommand (CREATE_NODE_CMD, (AcmeModelInstance )m_modelInstance, system.getQualifiedName (), nodeName);
    }

    @Operation(name=CREATE_NODE_MANAGER_CMD)
    public CreateRosNodeManagerCommand createNodeManager (IAcmeSystem system, String nodeName) {
        if (ModelHelper.getAcmeSystem (system) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new CreateRosNodeManagerCommand (CREATE_NODE_MANAGER_CMD, (AcmeModelInstance )m_modelInstance, system.getQualifiedName (),
                nodeName);
    }
    
    @Operation(name=DELETE_PORT_CMD)
    public DeletePortCmd deletePort (IAcmeComponent comp, String port) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeletePortCmd (DELETE_PORT_CMD, (AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), port);
    }

    @Operation(name=DELETE_ROLE_CMD)
    public DeleteRoleCmd deleteRole (IAcmeComponent comp, String Role) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteRoleCmd (DELETE_ROLE_CMD, (AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), Role);
    }

    @Operation(name=ATTACH_CMD)
    public AttachCmd attach (IAcmePort port, IAcmeRole role) {
        IAcmeSystem sys = ModelHelper.getAcmeSystem (port);
        if (sys != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new AttachCmd (ATTACH_CMD, (AcmeModelInstance )m_modelInstance, "", port.getQualifiedName (),
                role.getQualifiedName ());
    }

    @Operation(name=DELETE_COMPONENT_CMD)
    public DeleteComponentCmd deleteComponent (IAcmeSystem sys, IAcmeComponent comp) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteComponentCmd (DELETE_COMPONENT_CMD, (AcmeModelInstance )m_modelInstance, sys.getQualifiedName (),
                comp.getQualifiedName ());
    }

    @Operation(name=DELETE_CONNECTOR_CMD)
    public DeleteConnectorCmd deleteConnector (IAcmeSystem sys, IAcmeConnector comp) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteConnectorCmd (DELETE_CONNECTOR_CMD, (AcmeModelInstance )m_modelInstance, sys.getQualifiedName (),
                comp.getQualifiedName ());
    }

    @Operation(name=CREATE_ACTION_PORT_CMD)
    public CreateActionPortCommand createActionPort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateActionPortCommand (CREATE_ACTION_PORT_CMD, (AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    @Operation(name=CREATE_TOPIC_PORT_CMD)
    public CreateTopicPortCommand createTopicPort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateTopicPortCommand (CREATE_TOPIC_PORT_CMD, (AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    @Operation(name=CREATE_SERVICE_PORT_CMD)
    public CreateServicePortCommand
    createServicePort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateServicePortCommand (CREATE_SERVICE_PORT_CMD, (AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    @Operation(name=CREATE_ACTION_ROLE_CMD)
    public CreateActionRoleCommand createActionRole (IAcmeConnector conn, String roleName, IAcmeElementType roleType) {
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(roleType instanceof IAcmeRoleType) && !(roleType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a role with something that is not a role type");
        if (conn.getRole (roleName) != null)
            throw new IllegalArgumentException ("Role already exists in " + conn.getQualifiedName ());
        return new CreateActionRoleCommand (CREATE_ACTION_ROLE_CMD, (AcmeModelInstance )m_modelInstance, conn.getQualifiedName (), roleName,
                roleType.getName ());
    }

    @Operation(name=CREATE_ACTION_CONNECTOR_CMD)
    public CreateActionConnectorCommand createActionConnector (IAcmeSystem sys, String connName) {
        if (ModelHelper.getAcmeSystem (sys) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (sys.lookupName (connName, false) != null)
            throw new IllegalArgumentException ("Name already exists in " + sys.getName ());
        return new CreateActionConnectorCommand (CREATE_ACTION_CONNECTOR_CMD, (AcmeModelInstance )m_modelInstance, sys.getName (), connName);
    }

    @Operation(name=CREATE_TOPIC_ROLE_CMD)
    public CreateTopicRoleCommand createTopicRole (IAcmeConnector conn, String roleName, IAcmeElementType roleType) {
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(roleType instanceof IAcmeRoleType) && !(roleType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a role with something that is not a role type");
        if (conn.getRole (roleName) != null)
            throw new IllegalArgumentException ("Role already exists in " + conn.getQualifiedName ());
        return new CreateTopicRoleCommand (CREATE_TOPIC_ROLE_CMD, (AcmeModelInstance )m_modelInstance, conn.getQualifiedName (), roleName,
                roleType.getName ());
    }

    @Operation(name=CREATE_TOPIC_CONNECTOR_CMD)
    public CreateTopicConnectorCommand
            createTopicConnector (IAcmeSystem sys, String connName, String topic, String msgType) {
        if (ModelHelper.getAcmeSystem (sys) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (sys.lookupName (connName, false) != null)
            throw new IllegalArgumentException ("Name already exists in " + sys.getName ());
        return new CreateTopicConnectorCommand (CREATE_TOPIC_CONNECTOR_CMD, (AcmeModelInstance )m_modelInstance, sys.getName (), connName, topic,
                msgType);
    }

}
