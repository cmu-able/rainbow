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
package org.sa.rainbow.model.acme;

import org.acmestudio.acme.core.extension.IAcmeElementExtension;

/**
 * Used by the typechecking analyser to indicate whether the model typechecks or not.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
class RainbowModelTypecheckExtension implements IAcmeElementExtension {

    private final boolean m_typechecks;

    public RainbowModelTypecheckExtension (boolean typechecks) {
        m_typechecks = typechecks;
    }

    public boolean typechecks () {
        return m_typechecks;
    }

    @Override
    public boolean isDistinctInDerivedViews () {
        return false;
    }

    @Override
    public boolean isPersistent () {
        return false;
    }

    @Override
    public Object clone () throws CloneNotSupportedException {
        return super.clone ();
    }

}