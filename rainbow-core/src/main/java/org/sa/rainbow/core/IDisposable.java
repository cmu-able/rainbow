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
 * Created January 17, 2007.
 */
package org.sa.rainbow.core;

/**
 * An interface declaring the support for, thus (hopefully) the implementation
 * of, a terminate (i.e., dispose) method.  A Class with its own dispose
 * methods may call dispose() from terminate.
 * <p>
 * The contract is that, before terminate() gets executed, isTerminate() should
 * NOT return <code>true</code> (except possibly in the early, uninitialized
 * stage).  After the end of the terminate() method, isTerminated() should
 * return <code>true</code>.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IDisposable {

    /**
     * Disconnects any I/O connections, disposes of all resources, and
     * null-out members as good measure.
     */
    void dispose ();

    /**
     * Returns whether this object is considered to have been disposed,
     * usually by checking some key data member, or via an explicit boolean.
     * @return boolean  <code>true</code> if object disposed, <code>false</code> otherwise
     */
    boolean isDisposed ();

}
