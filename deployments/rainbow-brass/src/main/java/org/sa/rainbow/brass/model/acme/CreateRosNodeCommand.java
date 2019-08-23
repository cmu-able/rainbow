package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class CreateRosNodeCommand extends RosAcmeModelCommand<IAcmeComponent> {

    private String                      m_nodeName;
    private IAcmeComponentCreateCommand m_rosNodeCmd;
    private String                      m_nodeType;

    public CreateRosNodeCommand (AcmeModelInstance model, String system, String nodeName) {
        super ("createNode", model, system, nodeName);
        m_nodeName = nodeName;
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_rosNodeCmd.getComponent ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_rosNodeCmd = getModel ().getCommandFactory ().componentCreateCommand (getModel (), m_nodeName,
                Arrays.asList ("RosNodeCompT"), Arrays.asList ("RosNodeCompT"));
        return Arrays.<IAcmeCommand<?>> asList (m_rosNodeCmd);
    }


}
