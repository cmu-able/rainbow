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
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeUserDataCommand;
import org.sa.rainbow.core.error.RainbowModelException;

import java.util.LinkedList;
import java.util.List;

public class AcmeTypecheckSetCmd extends AcmeModelOperation<IAcmeElementExtension> {

    private final Boolean m_typechecks;

    public AcmeTypecheckSetCmd (AcmeModelInstance model, String typechecks) {
        super ("setTypecheckResult", model, "self", typechecks);
        m_typechecks = Boolean.valueOf (typechecks);
    }

    @Override
    public IAcmeElementExtension getResult () throws IllegalStateException {
        return ((IAcmeUserDataCommand )m_command).getValue ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_command = getModel ().getCommandFactory ().setElementUserData (getModel (), "TYPECHECKS",
                new RainbowModelTypecheckExtension (m_typechecks));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }


    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
