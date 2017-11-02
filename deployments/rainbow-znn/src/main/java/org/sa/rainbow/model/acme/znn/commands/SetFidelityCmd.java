/*
 * The MIT License
 *
 * Copyright 2014 CMU Aimport java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
otice shall be included in
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
package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public class SetFidelityCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private int    m_fidelity;

    public SetFidelityCmd (AcmeModelInstance model, String server, String fidelity) {
        super ("setFidelity", model, server, fidelity);
        m_server = server;
        m_fidelity = Integer.valueOf (fidelity);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (m_server, IAcmeComponent.class);
        if (server == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' could not be found in the model", getTarget ()));
        if (!server.declaresType ("ArchElementT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''fidelity''",
                    getTarget ()));
        IAcmeProperty property = server.getProperty ("fidelity");
        try {
            IAcmeIntValue acmeVal = PropertyHelper.toAcmeVal (m_fidelity);
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            if (propertyValueChanging (property, acmeVal)) {
                m_command = server.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
                cmds.add (m_command);
            }
            return cmds;
        }
        catch (Exception e) {
            throw new RainbowModelException (MessageFormat.format ("Could not parse ''{0}'' as an integer.",
                    getParameters ()[0]));
        }
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
