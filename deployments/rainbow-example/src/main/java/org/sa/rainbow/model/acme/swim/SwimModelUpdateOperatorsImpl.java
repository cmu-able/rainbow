/*
 * The MIT License
 *
 * Copyright 2import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.znn.commands.ZNNCommandFactory;
oftware"), to deal
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
package org.sa.rainbow.model.acme.swim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.commands.SwimCommandFactory;

import javassist.Modifier;

/**
 * An Acme model that embodies the SWIM architecture and the associated
 * style-specific operations
 * 
 * @author Bradley Schmerl: schmerl
 * @author gmoreno
 */
public class SwimModelUpdateOperatorsImpl extends AcmeModelInstance {
	private static final String MAX_KEY = "[Max]";
	private static final String AVG_KEY = "[Avg]";
	private static final String CALL_KEY = "[CALL]";

	private final Logger LOGGER = Logger.getLogger(this.getClass());

	private SwimCommandFactory m_commandFactory;

	public SwimModelUpdateOperatorsImpl(IAcmeSystem system, String source) {
		super(system, source);
		// Make sure it is the right family
	}

	@Override
	public SwimCommandFactory getCommandFactory() throws RainbowException {
		if (m_commandFactory == null) {
			m_commandFactory = new SwimCommandFactory(this);
		}
		return m_commandFactory;
	}

	@Override
	protected AcmeModelInstance generateInstance(IAcmeSystem sys) {
		return new SwimModelUpdateOperatorsImpl(sys, getOriginalSource());
	}

	@Override
	public Object getProperty(String id) {
		if (getModelInstance() == null)
			return null;
		Object prop = null;
		if (id.startsWith(MAX_KEY) || id.startsWith(AVG_KEY) || id.startsWith(CALL_KEY)) {
			prop = internalGetProperty(id, 0);
		} else {
			prop = super.getProperty(id);
		}
		return prop;
	}

	/**
	 * Gets current or predicted property, depending on whether prediction is
	 * enabled and future duration is greater than 0.
	 * 
	 * @param id  the identifier for the property to get value for
	 * @param dur duration into the future to predict value, if applicable
	 * @return the Object that represents the value of the sought property.
	 */
	private Object internalGetProperty(String id, long dur) {
		if (dur > 0)
			throw new NotImplementedException("Prediction is not implemented");
		if (getModelInstance() == null)
			return null;
		Object prop = null;
		if (id.startsWith(MAX_KEY)) {
			// the property id is expected to be of the form <elem-type>.<prop>
			int idxStart = MAX_KEY.length();
			int idxDot = id.indexOf(".");
			if (idxDot == -1) { // property ID is not of expected form
				LOGGER.error("Unrecognized form of Max Property Name!" + id);
				return null;
			}
			String typeName = id.substring(idxStart, idxDot);
			String propName = id.substring(idxDot + 1);

			List<Double> propValues = collectInstancePropValues(typeName, propName);
			if (propValues.size() > 0) {

				// find the maximum value of the property
				double max = -Double.MAX_VALUE;
				for (Double v : propValues) {
					max = Math.max(max, v);
				}
				prop = max;
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Max Prop " + id + (dur > 0 ? "(+" + dur + ") " : "") + " requested == " + prop);
			}
		} else if (id.startsWith(AVG_KEY)) {
			// the property id is expected to be of the form <elem-type>.<prop>
			int idxStart = AVG_KEY.length();
			int idxDot = id.indexOf(".");
			if (idxDot == -1) { // property ID is not of expected form
				LOGGER.error("Unrecognized form of Average Property Name!" + id);
				return null;
			}
			String typeName = id.substring(idxStart, idxDot);
			String propName = id.substring(idxDot + 1);

			List<Double> propValues = collectInstancePropValues(typeName, propName);
			if (propValues.size() > 0) {

				// find the average value of the property
				double sum = 0.0;
				for (Double v : propValues) {
					sum += v;
				}
				prop = sum / propValues.size();
			}
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Avg Prop " + id + (dur > 0 ? "(+" + dur + ") " : "") + " requested == " + prop);
			}
		} else if (id.startsWith(CALL_KEY)) {
			int idxStart = CALL_KEY.length();
			String call = id.substring(idxStart);
			Pattern methodRegex = Pattern.compile("([^\\(]*)\\(([^\\)]*)\\)");
			Matcher m = methodRegex.matcher(call);
			if (m.matches()) {
				String method = m.group(1);
				String[] args = Arrays.stream(m.group(2).split(",")).map(String::trim).toArray(String[]::new);

				int dotIdx = method.lastIndexOf(".");
				String methodClass = null;
				if (dotIdx > -1) {
					methodClass = method.substring(0, dotIdx);
					method = method.substring(dotIdx + 1);
				}
				if (methodClass == null) {
					LOGGER.error("Could not identify a class for the method `" + method + "'");
					return null;
				}

				Class<?> clazz;
				try {
					clazz = Class.forName(methodClass);
				} catch (ClassNotFoundException e) {
					LOGGER.error(MessageFormat.format("Could not find class ''{0}''", methodClass));
					return null;
				}
				Object result = null;
				Object[] argumentsJ = new Object[args.length];
				for (int i = 0; i < argumentsJ.length; i++) {
					argumentsJ[i] = getProperty(args[i]);
				}

				for (Method mthd : clazz.getDeclaredMethods()) {
					if (mthd.getName().equals(method) && Modifier.isStatic(mthd.getModifiers())
							&& mthd.getParameterTypes().length == args.length) {
//            			Object[] argumentsJ = IntStream.range(0, args.length).
//            					<Object>mapToObj(i -> PropertyHelper.toJavaVal(getProperty(args[i]), mthd.getParameterTypes()[i])).toArray();
						try {
							result = mthd.invoke(null, argumentsJ);
							break;
						} catch (IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
						}
					}
				}
				return result;

			}

		}

		return prop;
	}

	/**
	 * Iterates through model's systems to collect all instances that either
	 * declares or instantiates the specified type name, then for all instances
	 * found that also have the specified property names, collect the property
	 * values
	 * 
	 * @param typeName the specified element type name
	 * @param propName the specified property name within relevant instance
	 * @return the list of property values
	 */

	private List<Double> collectInstancePropValues(String typeName, String propName) {
		Vector<Double> values = new Vector<>();
		boolean useSatisfies = false;
		if (typeName.startsWith("!")) {
			typeName = typeName.substring(1);
			useSatisfies = true;
		}
		// Object elemObj = m_acme.findNamedObject(m_acme, typeName);
		// if (! (elemObj instanceof IAcmeElementType<?,?>)) {
		// Debug.errorln("Element type of Average Property requested NOT found! "
		// + typeName);
		// return propKeys;
		// }
		IAcmeSystem sys = getModelInstance();
		Set<IAcmeElementInstance<?, ?>> children = new HashSet<>();
		children.addAll(sys.getComponents());
		children.addAll(sys.getConnectors());
		children.addAll(sys.getPorts());
		children.addAll(sys.getRoles());
		for (IAcmeElementInstance<?, ?> child : children) {

			// seek element with specified type AND specified property
			if ((useSatisfies && AcmeTypeHelper.satisfiesElementType(child,
					((IAcmeElementType<?, ?>) child.lookupName(typeName, true)), null)) || child.declaresType(typeName)
					|| child.instantiatesType(typeName)) {
				IAcmeProperty childProp = child.getProperty(propName);
				childProp.getValue();
				if (childProp != null) {
					if (childProp.getType() == DefaultAcmeModel.defaultIntType()) {
						values.add((double) ((IAcmeIntValue) childProp.getValue()).getValue());
					} else if (childProp.getType() == DefaultAcmeModel.defaultFloatType()) {
						values.add(((IAcmeFloatingPointValue) childProp.getValue()).getDoubleValue());
					}
				}
			}
		}
		return values;
	}
}
