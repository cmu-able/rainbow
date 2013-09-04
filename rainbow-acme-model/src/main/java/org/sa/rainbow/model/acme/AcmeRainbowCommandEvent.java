package org.sa.rainbow.model.acme;

import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

public class AcmeRainbowCommandEvent extends AcmeEvent {

    public static enum CommandEventT {
        START_COMMAND, FINISH_COMMAND, START_UNDO_COMMAND, FINISH_UNDO_COMMAND;

        public boolean isEnd () {
            return this == FINISH_COMMAND || this == FINISH_UNDO_COMMAND;
        }
    };

    private IRainbowModelCommandRepresentation m_rep;
    private CommandEventT                      m_event;

    public CommandEventT getEventType () {
        return m_event;
    }

    public IRainbowModelCommandRepresentation getCommand () {
        return m_rep;
    }

    public AcmeRainbowCommandEvent (CommandEventT event, IRainbowModelCommandRepresentation rep) {
        super (AcmeModelEventType.SET_COMMENT);
        m_event = event;
        m_rep = rep;
    }

}
