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
 * Created April 14, 2007.
 */
package org.sa.rainbow.stitch.model;

/**
 * An interface provided to the rainbow.core package to allow a ModelOperator
 * implementation to be set on Ohana, which Ohana would use to invoke Effectors.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface ModelOperator {

    enum OperatorResult {
        UNKNOWN, SUCCESS, FAILURE;
        public static OperatorResult parseEffectorResult (String result) {
            OperatorResult rv = FAILURE;
            if (result == null) return rv;
            try {
                rv = valueOf(result);
            } catch (IllegalArgumentException e) {  // interpret as failure!
            }
            return rv;
        }
    }

    /**
     * A No-op instance of this interface, used when a ModelOperator
     * implementation has not yet been set when required.
     */
    ModelOperator NO_OP = new ModelOperator () {
        @Override
        public Object invoke(String name, Object[] args) {
            return null;
        }

        @Override
        public Object lookupOperator(String name) {
            return null;
        }
    };

    /**
     * If an operator identified by the given name exists, invokes the named
     * operator, supplying the arguments, where the zero-th argument should be
     * the object used to determine the target to invoke the arch operator,
     * and the remainder list serves as arguments.  If no operator exists, this
     * method returns the String "UNKNOWN".
     * @param name  the name of arch operator to invoke
     * @param args  args[0] is the component, connector, or element on which to
     *     invoke the arch operator
     * @return Object  a String representation of the return value, which should be
     *     parsed into an {@link OperatorResult} enum..
     */
    Object invoke (String name, Object[] args);

    Object lookupOperator (String name);

}
