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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map.Entry;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class SaveUtilityHistoryCmd extends AbstractSaveModelCmd<UtilityHistory> {

    public SaveUtilityHistoryCmd (String commandName, IModelsManager mm, String resource, OutputStream os, String source) {
        super (commandName, mm, resource, os, source);

    }

    @Override
    public Object getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference ("", "UtilityHistory");
    }

    @Override
    protected void subExecute () throws RainbowException {
        UtilityHistory model = getModelContext ().getModelInstance ();

        PrintStream ps = new PrintStream (getStream ());
        for (String key : model.getUtilityKeys ()) {
            for (Entry<Long, Double> entry : model.getUtilityHistory (key).entrySet ()) {
                ps.print (key);
                ps.print (",");
                ps.print (entry.getKey ());
                ps.print (",");
                ps.print (entry.getValue ());
                ps.println ();
            }
        }
        ps.close ();
    }

    @Override
    protected void subRedo () throws RainbowException {

    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (UtilityHistory model) {
        return true;
    }

}
