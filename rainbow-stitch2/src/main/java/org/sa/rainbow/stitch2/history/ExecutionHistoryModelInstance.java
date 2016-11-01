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
package org.sa.rainbow.stitch2.history;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.stitch2.util.ExecutionHistoryData;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ExecutionHistoryModelInstance implements IModelInstance<Map<String, ExecutionHistoryData>> {

    public static final String EXECUTION_HISTORY_TYPE = "ExecutionHistory";
    public static final String TACTIC                 = "Tactic";
    public static final String STRATEGY               = "Strategy";

    Map<String, ExecutionHistoryData> m_tacticHistoryMap;
    private String m_name;
    private String m_source;

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    public ExecutionHistoryModelInstance (Map<String, ExecutionHistoryData> map, String name, String source) {
        m_name = name;
        m_source = source;
        m_tacticHistoryMap = map;
    }

    @Override
    public Map<String, ExecutionHistoryData> getModelInstance () {
        return m_tacticHistoryMap;
    }

    @Override
    public void setModelInstance (Map<String, ExecutionHistoryData> model) {
        m_tacticHistoryMap = model;
    }

    @Override
    public IModelInstance<Map<String, ExecutionHistoryData>> copyModelInstance (String newName)
            throws RainbowCopyException {
        Map<String, ExecutionHistoryData> n = new HashMap<> ();
        for (Entry<String, ExecutionHistoryData> e : m_tacticHistoryMap.entrySet ()) {
            n.put (e.getKey (), new ExecutionHistoryData (e.getValue ()));
        }
        return new ExecutionHistoryModelInstance (n, newName, null);
    }

    @Override
    public String getModelType () {
        return EXECUTION_HISTORY_TYPE;
    }

    @Override
    public String getModelName () {
        return m_name;
    }

    public void setModelName (String name) {
        m_name = name;
    }

    @Override
    public ExecutionHistoryCommandFactory getCommandFactory () {
        return new ExecutionHistoryCommandFactory (this);
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public void dispose () throws RainbowException {
    }

    public void markDisruption (double level) {

    }


}