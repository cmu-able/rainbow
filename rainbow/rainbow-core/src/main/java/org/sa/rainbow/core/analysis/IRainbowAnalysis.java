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
package org.sa.rainbow.core.analysis;

import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

/**
 * The interface of Rainbow analyses, which analyze and change models in the models manager.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public interface IRainbowAnalysis extends Identifiable, IRainbowRunnable {

    /**
     * Initializes the component
     * 
     * @param port
     * @throws RainbowConnectionException
     */
    void initialize (IRainbowReportingPort port) throws RainbowConnectionException;

    void setProperty (String key, String value);

    String getProperty (String key);

}
