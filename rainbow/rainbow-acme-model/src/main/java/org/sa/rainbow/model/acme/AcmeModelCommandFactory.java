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
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class AcmeModelCommandFactory extends ModelCommandFactory<IAcmeSystem> {

    public static final String SET_TYPECHECK_RESULT_CMD = "setTypecheckResult";


	public AcmeModelCommandFactory (AcmeModelInstance model) throws RainbowException {
        super (AcmeModelInstance.class, model);
    }


    @Operation(name=SET_TYPECHECK_RESULT_CMD)
    public AcmeTypecheckSetCmd setTypecheckResultCmd (IAcmeSystem system, boolean typechecks) {
        return new AcmeTypecheckSetCmd (SET_TYPECHECK_RESULT_CMD, (AcmeModelInstance) m_modelInstance, "self",
                Boolean.toString (typechecks));
    }




    @Override
    public AcmeSaveModelCommand saveCommand ( String location) throws RainbowModelException {
        try {
            FileOutputStream stream = new FileOutputStream (location);
            return new AcmeSaveModelCommand (m_modelInstance.getModelName (),
                    (AcmeModelInstance )m_modelInstance, stream);
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}
