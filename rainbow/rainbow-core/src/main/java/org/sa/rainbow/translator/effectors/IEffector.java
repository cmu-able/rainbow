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
 * Created November 4, 2006.
 */
package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.core.ports.IRainbowReportingPort;

import java.util.List;

/**
 * General interface for the system Effector. The Identifiable.id() returns the reference ID of this effector, which is
 * the same ID used by Rainbow to obtain access to particular effectors.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEffector extends IEffectorIdentifier, IEffectorExecutionPort {


    IEffector NULL_EFFECTOR = new IEffector () {
        @Override
        public String id () {
            return "NullEffector@0.0.0.0";
        }

        @Override
        public String service () {
            return "ANull";
        }

        @Override
        public Kind kind () {
            return Kind.NULL;
        }

        @Override
        public Outcome execute (List<String> args) {
            return Outcome.UNKNOWN;
        }

        @Override
        public void setReportingPort (IRainbowReportingPort port) {
        }

        @Override
        public void dispose () {

        }

    };


    void setReportingPort (IRainbowReportingPort port);

}
