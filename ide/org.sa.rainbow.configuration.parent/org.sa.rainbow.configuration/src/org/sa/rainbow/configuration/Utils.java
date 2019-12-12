package org.sa.rainbow.configuration;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.sa.rainbow.configuration.configModel.Value;

public class Utils {

	public static <T extends EObject> T getContainerOfType(EObject ele, Class<T> type, Function1<EObject, Boolean> func) {
		for (EObject e = ele; e != null; e = e.eContainer()) {
			if (type.isInstance(e) && func.apply(type.cast(e))) {
				return type.cast(e);
			}
		}
		return null;
	}
	
	

}
