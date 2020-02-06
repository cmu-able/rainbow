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
 * Created March 8, 2007.
 */
package org.sa.rainbow.stitch.error;

/**
 * This exception is thrown when either the Tactic or Strategy has been invoked
 * with arguments that are mismatched from the formal parameters. 
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ArgumentMismatchException extends RuntimeException {

    private static final long serialVersionUID = 3556065601989614311L;

    /**
     * Default constructor.
     */
    public ArgumentMismatchException() {
        super("");
    }

    /**
     * @param message
     */
    public ArgumentMismatchException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ArgumentMismatchException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ArgumentMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

}
