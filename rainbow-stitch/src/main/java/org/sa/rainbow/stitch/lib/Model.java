/**
 * Created September 2006, renamed to Model on April 27, 2007.
 */
package org.sa.rainbow.stitch.lib;

import java.util.Set;

import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.apache.commons.lang.NotImplementedException;

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
                    sum += ((IAcmeFloatValue )val).getValue();
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

    public static int availableServices (IAcmeElementType<?,?> elemType) {
//		return ((IModelManager )Oracle.instance().modelManager()).availableServices(elemType);
        throw new NotImplementedException ("Model.availableServices");

    }

    public static Set<IAcmeElementInstance<?,?>> findServices (IAcmeElementType<?,?> type) {
//		Map<String,String> filters = new HashMap<String,String>();
//		return ((IModelManager )Oracle.instance().modelManager()).findServices(type, filters);
        throw new NotImplementedException ("Model.findServices");
    }

}
