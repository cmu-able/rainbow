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
 * Created February 1, 2007.
 */
package org.sa.rainbow.translator.effectors;


/**
 * This interface defines common strings and commands required for the Gauge
 * Protocol.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEffectorProtocol {

    /** The ID is used for report, since SystemDelegate already knows the provisioning Effector. */
    String ID = "id";
    /** The Name is used in concert with Location for execution, to match Effector. */
    String NAME = "name";
    String LOCATION = "location";
    String OUTCOME = "outcome";
    String ARGUMENT = "argument";
    String SIZE = "_size";
    String EFFECTOR_CREATED = "effectorCreated";
    String SERVICE = "service";
    String KIND = "kind";
    String EFFECTOR_DELETED = "effectorDeleted";
    String EFFECTOR_EXECUTED = "effectorExecuted";

}
