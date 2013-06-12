package incubator.ctxaction;

import org.junit.Assert;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;

/**
 * Robustness tests for the {@link ContextualAction} class.
 */
public class ContextualActionRbTest extends Assert {
	/**
	 * Creates a contextual action with <code>null</code> template.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createActionWithNullTemplate() throws Exception {
		new ContextualAction(null) {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValid(ActionContext context) {
				return false;
			}

			@Override
			protected void perform(ActionContext context) {
				/*
				 * Nothing to do.
				 */
			}
		};
	}
	
	/**
	 * Adds a <code>null</code> listener to a contextual action.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addNullListener() throws Exception {
		ContextualAction action = new ContextualAction() {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValid(ActionContext context) {
				/*
				 * Nothing to do.
				 */
				return false;
			}

			@Override
			protected void perform(ActionContext context) {
				/*
				 * Nothing to do.
				 */
			}
		};
		
		action.addContextualActionListener(null);
	}
	
	/**
	 * Removes a <code>null</code> listener to a contextual action.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeNullListener() throws Exception {
		ContextualAction action = new ContextualAction() {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValid(ActionContext context) {
				/*
				 * Nothing to do.
				 */
				return false;
			}

			@Override
			protected void perform(ActionContext context) {
				/*
				 * Nothing to do.
				 */
			}
		};
		
		action.removeContextualActionListener(null);
	}
	
	/**
	 * Removes an unregistered listener from a contextual action.
	 * 
	 * @throws IllegalStateException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalStateException.class)
	public void removeNonRegisteredListener() throws Exception {
		ContextualAction action = new ContextualAction() {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isValid(ActionContext context) {
				/*
				 * Nothing to do.
				 */
				return false;
			}

			@Override
			protected void perform(ActionContext context) {
				/*
				 * Nothing to do.
				 */
			}
		};
		
		ContextualActionListener listener = new ContextualActionListener() {
			@Override
			public void actionPerformed() {
				// TODO Auto-generated method stub
			}
		};
		
		action.removeContextualActionListener(listener);
	}
}
