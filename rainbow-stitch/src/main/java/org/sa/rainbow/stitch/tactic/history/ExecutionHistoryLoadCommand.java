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
package org.sa.rainbow.stitch.tactic.history;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class ExecutionHistoryLoadCommand extends AbstractLoadModelCmd<Map<String, ExecutionHistoryData>> {



    private ExecutionHistoryModelInstance m_result;
    private InputStream                   m_inputStream;
    private String                        m_name;

    public ExecutionHistoryLoadCommand (ModelsManager modelsManager, String modelName, InputStream stream, String source) {
        super ("loadExecutionHistory", modelsManager, modelName, stream, source);
        m_inputStream = stream;
        m_name = modelName;
    }

    @Override
    public IModelInstance<Map<String, ExecutionHistoryData>> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    public String getModelName () {
        return m_name;
    }

    @Override
    public String getModelType () {
        return ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE;
    }

    @Override
    protected void subExecute () throws RainbowException {
        Map<String, ExecutionHistoryData> map = new HashMap<> ();
        BufferedReader br = new BufferedReader (new InputStreamReader (m_inputStream));
        Pattern p = Pattern.compile ("^(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)$");
        String line = null;
        try {
            while ((line = br.readLine ()) != null) {
                Matcher m = p.matcher (line);
                if (m.matches ()) {
                    try {
                        String iden = m.group (1);
                        ExecutionHistoryData datum = new ExecutionHistoryData (iden, Integer.parseInt (m.group (2)),
                                Double.parseDouble (m.group (3)), Double.parseDouble (m.group (4)), Long.parseLong (m
                                        .group (5)), Long.parseLong (m.group (6)));
                        map.put (iden, datum);
                    }
                    catch (NumberFormatException e) {
                        throw new RainbowException ("Tactic history formatting error? " + line, e);

                    }
                }
                else
                    throw new RainbowException ("Tactic history formatting error? " + line);
            }
            m_result = new ExecutionHistoryModelInstance (map, getModelName (), getOriginalSource ());
            doPostExecute ();
        }
        catch (IOException e) {
            throw new RainbowException (e);
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        doPostExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        doPostUndo ();
    }

    @Override
    protected boolean checkModelValidForCommand (Object model) {
        return true;
    }

}
