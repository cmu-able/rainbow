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
/**
 * Created September 2006, renamed to Model on April 27, 2007.
 */
package org.sa.rainbow.stitch.lib;

import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

/**
 * This utility class provides useful operations that can be performed on the
 * architecture and environment models during strategy and tactic executions.
 * <p></p>
 * This class is not instantiable.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class Model {

    public static boolean hasType (IAcmeElementInstance<?,?> context, String typeName) {
        return context.lookupName(typeName, true) != null;
    }

    public static boolean setModelProperty (IAcmeProperty acmeProp, Object value) {
//		boolean ok = false;
//		IAcmeModel model = (IAcmeModel )Oracle.instance().rainbowModel().getAcmeModel();
//		IAcmeSystem sys = model.getSystems().iterator().next();
//		try {
//			IAcmePropertyValue pVal = Tool.convertValue(value);
//			IAcmeCommand<?> cmd = sys.getCommandFactory().propertyValueSetCommand(acmeProp, pVal);
//			cmd.execute();
//			ok = true;
//		} catch (Exception e) {
//			RainbowLoggerFactory.logger(Model.class).error("Failed setting model property "
//					+ acmeProp.getQualifiedName() + " == " + value, e);
//		}
//		return ok;
        throw new NotImplementedException ("Model.setModelProperty");
    }

    public static double sumOverProperty (String name, Set<?> set) {
        double sum = 0.0;
        for (Object o: set) {
            if (o instanceof IAcmeElementInstance<?,?>) {
                IAcmeProperty prop = ((IAcmeElementInstance<?,?> )o).getProperty(name);
                IAcmeType type = prop.getType();
                IAcmePropertyValue val = prop.getValue();
                if (type == DefaultAcmeModel.defaultIntType()) {
                    sum += ((IAcmeIntValue )val).getValue();
                } else if (type == DefaultAcmeModel.defaultFloatType()) {
                    sum += ((IAcmeFloatingPointValue) val).getDoubleValue ();
                }
            }
        }
        return sum;
    }

    /* Query operators */
    public static double predictedProperty (IAcmeProperty p, int dur) {
//		return (Double )Oracle.instance().targetSystem().predictProperty(p.getQualifiedName(), dur, StatType.SINGLE);
        throw new NotImplementedException ("Model.predictedProperty");

    }



}
