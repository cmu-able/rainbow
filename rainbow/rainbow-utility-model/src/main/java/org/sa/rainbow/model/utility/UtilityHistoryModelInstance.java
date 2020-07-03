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
package org.sa.rainbow.model.utility;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;

public class UtilityHistoryModelInstance implements IModelInstance<UtilityHistory> {

    public static final String           UTILITY_HISTORY_TYPE = "UtilityHistory";
    private UtilityHistory m_history;
    private String         m_source;
    private UtilityHistoryCommandFactory m_commandFactory;

    public UtilityHistoryModelInstance (UtilityHistory history, String source) {
        setModelInstance (history);
        setOriginalSource (source);
    }

    @Override
    public UtilityHistory getModelInstance () {
        return m_history;
    }

    @Override
    public void setModelInstance (UtilityHistory model) {
        m_history = model;
    }

    @Override
    public IModelInstance<UtilityHistory> copyModelInstance (String newName) throws RainbowCopyException {
        return new UtilityHistoryModelInstance (getModelInstance ().copy (), getOriginalSource ());
    }

    @Override
    public String getModelType () {
        return UTILITY_HISTORY_TYPE;
    }

    @Override
    public String getModelName () {
        return getModelInstance ().getModelReference ().getModelName ();
    }

    @Override
    public UtilityHistoryCommandFactory getCommandFactory () throws RainbowException {
        if (m_commandFactory == null) {
            m_commandFactory = new UtilityHistoryCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void dispose () throws RainbowException {
        m_history = null;
        m_source = null;
    }


}
