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

import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.armani.ArmaniExportVisitor;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

public class AcmeSaveModelCommand extends AbstractSaveModelCmd<IAcmeSystem> {

    private final String m_systemName;

    public AcmeSaveModelCommand (String systemName, AcmeModelInstance model, OutputStream os) {
        super ("SaveAcmeModel", null, systemName, os, "");
        m_systemName = systemName;
    }


    @Override
    public IModelInstance<IAcmeSystem> getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference (m_systemName, "Acme");
    }

    @Override
    protected void subExecute () throws RainbowException {
        IAcmeModel model = getModelContext ().getModelInstance ().getContext ().getModel ();

        try {
            BufferedOutputStream stream = new BufferedOutputStream (getStream ());
            ArmaniExportVisitor visitor = new ArmaniExportVisitor (stream);
            model.visit (visitor, null);
            stream.flush ();
            stream.close ();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void subRedo () throws RainbowException {

    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
