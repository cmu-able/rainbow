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

import org.acmestudio.acme.model.event.AcmeEvent;
import org.acmestudio.acme.model.event.AcmeModelEventType;
import org.sa.rainbow.core.models.commands.IRainbowOperation;

public class AcmeRainbowOperationEvent extends AcmeEvent {

    public enum CommandEventT {
        START_COMMAND, FINISH_COMMAND, START_UNDO_COMMAND, FINISH_UNDO_COMMAND, LOAD_MODEL;

        public boolean isEnd () {
            return this == FINISH_COMMAND || this == FINISH_UNDO_COMMAND;
        }

    }

    private final IRainbowOperation m_rep;
    private final CommandEventT m_event;

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
