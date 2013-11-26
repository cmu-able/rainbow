package incubator.ctxaction;

import org.junit.Before;
import org.junit.Test;

/**
 * Robustness tests for <code>FixedKeyContextualAction</code>.
 */
public class FixedKeyActionRbTest {
	/**
	 * Sets up tests.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}
	
	/**
	 * Cannot configure keys with private modifier..
	 * 
	 * @throws Exception expected
	 */
	@SuppressWarnings("unused")
	@Test(expected = KeyConfigurationException.class)
	public void privateContextKey() throws Exception  {
		/**
		 * Dummy action for test.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1;
			
			@Key(contextKey = "foo")
			private String foo;

			@Override
			protected void handleError(Exception e, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				return false;
			}

			@Override
			protected void perform() throws Exception {
				foo = foo + "";
			}
		}
		
		new TestAction();
	}
	
	/**
	 * Cannot configure keys with default protection modifier..
	 * 
	 * @throws Exception expected
	 */
	@SuppressWarnings("unused")
	@Test(expected = KeyConfigurationException.class)
	public void defaultProtectionContextKey() throws Exception  {
		/**
		 * Dummy action for test.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1;
			
			@Key(contextKey = "foo")
			String foo;

			@Override
			protected void handleError(Exception e, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				return false;
			}

			@Override
			protected void perform() throws Exception {
				foo = foo + "";
			}
		}
		
		new TestAction();
	}
	
	/**
	 * Cannot configure keys with protected modifier..
	 * 
	 * @throws Exception expected
	 */
	@SuppressWarnings("unused")
	@Test(expected = KeyConfigurationException.class)
	public void protectedContextKey() throws Exception  {
		/**
		 * Dummy action for test.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1;
			
			@Key(contextKey = "foo")
			protected String foo;

			@Override
			protected void handleError(Exception e, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				return false;
			}

			@Override
			protected void perform() throws Exception {
				foo = foo + "";
			}
		}
		
		new TestAction();
	}
}
