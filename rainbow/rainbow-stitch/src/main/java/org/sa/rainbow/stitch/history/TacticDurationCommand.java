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
package org.sa.rainbow.stitch.history;

import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class TacticDurationCommand
extends
AbstractRainbowModelOperation<org.sa.rainbow.stitch.util.ExecutionHistoryData, Map<String, ExecutionHistoryData>> {

    private ExecutionHistoryData m_oldDatum;
    private ExecutionHistoryData m_newDatum;

    public TacticDurationCommand (String commandName, IModelInstance<Map<String, ExecutionHistoryData>> model,
            String target, String duration, String successful) {
        super (commandName, model, target, duration, successful);
    }


    @Override
    public ExecutionHistoryData getResult () throws IllegalStateException {
        return m_newDatum;
    }



    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "TacticHistoryOperation");
    }


    @Override
    protected void subExecute () throws RainbowException {

        ExecutionHistoryData datum = getModelContext ().getModelInstance ().get (getTarget ());
        if (datum == null) {
            datum = new ExecutionHistoryData ();
            datum.setIdentifier (getTarget ());
            getModelContext ().getModelInstance ().put (getTarget (), datum);
        }
        else {
            m_oldDatum = new ExecutionHistoryData (datum);
        }
        datum.addDurationSample (Long.parseLong (getParameters ()[0]), Boolean.parseBoolean (getParameters ()[1]));
        m_newDatum = datum;
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        if (m_oldDatum == null) {
            getModelContext ().getModelInstance ().remove (getTarget ());
        }
        else {
            getModelContext ().getModelInstance ().put (getTarget (), m_oldDatum);
        }
    }

    @Override
    protected boolean checkModelValidForCommand (Map<String, ExecutionHistoryData> model) {
        return true;
    }

}
