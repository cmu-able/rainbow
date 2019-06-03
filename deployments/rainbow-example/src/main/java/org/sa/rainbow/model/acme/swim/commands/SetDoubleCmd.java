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
package org.sa.rainbow.model.acme.swim.commands;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public abstract class SetDoubleCmd extends SwimAcmeModelCommand<IAcmeProperty> {

	protected String m_property; 
	
	private String m_component;
    private double m_value;

    public SetDoubleCmd (String command, String property, AcmeModelInstance model, String component, String value) {
        super (command, model, component, value);
        m_component = component;
        m_value = Double.valueOf (value);
        m_property = property;
    }
    
    /**
     * This method can be overriden by derived classes to process the value
     * before it is set in the model.
     * 
     * @param value
     * @return processed value to be set in the model
     */
    public double processValue(double value) {
    	return value;
    }
    

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (m_component, IAcmeComponent.class);
        if (server == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' could not be found in the model", getTarget ()));
        IAcmeProperty property = server.getProperty (m_property);
        if (property == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''{1}''",
                    getTarget (), m_property));
        
        try {
            IAcmeFloatingPointValue acmeVal = PropertyHelper.toAcmeVal (m_value);
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            if (propertyValueChanging (property, acmeVal)) {
                m_command = server.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
                cmds.add (m_command);
            }
            return cmds;
        }
        catch (Exception e) {
            throw new RainbowModelException (MessageFormat.format ("Error setting ACME model property ''{0}.{1}: {3}",
                    getTarget(), m_property, e.getMessage()));
        }
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
