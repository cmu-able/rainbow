package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorCreateCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCreateCommand;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class CreateTopicConnectorCommand extends RosAcmeModelCommand<IAcmeConnector> {

    private String                      m_connName;
    private IAcmeConnectorCreateCommand m_connectorCreateCommand;
    private String                      m_topic;
    private String                      m_msgType;

    public CreateTopicConnectorCommand (String commandName, AcmeModelInstance model, String target, String connName, String topic,
            String msgType) {
        super (commandName, model, target, connName);
        m_connName = connName;
        m_topic = topic;
        m_msgType = msgType;
    }

    @Override
    public IAcmeConnector getResult () throws IllegalStateException {
        return m_connectorCreateCommand.getConnector ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_connectorCreateCommand = getModel ().getCommandFactory ().connectorCreateCommand (getModel (), m_connName,
                Arrays.asList ("ActionConnT"), Arrays.asList ("ActionConnT"));
        IAcmePropertyCreateCommand topicPropCommand = getModel ().getCommandFactory ()
                .propertyCreateCommand (m_connectorCreateCommand, "topic", "string", new UMStringValue (m_topic));
        IAcmePropertyCreateCommand msgTypePropCommand = getModel ().getCommandFactory ()
                .propertyCreateCommand (m_connectorCreateCommand, "msg_type", "string", new UMStringValue (m_msgType));
        return Arrays.<IAcmeCommand<?>> asList (m_connectorCreateCommand, topicPropCommand, msgTypePropCommand);
    }

}
