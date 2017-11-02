/*
 * The MIT License
 *
 * Copyright 2014 CMU Aimport java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
e shall be included in
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
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * This commands sets the model property indicating whether captcha is enabled.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class SetCaptchaEnabledCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    /**
     *  @param model
     * @param target
     *            The load balancer to set the property on
     * @param enabled
     */
    public SetCaptchaEnabledCmd (AcmeModelInstance model, String target, String enabled) {
        super ("setCaptchaEnabled", model, target, enabled);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent lb = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        if (lb == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The load balancer ''{0}'' could not be found in the model", getTarget ()));
        if (!lb.declaresType ("CaptchaRedirectT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''captchaEnabled''",
                    getTarget ()));

        List<IAcmeCommand<?>> cmds;
        try {
            boolean enabled = Boolean.valueOf (getParameters ()[0]);
            IAcmeProperty property = lb.getProperty ("captchaEnabled");
            IAcmeBooleanValue acmeVal = PropertyHelper.toAcmeVal (enabled);
            cmds = new LinkedList<> ();
            if (propertyValueChanging (property, acmeVal)) {
                m_command = lb.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
                cmds.add (m_command);
            }
        }
        catch (Exception e) {
            throw new RainbowModelException (MessageFormat.format (
                    "The parameter ''{0}'' cannot be parsed as a boolean", getParameters ()[0]));
        }
        return cmds;
    }

}
