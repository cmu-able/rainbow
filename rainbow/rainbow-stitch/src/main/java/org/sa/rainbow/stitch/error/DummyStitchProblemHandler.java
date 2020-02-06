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
 * Created August 16, 2006
 */
package org.sa.rainbow.stitch.error;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This class serves as the default problem handler when the language module
 * is used alone, rather than by the Editor, to parse a Stitch script. 
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class DummyStitchProblemHandler implements StitchProblemHandler {

    private LinkedHashSet<IStitchProblem> m_problems = null;

    /**
     * Default constructor
     */
    public DummyStitchProblemHandler() {
        m_problems = new LinkedHashSet<IStitchProblem>();
    }

    public DummyStitchProblemHandler clone () {
        DummyStitchProblemHandler c = new DummyStitchProblemHandler ();
        for (IStitchProblem p : m_problems
             ) {
            c.m_problems.add (p.clone ());
        }
        return c;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitch.error.StitchProblemHandler#setProblem(org.sa.rainbow.stitch.error.StitchProblem)
     */
    @Override
    public void setProblem(IStitchProblem problem) {
        m_problems.add(problem);
    }

    public Collection<IStitchProblem> getProblems () {
        return m_problems;
    }
    public void clearProblems () {
        m_problems.clear();
    }

    @Override
    public void addAll(Collection<IStitchProblem> problems) {
        m_problems.addAll (problems);
    }

    @Override
    public Collection<IStitchProblem> unreportedProblems() {
        return m_problems;
    }
}
