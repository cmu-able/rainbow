package incubator.ctxaction;

import incubator.pval.Ensure;

import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * <p>
 * A dynamic contextual action is a contextual action whose validation and
 * execution codes are determined dynamically through configuration. The
 * most common way of using dynamic execution is defining the properties
 * <code>is-valid-bsh</code> and <code>execute-bsh</code> in the
 * configuration properties and set validation and execution code using
 * beanshell scripts. All context variables are set as beanshell scripts
 * variables. The execution code is not supposed to return anything but the
 * validation is expected to return a boolean value indicating whether the
 * action is valid or not.
 * </p>
 * 
 * <p>
 * If an exception is thrown, the beanshell code defined in the property
 * <code>error-handler-bsh</code> is run with the context defined as in the
 * other scripts and the variable 'thrown' defined with the throwable that
 * was raised.
 * </p>
 * 
 * <p>
 * An optional initialization script may be provided in the
 * <code>init-bsh</code> property. This code will run to initialize the
 * interpreter. An optional destruction script may be provided in the
 * <code>destroy-bsh</code> property. This code will always be run after the
 * validate or execute code.
 * </p>
 * 
 * <p>
 * If more complex code is required than what can be placed practically in a
 * beanshell script, classes may be defined that will be used for validation
 * and execution. The classes used should implement the
 * {@link DynamicContextualActionValidator} and
 * {@link DynamicContextualActionExecuter} interfaces, respectively. This
 * technique is used is <code>is-valid-java</code> is defined instead of
 * <code>is-valid-bsh</code> and <code>execute-java</code> is defined
 * instead of <code>execute-bsh</code>.
 * </p>
 * 
 * <p>
 * Error handling can also be done using Java classes instead of beanshell
 * scripts. An error handler class that implements the
 * {@link DynamicContextualActionErrorHandler} can be set by setting the
 * property <code>error-handler-java</code> to the class name of the error
 * handler.
 * </p>
 * 
 * <p>
 * Actions properties can be configured at an action or global level.
 * Configurations defined at the action level take precedence over those
 * defined at the global level.
 * </p>
 */
public class DynamicContextualAction extends ContextualAction {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Beanshell script initalization code (<code>null</code> if not
	 * defined).
	 */
	private String initBsh;

	/**
	 * Beanshell script destruction code (<code>null</code> if not defined).
	 */
	private String destroyBsh;

	/**
	 * Beanshell script for validation (<code>null</code> if not defined).
	 */
	private String validationBsh;

	/**
	 * Beanshell script for execution (<code>null</code> if not defined).
	 */
	private String executionBsh;

	/**
	 * Beanshell script for error handling (<code>null</code> if not
	 * defined).
	 */
	private String errorHandlerBsh;

	/**
	 * Java validator (<code>null</code> if not defined).
	 */
	private DynamicContextualActionValidator validator;

	/**
	 * Java executer (<code>null</code> if not defined).
	 */
	private DynamicContextualActionExecuter executer;

	/**
	 * Java error handler (<code>null</code> if not defined).
	 */
	private DynamicContextualActionErrorHandler errorHandler;

	/**
	 * Creates a new dynamic action. See superclass for details.
	 */
	public DynamicContextualAction() {
		init();
	}

	/**
	 * Creates a new dynamic action.
	 * 
	 * @param bundle the name of the resource bundle from which action data
	 * will be read. If <code>null</code>, a default bundle name (which is
	 * the class's package followed by ".action-config") is used
	 * @param name the action name. If <code>null</code>, the class name
	 * (without qualification) is used
	 */
	public DynamicContextualAction(String bundle, String name) {
		super(bundle, name);

		init();
	}

	/**
	 * Creates a new dynamic action.
	 * 
	 * @param classForBundle a class used to determine the bundle name
	 * (using the same rules as in the previous constructor)
	 * @param name the action name. If <code>null</code>, the class name
	 * (without qualification) is used
	 * @param context the execution context
	 */
	public DynamicContextualAction(Class<?> classForBundle, String name,
			ActionContext context) {
		super(classForBundle, name, context);

		init();
	}

	/**
	 * Initializes the object loading configuration.
	 */
	private void init() {
		initBsh = (String) readConfiguration("init-bsh", null, null);
		destroyBsh = (String) readConfiguration("destroy-bsh", null, null);

		Object vres = readConfiguration("is-valid-bsh", "is-valid-java",
				DynamicContextualActionValidator.class);
		if (vres != null) {
			if (vres instanceof String) {
				validationBsh = (String) vres;
				validator = null;
			} else {
				validationBsh = null;
				validator = (DynamicContextualActionValidator) vres;
			}
		}

		Object eres = readConfiguration("execute-bsh", "execute-java",
				DynamicContextualActionExecuter.class);
		if (eres != null) {
			if (eres instanceof String) {
				executionBsh = (String) eres;
				executer = null;
			} else {
				executionBsh = null;
				executer = (DynamicContextualActionExecuter) eres;
			}
		}

		Object ehres = readConfiguration("error-handler-bsh",
				"error-handler-java",
				DynamicContextualActionErrorHandler.class);
		if (ehres != null) {
			if (ehres instanceof String) {
				errorHandlerBsh = (String) ehres;
				errorHandler = null;
			} else {
				errorHandlerBsh = null;
				errorHandler = (DynamicContextualActionErrorHandler) ehres;
			}
		}
	}

	/**
	 * Reads a configuration parameter. Tries the bean shell configuration
	 * of the action, if not defined, tries the java configuration of the
	 * action. If none is defined, tries the global bean shell configuration
	 * and, at the end, the global java configuration.
	 * 
	 * @param bshName the bean shell parameter name
	 * @param javaName the java class parameter name (may be
	 * <code>null</code> is a java class is not expected)
	 * @param javaClass the expected java code (may be <code>null</code> if
	 * a java class is not expected)
	 * 
	 * @return either the bean shell script (a <code>String</code>) or the
	 * java object. Returns <code>null</code> if none is defined
	 */
	private Object readConfiguration(String bshName, String javaName,
			Class<?> javaClass) {
		assert bshName != null;
		assert javaName == null && javaClass == null || javaName != null
				&& javaClass != null;

		/*
		 * Try action bean shell script.
		 */
		String bstr = getOptionalConfig(bshName);
		if (bstr != null) {
			return bstr;
		}

		/*
		 * Try action java class.
		 */
		if (javaName != null) {
			String javaClassName = getOptionalConfig(javaName);
			if (javaClassName != null) {
				
				/*
				 * We know this is always true but the compiler doesn't care
				 * and warns that javaClass may be null...
				 */
				Ensure.not_null(javaClass, "unexpected javaClass == null");
				if (javaClass != null) {
					try {
						Class<?> cls = Class.forName(javaClassName);
						if (javaClass.isAssignableFrom(cls)) {
							return cls.newInstance();
						}
					} catch (Exception e) {
						// FIXME: Handle exception.
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		/*
		 * Try global bean shell script.
		 */
		bstr = getOptionalGlobalConfig(bshName);
		if (bstr != null) {
			return bstr;
		}

		/*
		 * Try global java class.
		 */
		if (javaName != null) {
			String javaClassName = getOptionalGlobalConfig(javaName);
			if (javaClassName != null) {
				
				/*
				 * We know this is always true but the compiler doesn't care
				 * and warns that javaClass may be null...
				 */
				Ensure.not_null(javaClass, "unexpected javaClass == null");
				if (javaClass != null) {
					try {
						Class<?> cls = Class.forName(javaClassName);
						if (javaClass.isAssignableFrom(cls)) {
							return cls.newInstance();
						}
					} catch (Exception e) {
						// FIXME: Handle exception.
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Creates a beanshell interpreter.
	 * 
	 * @param context the action context.
	 * 
	 * @return the created interpreter or <code>null</code> if creation
	 * failed
	 */
	private Interpreter createInterpreter(ActionContext context) {
		Interpreter bsh = new Interpreter();

		try {
			for (Map.Entry<String, Object> e : context.getAll().entrySet()) {
				String key = e.getKey();
				Object value = e.getValue();
				bsh.set(key, value);
			}

			if (initBsh != null) {
				bsh.eval(initBsh);
			}
		} catch (EvalError e) {
			// FIXME: Handle this decently.
			e.printStackTrace();
		}

		return bsh;
	}

	/**
	 * Destroys a created interpreter.
	 * 
	 * @param bsh the interpreter
	 */
	private void destroyInterpreter(Interpreter bsh) {
		assert bsh != null;

		if (destroyBsh != null) {
			try {
				bsh.eval(destroyBsh);
			} catch (EvalError e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean isValid(ActionContext context) {
		assert context != null;

		if (validationBsh == null && validator == null) {
			return false;
		}

		if (validator != null) {
			return validator.isValid(context);
		}

		Interpreter bsh = createInterpreter(context);
		if (bsh == null) {
			return false;
		}

		try {
			Object computed = bsh.eval(validationBsh);
			if (computed != null && computed instanceof Boolean) {
				return (Boolean) computed;
			} else {
				return false;
			}
		} catch (EvalError ex) {
			try {
				handleError(context, ex, true);
			} catch (Throwable t) {
				assert false;
			}

			return false;
		} finally {
			destroyInterpreter(bsh);
		}
	}

	@Override
	protected void perform(ActionContext context) {
		try {
			perform(context, true);
		} catch (Throwable t) {
			assert false;
		}
	}

	/**
	 * Executes the action.
	 * 
	 * @param context the execution context
	 * @param handleException should exceptions be handled automatically?
	 * 
	 * @throws Throwable an error occurred during action execution
	 */
	private void perform(ActionContext context, boolean handleException)
			throws Throwable {
		assert context != null;

		if (executionBsh == null && executer == null) {
			return;
		}

		if (executer != null) {
			try {
				executer.execute(context);
			} catch (Throwable t) {
				handleError(context, t, handleException);
			}
			return;
		}

		Interpreter bsh = createInterpreter(context);
		if (bsh == null) {
			return;
		}

		try {
			bsh.eval(executionBsh);
		} catch (EvalError ex) {
			if (ex instanceof TargetError) {
				TargetError te = (TargetError) ex;
				handleError(context, te.getTarget(), handleException);
			} else {
				handleError(context, ex, handleException);
			}
		}

		destroyInterpreter(bsh);
	}

	/**
	 * Handles an exception thrown while executing an action.
	 * 
	 * @param context the action context
	 * @param t the exception thrown
	 * @param handleException should exceptions be handled automatically?
	 * (if <code>false</code> the throwable is thrown again)
	 * 
	 * @throws Throwable if <code>handleException</code> is
	 * <code>false</code> the throwable is just rethrown
	 */
	private void handleError(ActionContext context, Throwable t,
			boolean handleException) throws Throwable {
		assert context != null;
		assert t != null;

		if (!handleException) {
			throw t;
		}

		if (errorHandler != null) {
			errorHandler.handleError(context, t);
			return;
		}

		if (errorHandlerBsh != null) {
			try {
				Interpreter bsh = createInterpreter(context);
				bsh.set("thrown", t);
				bsh.eval(errorHandlerBsh);
			} catch (EvalError ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Programatically for the action to be executed (if it is valid).
	 * 
	 * @param handleException should exceptions be handled automatically by
	 * the action's error handler?
	 * 
	 * @throws Throwable execution error (only thrown if
	 * <code>handleException</code> is <code>false</code>
	 */
	public void programaticExecute(boolean handleException) throws Throwable {
		if (isValid(getActionContext())) {
			perform(getActionContext(), handleException);
		}
	}
}
