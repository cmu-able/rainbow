package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeAttachment;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeAttachmentCommand;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

public class AttachCmd extends AcmeModelOperation<IAcmeAttachment> {

    private String                 m_port;
    private String                 m_role;
    private IAcmeAttachmentCommand m_attachCmd;

    public AttachCmd (AcmeModelInstance model, String system, String port, String role) {
        super ("attach", model, system, port, role);
        // TODO Auto-generated constructor stub
        m_port = port;
        m_role = role;
    }

    @Override
    public IAcmeAttachment getResult () throws IllegalStateException {
        return m_attachCmd.getAttachment ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmePort port = getModelContext ().resolveInModel (m_port, IAcmePort.class);
        IAcmeRole role = getModelContext ().resolveInModel (m_role, IAcmeRole.class);
        if (port == null || role == null) throw new RainbowModelException ("Neither port nor role can be null");
        m_attachCmd = port.getCommandFactory ().attachmentCreateCommand (port, role);
        return Arrays.<IAcmeCommand<?>> asList (m_attachCmd);
    }

    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
