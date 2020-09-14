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
package org.sa.rainbow.core.models.commands;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.util.Util;

public abstract class ModelCommandFactory<T> {
	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface Operation {
		String name();
	}

	@Retention(RUNTIME)
	@Target(METHOD)
	public @interface LoadOperation {

	}

	private final Class<? extends IModelInstance<T>> m_instanceClass;
	protected final IModelInstance<T> m_modelInstance;
	private final Map<String, Class<? extends AbstractRainbowModelOperation<?, T>>> m_commandMap = new HashMap<>();

	/**
	 * Is used by the ModelsManager to load a model into Rainbow. The ModelsManager
	 * looks for this method through reflection on the command factory.
	 * 
	 * @param modelsManager The ModelsManager that is loading the model
	 * @param modelName     The name by which the model will be referred in the
	 *                      ModelsManager
	 * @param stream        A stream that can be used to load the model
	 * @param source        The original source of the model (e.g., its filename)
	 * @return Returns the command used by the ModelsManager to load the model
	 */
	@LoadOperation
	public static AbstractLoadModelCmd<?> loadCommand(ModelsManager modelsManager, String modelName, InputStream stream,
			String source) {
		throw new UnsupportedOperationException("This method should be implemented in all subclasses and wasn't");
	}

	public ModelCommandFactory(Class<? extends IModelInstance<T>> instanceClass, IModelInstance<T> model)
			throws RainbowException {
		m_instanceClass = instanceClass;
		m_modelInstance = model;
		fillInCommandMap();
		if (Arrays.asList(this.getClass().getMethods()).stream().filter(m -> {
			return m.isAnnotationPresent(LoadOperation.class);
		}).findAny().orElse(null) == null) {
			throw new RainbowException("Method does not have a method to load the model");
		}

	}

	public Map<String, Class<? extends AbstractRainbowModelOperation<?, T>>> getCommands() {
		return Collections.unmodifiableMap(m_commandMap);
	}

	private void fillInCommandMap() throws RainbowException {
		List<Method> cmds = Util.getMethodsAnnotatedWith(this.getClass(), Operation.class);
//		Type t = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		cmds.forEach(m -> {
			Class<?> returnType = m.getReturnType();
			if (AbstractRainbowModelOperation.class.isAssignableFrom(returnType)) {
//				Type sct = returnType.getGenericSuperclass();
//				if (ParameterizedType.class.isAssignableFrom(sct.getClass())
//				/* && ((ParameterizedType) sct).getActualTypeArguments()[1] == t */)
					m_commandMap.put(m.getAnnotation(Operation.class).name().toLowerCase(),
							(Class<? extends AbstractRainbowModelOperation<?, T>>) returnType);

			}

		});
		if (m_commandMap.isEmpty()) {
			throw new RainbowException("Model Factory has no operations. (Need to be annotated with @Operation.)");
		}
	}

	/**
	 * Given a command name and a set of arguments (where the first argument is
	 * usually the target), return the appropriate model operation. This method
	 * looks in the command map to find the appropriate implementation of
	 * IRainbowModelOperation to construct and return. If it is not found in the
	 * command map, reflection is used to search for methods on the command factory
	 * that match the name of the operation and constructs the operation based on
	 * the return type of this method. If an operation is called "xxx", reflection
	 * looks for a method called xxxCmd.
	 * 
	 * @param commandName The name of the command
	 * @param args        The argument to use in the constructor
	 * @return A model operation that will execute the command with the arguments
	 * @throws RainbowModelException When the commandName cannot be found
	 */
	public IRainbowModelOperation<?, T> generateCommand(String commandName, String... args)
			throws RainbowModelException {
		try {
			Class<? extends AbstractRainbowModelOperation<?, T>> cmdClass = m_commandMap.get(commandName.toLowerCase());
			if (cmdClass == null) {
				cmdClass = tryThroughReflection(commandName);
			}
			if (cmdClass == null)
				throw new RainbowModelException("Cannot find a command that matches " + commandName);
			Constructor<? extends AbstractRainbowModelOperation<?, T>>[] constructors = (Constructor<? extends AbstractRainbowModelOperation<?, T>>[]) cmdClass
					.getConstructors();
			Constructor<? extends AbstractRainbowModelOperation<?, T>> constructor = null;
			for (Constructor<? extends AbstractRainbowModelOperation<?, T>> c : constructors) {
				Class<?>[] parameterTypes = c.getParameterTypes();
				if (parameterTypes[0] == m_instanceClass) {
					final Class<?>[] a2 = Arrays.copyOfRange (parameterTypes, 0, 2);
	                
	                // For backwards compatibility
	                if (Arrays.equals (new Class<?>[]{m_instanceClass, String.class},
	                                   a2)) {
	                    if (parameterTypes.length == 1 + args.length) {
	                        constructor = c;
	                        break;
	                    }
	                }
	                else {
	                	// Check instance class assignable
	                	if (a2[0].isAssignableFrom(m_instanceClass) && a2[1] == String.class) {
	                		if (parameterTypes.length == 1 + args.length) {
	                			constructor = c;
	                			break;
	                		}
	                	}
	                }
				} else {
					final Class<?>[] a2 = Arrays.copyOfRange(parameterTypes, 0, 3);

					if (Arrays.equals(new Class<?>[] { String.class, m_instanceClass, String.class }, a2)) {
						if (parameterTypes.length == 2 + args.length) {
							constructor = c;
							break;
						}
					} else {
						// Check instance class assignable
						if (a2[0] == String.class && a2[1].isAssignableFrom(m_instanceClass) && a2[2] == String.class) {
							if (parameterTypes.length == 1 + args.length) {
								constructor = c;
								break;
							}
						}
					}
				}
			}
			if (constructor == null)
				throw new NoSuchMethodException("Could not find a constructor for " + cmdClass.getName() + " ("
						+ m_instanceClass.getName() + ", String, String ...)");
			Object[] cargs = new Object[2 + args.length];
//            cargs[0] = commandName;
			cargs[0] = commandName;
			cargs[1] = m_modelInstance;
			System.arraycopy(args, 0, cargs, 2, args.length);
			return constructor.newInstance(cargs/*
												 * commandName, m_modelInstance.getModelInstance (), args
												 */);
		} catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new RainbowModelException("Cannot create a command for the commandName: " + commandName, e);
		}
	}

	private Class<? extends AbstractRainbowModelOperation<?, T>> tryThroughReflection(String commandName) {
		Method[] methods = this.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equals(commandName + "Cmd")) {
				return (Class<? extends AbstractRainbowModelOperation<?, T>>) method.getReturnType();
			}
		}
		return null;
	}

	public abstract AbstractSaveModelCmd<T> saveCommand(String location) throws RainbowModelException;

}
