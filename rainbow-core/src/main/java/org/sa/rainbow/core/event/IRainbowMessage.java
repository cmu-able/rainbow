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

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;

/**
 * Interface for an event message in the Rainbow framework.  The underlying
 * form of a message is a set of key-value pairs.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IRainbowMessage {

    public static final String CHANNEL = "channel";
    public static final String MESSAGE_CREATED = "msg-created";
    public static final String MESSAGE_SENT = "msg-sent";

    public List<String> getPropertyNames ();

    public Object getProperty (String id);

    public void setProperty (String id, Object prop) throws RainbowException;

}
