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
 * Created November 22, 2006.
 */
package org.sa.rainbow.core.error;

/**
 * This RuntimeException is thrown anytime a Probe Lifecycle transition is done
 * that is not valid, i.e., doesn't follow one of the allowed transitions.
 * @see <code>IProbe</code>
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class BadLifecycleStepException extends RuntimeException {
    private static final long serialVersionUID = -6100659947752279326L;

    /**
     * Default Constructor, no message.
     */
    public BadLifecycleStepException () {
        this("A bad Probe Lifecycle transition just occurred!");
    }

    public BadLifecycleStepException (String message) {
        super(message);
    }

    public BadLifecycleStepException (Throwable cause) {
        super(cause);
    }

    public BadLifecycleStepException (String message, Throwable cause) {
        super(message, cause);
    }

}
