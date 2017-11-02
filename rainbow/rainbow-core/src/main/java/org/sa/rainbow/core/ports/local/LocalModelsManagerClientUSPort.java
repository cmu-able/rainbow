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
package org.sa.rainbow.core.ports.local;

import java.util.List;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;

public class LocalModelsManagerClientUSPort implements IModelUSBusPort {

    LocalModelsManagerUSPort m_connectedPort;

    public LocalModelsManagerClientUSPort (Identifiable client) {
    }

    @Override
    public void updateModel (IRainbowOperation command) {
        if (m_connectedPort != null) {
            m_connectedPort.updateModel (command);
        }
        else
            throw new IllegalStateException ("This port is not connected to anything");

    }

    @Override
    public void updateModel (List<IRainbowOperation> commands, boolean transaction) {
        if (m_connectedPort != null) {
            m_connectedPort.updateModel (commands, transaction);
        }
        else
            throw new IllegalStateException ("This port is not connected to anything");
    }

    @Override
    public IModelInstance getModelInstance (ModelReference modelRef) {
        if (m_connectedPort != null)
            return m_connectedPort.getModelInstance (modelRef);
        else
            throw new IllegalStateException ("This port is not connected to anything");
    }

    public void connect (LocalModelsManagerUSPort localModelsManagerUSPort) {
        m_connectedPort = localModelsManagerUSPort;
    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }


}
