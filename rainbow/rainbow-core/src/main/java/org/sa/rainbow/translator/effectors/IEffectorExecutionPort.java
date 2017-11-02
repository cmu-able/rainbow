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
package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.core.ports.IDisposablePort;

import java.util.List;

public interface IEffectorExecutionPort extends IDisposablePort {
    enum Outcome {
        UNKNOWN, CONFOUNDED, FAILURE, SUCCESS, TIMEOUT, result, LONG_RUNNING
    }

    /**
     * Executes the effect supplied by this effector, applying any arguments.
     * 
     * @param args
     *            array of String arguments
     * @return Outcome the execution outcome as defined in the enum
     *         {@link org.sa.rainbow.translator.effectors.IEffector.Outcome <code>Outcome</code>}
     */
    Outcome execute (List<String> args);
}
