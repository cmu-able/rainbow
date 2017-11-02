/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
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
package edu.cmu.rainbow_ui.display;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.core.type.IAcmeStringValue;
import org.acmestudio.acme.element.property.IAcmeProperty;

import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.common.DataValueSupport;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.property.IAcmePropertyValue;

/**
 * This class provides a method for converting AcmeProperties to DataValues
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class AcmeConverterSupport {

    /**
     * Determine the type of value, convert it and return a DataValue object
     *
     * @param val an IAcmeProperty to be converted
     * @return a DataValue object with the same value as the IAcmeProperty
     * @throws edu.cmu.rainbow_ui.display.AcmeConversionException
     */
    public static DataValue convertToDataValue(IAcmeProperty val)
            throws AcmeConversionException {
        IAcmePropertyValue av = val.getValue();
        if (av == null) {
            throw new AcmeConversionException("The property doesn't hold any value");
        }
        Object jv = PropertyHelper.toJavaVal(av);
        try {
            DataValue dv = DataValueSupport.converter.from_java(jv, null);
            return dv;
        } catch (ValueConversionException ex) {
            throw new AcmeConversionException(ex.getMessage());
        }        
    }
}
