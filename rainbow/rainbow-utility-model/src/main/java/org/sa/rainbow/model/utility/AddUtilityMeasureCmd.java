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

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class AddUtilityMeasureCmd extends AbstractRainbowModelOperation<Double, UtilityHistory> {

    private Double                      m_utility;
    private long   m_timestampRecorded;

    public AddUtilityMeasureCmd(String name, UtilityHistoryModelInstance modelInstance, String target, double utility) {
    	this(name, modelInstance, target, Double.toString(utility));
    }
    
    
    public AddUtilityMeasureCmd (String name, UtilityHistoryModelInstance modelInstance, String target, String utility) {
        super (name, modelInstance, target, utility);
        m_utility = Double.valueOf (utility);
    }




    @Override
    public Double getResult () throws IllegalStateException {
        return m_utility;
    }



    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "UtilityHistoryOperation");
    }


    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().add (getTarget (), m_timestampRecorded, m_utility);
    }


    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }


    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().forget (m_timestampRecorded);
    }



    @Override
    protected boolean checkModelValidForCommand (UtilityHistory model) {
        return true;
    }


}
