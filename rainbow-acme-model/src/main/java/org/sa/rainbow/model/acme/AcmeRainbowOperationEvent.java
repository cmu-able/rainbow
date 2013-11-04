package org.sa.rainbow.model.acme;

import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class AcmeRainbowOperationEvent extends AcmeEvent {

    public static enum CommandEventT {
        START_COMMAND, FINISH_COMMAND, START_UNDO_COMMAND, FINISH_UNDO_COMMAND, LOAD_MODEL;

        public boolean isEnd () {
            return this == FINISH_COMMAND || this == FINISH_UNDO_COMMAND;
        }

    };

    private IRainbowOperation m_rep;
    private CommandEventT                      m_event;

    public CommandEventT getEventType () {
        return m_event;
    }

    public IRainbowOperation getCommand () {
        return m_rep;
    }

    public AcmeRainbowOperationEvent (CommandEventT event, IRainbowOperation rep) {
        super (AcmeModelEventType.SET_COMMENT);
        m_event = event;
        m_rep = rep;
    }

}
