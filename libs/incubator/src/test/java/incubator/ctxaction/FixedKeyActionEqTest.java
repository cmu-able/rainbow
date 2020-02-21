package incubator.ctxaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.event.ActionEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * Equivalence class tests for fixed key actions.
 */
public class FixedKeyActionEqTest {
	/**
	 * Prepares the test.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}
	
	/**
	 * Tests creating an action with no keys defined. Creates an action context
	 * and registers an action which has no keys defined. Is valid should be
	 * invoked and, when the action is performed, the perform method should be
	 * invoked.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void noKeysDefined() throws Exception {
		/**
		 * Declare a new action which will keep track of invocation count.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * Number of times isValid has been invoked.
			 */
			int validCount;
			
			/**
			 * Number of times perform has been invoked.
			 */
			int performCount;
			
			/**
			 * Creates a new test action.
			 */
			TestAction() {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			protected void handleError(Exception t, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				validCount++;
				return true;
			}

			@Override
			protected void perform() throws Exception {
				performCount++;
			}
		}
		
		ActionContext ctx = new ActionContext();
		TestAction ta = new TestAction();
		ta.bind(ctx);
		assertEquals(1, ta.validCount);
		assertEquals(0, ta.performCount);
		
		ta.actionPerformed(new ActionEvent(new Object(), 0, ""));
		assertEquals(1, ta.validCount);
		assertEquals(1, ta.performCount);
	}
	
	/**
	 * Checks that if validate meets minimum requirements, the subclass'
	 * validate action is invoked.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void delegateValidate() throws Exception {
		/**
		 * Declare a new action which will keep track of invocation count.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * Number of times isValid has been invoked.
			 */
			int validCount;
			
			/**
			 * Dummy key variable.
			 */
			@MandatoryKey
			@Key(contextKey = "foo")
			public int someKey;
			
			/**
			 * Creates a new test action.
			 */
			TestAction() {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			protected void handleError(Exception t, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				validCount++;
				return someKey > 0;
			}

			@Override
			protected void perform() throws Exception {
				/*
				 * Nothing to do.
				 */
			}
		}
		
		ActionContext ctx = new ActionContext();
		TestAction ta = new TestAction();
		ta.bind(ctx);
		assertFalse(ta.isEnabled());
		assertEquals(0, ta.validCount);
		ctx.set("foo", 12);
		assertTrue(ta.isEnabled());
		assertEquals(1, ta.validCount);
	}
	
	/**
	 * If an exception is raised during validation, the error handling method is
	 * invoked.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void errorHandlingIsDelegated() throws Exception {
		/**
		 * Declare a new action which will keep track of invocation count.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;
			
			/**
			 * Some key to ensure we force revalidating.
			 */
			@Key(contextKey = "foo")
			public String foo;

			/**
			 * Exception reported.
			 */
			Throwable ex;
			
			/**
			 * Was the exception thrown when performing?
			 */
			boolean performing;
			
			/**
			 * Has valid thrown an exception?
			 */
			boolean validThrown;
			
			/**
			 * Creates a new test action.
			 */
			TestAction() {
				/*
				 * Nothing to do.
				 */
			}
			
			@Override
			protected void handleError(Exception t, boolean duringPerform) {
				assertNull(ex);
				ex = t;
				performing = duringPerform;
			}

			@Override
			protected boolean isValid() throws Exception {
				if (validThrown) {
					return true;
				}
				
				foo = "x" + foo;
				validThrown = true;
				throw new Exception("valid");
			}

			@Override
			protected void perform() throws Exception {
				throw new Exception("perform");
			}
		}
		
		ActionContext ctx = new ActionContext();
		TestAction ta = new TestAction();
		assertNull(ta.ex);
		
		ta.bind(ctx);
		assertNotNull(ta.ex);
		assertFalse(ta.performing);
		assertFalse(ta.isEnabled());
		
		/*
		 * Force reload.
		 */
		ta.ex = null;
		ctx.set("foo", "foo");
		assertNull(ta.ex);
		assertTrue(ta.isEnabled());
		
		ta.actionPerformed(new ActionEvent(new Object(), 1, "foo"));
		assertNotNull(ta.ex);
		assertTrue(ta.performing);
		assertTrue(ta.isEnabled());
	}
	
	/**
	 * Fields declared in super classes are also caught in processing.
	 * 
	 * @throws Exception test failed
	 */
	public void superClassFields() throws Exception {
		/**
		 * Declare a new action which will keep track of invocation count.
		 */
		abstract class TestSuperAction extends FixedKeyContextualAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;
			
			/**
			 * Some key to test.
			 */
			@Key(contextKey = "foo")
			@MandatoryKey
			public String foo;
			
			@Override
			protected void handleError(Exception e, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				assertNotNull(foo);
				return true;
			}

			@Override
			protected abstract void perform() throws Exception;
		}
		
		/**
		 * Sub class used for action.
		 */
		class TestAction extends TestSuperAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;
			
			/**
			 * Some other key.
			 */
			@Key(contextKey = "bar")
			@MandatoryKey
			public String bar;

			@Override
			protected void perform() throws Exception {
				bar = bar + "";
			}
		}
		
		ActionContext ctx = new ActionContext();
		TestAction ta = new TestAction();
		ta.bind(ctx);
		assertFalse(ta.isEnabled());
		
		ctx.set("bar", "bar");
		assertFalse(ta.isEnabled());
		
		ctx.set("foo", "foo");
		ctx.clear("bar");
		assertFalse(ta.isEnabled());
		
		ctx.set("bar", "bar");
		assertTrue(ta.isEnabled());
	}
	
	/**
	 * If the context has values with a different (or non-coercible) type,
	 * no error is generated but the values are treated as empty.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void wrongTypesAreAccepted() throws Exception {
		/**
		 * Declare a new action.
		 */
		class TestAction extends FixedKeyContextualAction {
			/**
			 * Serial version UID.
			 */
			private static final long serialVersionUID = 1L;
			
			/**
			 * A string key
			 */
			@Key(contextKey = "foo")
			public String foo;
			
			/**
			 * A double key.
			 */
			@Key(contextKey = "bar")
			public double bar;
			
			/**
			 * Values reported in validation.
			 */
			protected Object[] reported;
			
			@Override
			protected void handleError(Exception t, boolean duringPerform) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			protected boolean isValid() throws Exception {
				reported = new Object[] { foo, bar };
				return true;
			}

			@Override
			protected void perform() throws Exception {
				/*
				 * Nothing to do.
				 */
			}
		}
		
		ActionContext ctx = new ActionContext();
		TestAction ta = new TestAction();
		ta.bind(ctx);
		
		assertNotNull(ta.reported);
		assertEquals(2, ta.reported.length);
		assertNull(ta.reported[0]);
		assertEquals(0.0, ta.reported[1]);
		ta.reported = null;
		
		ctx.set("foo", new Integer(12));
		assertNotNull(ta.reported);
		assertEquals(2, ta.reported.length);
		assertNull(ta.reported[0]);
		assertEquals(0.0, ta.reported[1]);
		ta.reported = null;
		
		ctx.set("bar", "14");
		assertNotNull(ta.reported);
		assertEquals(2, ta.reported.length);
		assertNull(ta.reported[0]);
		assertEquals(0.0, ta.reported[1]);
		ta.reported = null;
		
		ctx.set("foo", "foo");
		assertNotNull(ta.reported);
		assertEquals(2, ta.reported.length);
		assertEquals("foo", ta.reported[0]);
		assertEquals(0.0, ta.reported[1]);
		ta.reported = null;

		ctx.set("bar", 4.5);
		assertNotNull(ta.reported);
		assertEquals(2, ta.reported.length);
		assertEquals("foo", ta.reported[0]);
		assertEquals(4.5, ta.reported[1]);
		ta.reported = null;
	}
}
