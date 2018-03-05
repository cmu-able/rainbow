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
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class RosModelCommandFactory extends AcmeModelCommandFactory {

    public static RosLoadModelCommand
    loadCommand (ModelsManager modelsManager, String modelName, InputStream stream, String source) {
        return new RosLoadModelCommand (modelName, modelsManager, stream, source);
    }

    public RosModelCommandFactory (AcmeModelInstance model) {
        super (model);
    }

    @Override
    protected void fillInCommandMap () {
        super.fillInCommandMap ();
        m_commandMap.put ("setSystem".toLowerCase (), SetSystemCommand.class);
        m_commandMap.put ("createNode".toLowerCase (), CreateRosNodeCommand.class);
        m_commandMap.put ("createNodeManager".toLowerCase (), CreateRosNodeCommand.class);
        m_commandMap.put ("deletePort".toLowerCase (), DeletePortCmd.class);
        m_commandMap.put ("deleteRole".toLowerCase (), DeleteRoleCmd.class);
        m_commandMap.put ("attach".toLowerCase (), AttachCmd.class);
        m_commandMap.put ("deleteComponent".toLowerCase (), DeleteComponentCmd.class);
        m_commandMap.put ("deleteConnector".toLowerCase (), DeleteConnectorCmd.class);
        m_commandMap.put ("createActionPort".toLowerCase (), CreateActionPortCommand.class);
        m_commandMap.put ("createTopicPort".toLowerCase (), CreateTopicPortCommand.class);
        m_commandMap.put ("createServicePort".toLowerCase (), CreateServicePortCommand.class);
        m_commandMap.put ("createActionRole".toLowerCase (), CreateActionRoleCommand.class);
        m_commandMap.put ("createActionConnector".toLowerCase (), CreateActionConnectorCommand.class);
        m_commandMap.put ("createTopicRole".toLowerCase (), CreateTopicRoleCommand.class);
        m_commandMap.put ("createTopicConnector".toLowerCase (), CreateTopicConnectorCommand.class);

    }

    public SetSystemCommand setSystem (IAcmeSystem system, String systemSource) {
        return new SetSystemCommand ((AcmeModelInstance )m_modelInstance, "", systemSource);
    }

    public CreateRosNodeCommand createNode (IAcmeSystem system, String nodeName) {
        if (ModelHelper.getAcmeSystem (system) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new CreateRosNodeCommand ((AcmeModelInstance )m_modelInstance, system.getQualifiedName (), nodeName);
    }

    public CreateRosNodeManagerCommand createNodeManager (IAcmeSystem system, String nodeName) {
        if (ModelHelper.getAcmeSystem (system) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new CreateRosNodeManagerCommand ((AcmeModelInstance )m_modelInstance, system.getQualifiedName (),
                nodeName);
    }

    public DeletePortCmd deletePort (IAcmeComponent comp, String port) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeletePortCmd ((AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), port);
    }

    public DeleteRoleCmd deleteRole (IAcmeComponent comp, String Role) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteRoleCmd ((AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), Role);
    }

    public AttachCmd attach (IAcmePort port, IAcmeRole role) {
        IAcmeSystem sys = ModelHelper.getAcmeSystem (port);
        if (sys != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new AttachCmd ((AcmeModelInstance )m_modelInstance, "", port.getQualifiedName (),
                role.getQualifiedName ());
    }

    public DeleteComponentCmd deleteComponent (IAcmeSystem sys, IAcmeComponent comp) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteComponentCmd ((AcmeModelInstance )m_modelInstance, sys.getQualifiedName (),
                comp.getQualifiedName ());
    }

    public DeleteConnectorCmd deleteConnector (IAcmeSystem sys, IAcmeConnector comp) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        return new DeleteConnectorCmd ((AcmeModelInstance )m_modelInstance, sys.getQualifiedName (),
                comp.getQualifiedName ());
    }

    public CreateActionPortCommand createActionPort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateActionPortCommand ((AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    public CreateTopicPortCommand createTopicPort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateTopicPortCommand ((AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    public CreateServicePortCommand
    createServicePort (IAcmeComponent comp, String portName, IAcmeElementType portType) {
        if (ModelHelper.getAcmeSystem (comp) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(portType instanceof IAcmePortType) && !(portType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a port with something that is not a port type");
        if (comp.getPort (portName) != null)
            throw new IllegalArgumentException ("Port already exists in " + comp.getQualifiedName ());
        return new CreateServicePortCommand ((AcmeModelInstance )m_modelInstance, comp.getQualifiedName (), portName,
                portType.getName ());
    }

    public CreateActionRoleCommand createActionRole (IAcmeConnector conn, String roleName, IAcmeElementType roleType) {
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(roleType instanceof IAcmeRoleType) && !(roleType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a role with something that is not a role type");
        if (conn.getRole (roleName) != null)
            throw new IllegalArgumentException ("Role already exists in " + conn.getQualifiedName ());
        return new CreateActionRoleCommand ((AcmeModelInstance )m_modelInstance, conn.getQualifiedName (), roleName,
                roleType.getName ());
    }

    public CreateActionConnectorCommand createActionConnector (IAcmeSystem sys, String connName) {
        if (ModelHelper.getAcmeSystem (sys) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (sys.lookupName (connName, false) != null)
            throw new IllegalArgumentException ("Name already exists in " + sys.getName ());
        return new CreateActionConnectorCommand ((AcmeModelInstance )m_modelInstance, sys.getName (), connName);
    }

    public CreateTopicRoleCommand createTopicRole (IAcmeConnector conn, String roleName, IAcmeElementType roleType) {
        if (ModelHelper.getAcmeSystem (conn) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (!(roleType instanceof IAcmeRoleType) && !(roleType instanceof IAcmeGenericElementType))
            throw new IllegalArgumentException ("Cannot create a role with something that is not a role type");
        if (conn.getRole (roleName) != null)
            throw new IllegalArgumentException ("Role already exists in " + conn.getQualifiedName ());
        return new CreateTopicRoleCommand ((AcmeModelInstance )m_modelInstance, conn.getQualifiedName (), roleName,
                roleType.getName ());
    }

    public CreateTopicConnectorCommand
            createTopicConnector (IAcmeSystem sys, String connName, String topic, String msgType) {
        if (ModelHelper.getAcmeSystem (sys) != m_modelInstance.getModelInstance ())
            throw new IllegalArgumentException ("Cannot create a command for a system that is not part of the system");
        if (sys.lookupName (connName, false) != null)
            throw new IllegalArgumentException ("Name already exists in " + sys.getName ());
        return new CreateTopicConnectorCommand ((AcmeModelInstance )m_modelInstance, sys.getName (), connName, topic,
                msgType);
    }

}
