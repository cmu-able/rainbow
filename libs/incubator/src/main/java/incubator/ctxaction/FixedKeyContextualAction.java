package incubator.ctxaction;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>A fixed key contextual action is an action which depends on a number of
 * keys on the context. These keys are known at compile time and do not change
 * during execution. This class greatly simplifies implementation of contextual
 * actions if these conditions are verified.</p>
 * 
 * <p>Subclasses declare fields marking them with the {@link Key} annotation.
 * These fields must <em>not</em> be private, default protected or protected:
 * they should be public. Whenever the context changes, this class will
 * obtain the values of the keys from the context and place then in the fields.
 * It will then invoke the no-argument methods <code>isValid</code> and
 * <code>perform</code>: subclasses should look at their variables to decide.
 * </p>
 * 
 * <p>In order to further simplify development annotations can have an extra
 * attributes: keys can be marked as mandatory. Keys which are marked
 * as mandatory will ensure the action is not enabled and never executed
 * unless they are defined. This can be used
 * for some speed optimization in certain scenarios.</p>
 * 
 * <p>This class also simplifies exception handling as the <code>isValid</code>
 * and <code>perform</code> methods can throw any exception. A default error
 * handler is invoked (which must be defined by sub classes). If a general
 * application-level error handling strategy is defined, a subclass of this
 * class can be used as superclass for all actions.</p>
 */
public abstract class FixedKeyContextualAction extends ContextualAction {
	/**
	 * Version for serialization
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Mandatory fields.
	 */
	private Set<Field> mandatory;

	/**
	 * Creates a new key.
	 */
	public FixedKeyContextualAction() {
		processKeys();
	}
	
	@Override
	protected final boolean isValid(ActionContext context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		
		try {
			if (!fillKeys(context)) {
				return false;
			}
		
			return isValid();
		} catch (Exception e) {
			handleError(e, false);
			return false;
		}
	}

	@Override
	protected final void perform(ActionContext context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		
		try {
			if (!fillKeys(context)) {
				return;
			}
		
			perform();
		} catch (Exception e) {
			handleError(e, true);
		}
	}
	
	/**
	 * Determines whether or not the action can be executed in the current
	 * context.
	 * @return can the action be executed?
	 * @throws Exception failed to check if the action can be executed
	 */
	protected abstract boolean isValid() throws Exception;
	
	/**
	 * Executes the action.
	 * @throws Exception failed to perform the action
	 */
	protected abstract void perform() throws Exception;
	
	/**
	 * Invoked to handle an error during validation or execution.
	 * @param e the error
	 * @param duringPerform did the error occur while performing the action?
	 */
	protected abstract void handleError(Exception e, boolean duringPerform);
	
	/**
	 * Reads the key configuration from the class and fills in the internal
	 * state required for future processing.
	 */
	private void processKeys() {
		mandatory = new HashSet<>();
		
		/*
		 * Preprocess the keys.
		 */
		KeyFieldProcessor.preprocess(this);
		
		/*
		 * Start by following all classes up in the hierarchy to search for
		 * mandatory keys.
		 */
		for (Class<?> cls = getClass(); cls != null;
				cls = cls.getSuperclass()) {
			/*
			 * For each class, process each one separately.
			 */
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				/*
				 * Check we the field has the @MandatoryKey annotation. If it
				 * doesn't, ignore it. 
				 */
				if (field.getAnnotation(MandatoryKey.class) != null) {
					/*
					 * Save the field in the list.
					 */
					mandatory.add(field);
				}
			}
		}
	}
	
	/**
	 * Fills the fields with values.
	 * @param context the action context
	 * @return have all mandatory fields been filled in?
	 * @throws Exception failed to fill the keys
	 */
	private boolean fillKeys(ActionContext context) throws Exception {
		assert context != null;
		
		/*
		 * Check all fields. Ignore any problems. Just proceed with
		 * coerced values.
		 */
		KeyFieldProcessResults results = KeyFieldProcessor.process(this,
				context);
		
		Set<String> empty = new HashSet<>();
		empty.addAll(results.getCoersionFailed());
		empty.addAll(results.getPrimitiveNulls());
		empty.addAll(results.getSuccessNull());
		
		/*
		 * Check if we're failing any mandatory field.
		 */
		for (Field f : mandatory) {
			if (empty.contains(f.getName())) {
				return false;
			}
		}
		
		return true;
	}
}
