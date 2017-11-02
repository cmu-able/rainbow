package incubator.ctxaction;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import incubator.ctxaction.ActionContext;

/**
 * Robustness tests for the {@link ActionContext} class.
 */
public class ActionContextRbTest extends Assert {
	/**
	 * Cannot add <code>null</code> listener.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void addingNullListeners() throws Exception {
		ActionContext ac = new ActionContext();
		ac.addActionContextListener(null);
	}
	
	/**
	 * Cannot remove <code>null</code> listener.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void removingNullListeners() throws Exception {
		ActionContext ac = new ActionContext();
		ac.removeActionContextListener(null);
	}
	
	/**
	 * Cannot clear with <code>null</code> key.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void clearingWithNullKey() throws Exception {
		ActionContext ac = new ActionContext();
		ac.clear(null);
	}
	
	/**
	 * Cannot set <code>null</code> key.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void settingWithNullKey() throws Exception {
		ActionContext ac = new ActionContext();
		ac.set(null, "foo");
	}
	
	/**
	 * Cannot redefine with <code>null</code> key array.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithNullKeyArray() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine(null, new Object[0]);
	}
	
	/**
	 * Cannot redefine with <code>null</code> parameter array.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithNullParameterArray() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine(new String[0], null);
	}
	
	/**
	 * Cannot redefine with <code>null</code>s in the key array.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithNullKeys() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine(new String[] { "foo", null }, new Object[] { 1, 2 });
	}
	
	/**
	 * Cannot redefine if the key and value arrays have different lengths.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithDifferentArrayLengths() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine(new String[] { "foo" }, new Object[] { 1, 2 });
	}
	
	/**
	 * Cannot redefine with <code>null</code> map.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithNullMap() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine((Map<String, Object>) null);
	}
	
	/**
	 * Cannot redefine with <code>null</code> context.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void redefiningWithNullContext() throws Exception {
		ActionContext ac = new ActionContext();
		ac.redefine((ActionContext) null);
	}
	
	/**
	 * Cannot get with <code>null</code> key.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected=IllegalArgumentException.class)
	public void gettingWithNullKey() throws Exception {
		ActionContext ac = new ActionContext();
		ac.get(null);
	}
}
