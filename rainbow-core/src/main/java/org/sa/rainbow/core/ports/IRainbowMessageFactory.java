package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.event.IRainbowMessage;

public interface IRainbowMessageFactory {
    public static final String ID_PROP         = "ID";
    public static final String COMMAND_PROP    = "COMMAND";
    public static final String TARGET_PROP     = "TARGET";
    public static final String MODEL_NAME_PROP = "MODEL_NAME";
    public static final String PARAMETER_PROP  = "PARAMETER";
    public static final String EVENT_TYPE_PROP = "EVENTTYPE";
    public static final String PARENT_ID_PROP  = "PARENT_ID";

    public IRainbowMessage createMessage ();

}
