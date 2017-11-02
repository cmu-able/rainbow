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
/**
 * Created November 27, 2006.
 */
package org.sa.rainbow.core.event;

import org.sa.rainbow.core.error.RainbowException;

import java.util.List;

/**
 * Interface for an event message in the Rainbow framework.  The underlying
 * form of a message is a set of key-value pairs.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IRainbowMessage {

    String CHANNEL = "channel";
    String MESSAGE_CREATED = "msg-created";
    String MESSAGE_SENT = "msg-sent";

    List<String> getPropertyNames ();

    Object getProperty (String id);

    void setProperty (String id, Object prop) throws RainbowException;

}
