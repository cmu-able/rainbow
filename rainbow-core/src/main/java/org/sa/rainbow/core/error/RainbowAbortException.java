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
 * Created May 8, 2007.
 */
package org.sa.rainbow.core.error;


/**
 * This RuntimeException may be thrown when Rainbow cannot continue to run,
 * in particular during initialization when certain required resources are
 * not found.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class RainbowAbortException extends RuntimeException {
    private static final long serialVersionUID = 5161763615812106395L;

    /**
     * Default Constructor, no message.
     */
    public RainbowAbortException () {
        this("Rainbow doesn't know how to proceed, aborted!");
    }

    public RainbowAbortException (String message) {
        super(message);
    }

    public RainbowAbortException (Throwable cause) {
        super(cause);
    }

    public RainbowAbortException (String message, Throwable cause) {
        super(message, cause);
    }

}
