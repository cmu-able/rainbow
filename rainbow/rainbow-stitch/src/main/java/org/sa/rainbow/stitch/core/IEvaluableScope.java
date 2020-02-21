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
 * Created August 30, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.sa.rainbow.core.adaptation.IEvaluable;

import java.util.Set;



/**
 * An identifier Interface which indicates an IScope that can also be evaluated
 * for some result or outcome.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IEvaluableScope<T> extends IEvaluable, IScope {


    /**
     * Returns a set of model elements (IAcmeElement) used by this IEvaluable. An element is considered <em>used</em> by
     * this IEvaluable if it an operator may have made changes to it, which usually means the element has been passed as
     * an argument into an operator.
     * 
     * @return <code>Set</code> of <code>IAcmeElement</code>, or empty set if none.
     */
    Set<? extends T> modelElementsUsed ();

}
