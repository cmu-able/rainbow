package org.sa.rainbow.configuration;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

public class Utils {

	public static <T extends EObject> T getContainerOfType(EObject ele, Class<T> type, Function1<EObject, Boolean> func) {
		for (EObject e = ele; e != null; e = e.eContainer()) {
			if (type.isInstance(e) && func.apply(type.cast(e))) {
				return type.cast(e);
			}
		}
		return null;
	}
	
	public static String removeQuotes(CharSequence cs) {
		if (cs == null) return null;
		String s = cs.toString();
		s = s.trim();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			return s.replaceAll("\"", "");
		}
		return s;
	}

}
