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
package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.event.IRainbowMessage;

public interface IRainbowMessageFactory {
    public static final String ID_PROP         = "ID";
    public static final String COMMAND_PROP    = "COMMAND";
    public static final String TARGET_PROP     = "TARGET";
    public static final String MODEL_NAME_PROP = "MODEL_NAME";
    public static final String MODEL_TYPE_PROP = "MODEL_TYPE";

    public static final String PARAMETER_PROP  = "PARAMETER";
    public static final String EVENT_TYPE_PROP = "EVENTTYPE";
    public static final String PARENT_ID_PROP  = "PARENT_ID";

    public IRainbowMessage createMessage ();

}
