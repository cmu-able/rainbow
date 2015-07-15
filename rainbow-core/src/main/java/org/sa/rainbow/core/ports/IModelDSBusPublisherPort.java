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

import org.sa.rainbow.core.models.commands.IRainbowOperation;

public interface IModelDSBusPublisherPort extends IRainbowMessageFactory, IDisposablePort {
    public enum Result {
        SUCCESS, FAILURE, UNKNOWN
    };

    public class OperationResult {
        public Result result;
        public String reply;
    }

    public static final String            TACTIC_NAME         = "TACTIC_NAME";
    public static final String            STRATEGY_NAME       = "STRATEGY_NAME";
    public static final String            STRATEGY_OUTCOME    = "STRATEGY_OUTCOME";
    public static final String            TACTIC_SUCCESS      = "TACTIC_SUCCESS";
    public static final String            TACTIC_DURATION     = "TACTIC_DURATION";
    public static final String            TACTIC_PARAM        = "TACTIC_PARAM_";

    public abstract OperationResult publishOperation (IRainbowOperation cmd);

//    public abstract void publishMessage (IRainbowMessage msg);

}
