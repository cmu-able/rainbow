package incubator.ctxaction;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextListener;

/**
 * Action context listener used for testing. Keeps track of its invocations.
 */
public class TestActionContextListener implements ActionContextListener {
	/**
	 * List with one context per invocation. Saves the context which was called
	 * in the invocation.
	 */
	public List<ActionContext> invocations;
	
	/**
	 * Creates a new test listener.
	 */
	public TestActionContextListener() {
		invocations = new ArrayList<>();
	}
	
	@Override
	public void contextChanged(ActionContext context) {
		Assert.assertNotNull(context);
		invocations.add(context);
	}
}
