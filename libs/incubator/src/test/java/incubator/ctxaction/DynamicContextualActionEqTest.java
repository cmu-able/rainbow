package incubator.ctxaction;

import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.DynamicContextualAction;
import incubator.ctxaction.DynamicContextualActionErrorHandler;
import incubator.ctxaction.DynamicContextualActionExecuter;
import incubator.ctxaction.DynamicContextualActionValidator;

/**
 * Equivalence class tests for the dynamic contextual action.
 */
public class DynamicContextualActionEqTest extends Assert {
	/**
	 * Disables the AWT thread check.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}

	/**
	 * Creates a dynamic contextual action with beanshell validation and
	 * execution.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void bshValidationAndExecution() throws Exception {
		boolean res[] = new boolean[1];
		ActionContext ac = new ActionContext();
		ac.set("bar", res);
		DynamicContextualAction dca = new DynamicContextualAction(
				"incubator.ctxaction.test-2", "bar");
		dca.bind(ac);

		assertFalse(dca.isEnabled());
		ac.set("foo", false);
		assertFalse(dca.isEnabled());
		ac.set("foo", true);
		assertTrue(dca.isEnabled());
		dca.actionPerformed(new ActionEvent(this, 0, ""));
		assertTrue(res[0]);
	}

	/**
	 * Creates a dynamic contextual action with java validation and
	 * execution.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void javaValidationAndExecution() throws Exception {
		boolean res[] = new boolean[1];
		ActionContext ac = new ActionContext();
		ac.set("bar", res);
		DynamicContextualAction dca = new DynamicContextualAction(
				"incubator.ctxaction.test-2", "barbar");
		dca.bind(ac);

		assertFalse(dca.isEnabled());
		ac.set("foo", false);
		assertFalse(dca.isEnabled());
		ac.set("foo", true);
		assertTrue(dca.isEnabled());
		dca.actionPerformed(new ActionEvent(this, 0, ""));
		assertTrue(res[0]);
	}

	/**
	 * Creates a dynamic contextual action with bean shell validation,
	 * execution and error handling. The action throws an exception which is
	 * captured in the error handler.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void bshErrorHandling() throws Exception {
		Throwable caught[] = new Throwable[1];
		ActionContext ac = new ActionContext();
		ac.set("caught", caught);
		DynamicContextualAction dca = new DynamicContextualAction(
				"incubator.ctxaction.test-2", "bsh-eh");
		dca.bind(ac);
		assertTrue(dca.isEnabled());
		dca.actionPerformed(new ActionEvent(this, 0, ""));
		assertNotNull(caught[0]);
		assertEquals("failed1", caught[0].getMessage());
	}

	/**
	 * Creates a dynamic contextual action with bean shell validation,
	 * execution and a java error handling procedure. The action throws an
	 * exception which is captured in the error handler.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void javaErrorHandling() throws Exception {
		ActionContext ac = new ActionContext();
		DynamicContextualAction dca = new DynamicContextualAction(
				"incubator.ctxaction.test-2", "java-eh");
		dca.bind(ac);
		assertTrue(dca.isEnabled());
		dca.actionPerformed(new ActionEvent(this, 0, ""));
		assertNotNull(TestErrorHandler.caught);
		assertEquals("failed2", TestErrorHandler.caught.getMessage());
	}

	/**
	 * Validator for java validation testing.
	 */
	public static class TestValidator implements
			DynamicContextualActionValidator {
		@Override
		public boolean isValid(ActionContext context) {
			return context.get("foo") != null
					&& (Boolean) context.get("foo");
		}
	}

	/**
	 * Executer for java validation testing.
	 */
	public static class TestExecuter implements
			DynamicContextualActionExecuter {
		@Override
		public void execute(ActionContext context) {
			((boolean[]) context.get("bar"))[0] = true;
		}
	}

	/**
	 * Error handler for java error handling.
	 */
	public static class TestErrorHandler implements
			DynamicContextualActionErrorHandler {
		/**
		 * The caught exception.
		 */
		public static Throwable caught;

		@Override
		public void handleError(ActionContext context, Throwable throwable) {
			caught = throwable;
		}
	}
}
