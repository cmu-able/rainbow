package incubator.dmgr;

import org.junit.Assert;
import org.junit.Test;

/**
 * Robustness tests for the {@link BeanPropertyChange} class.
 */
public class BeanPropertyChangeRbTest extends Assert {
	/**
	 * Cannot create a bean property change with <code>null</code> for first argument.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullArg1() throws Exception {
		new BeanPropertyChange(null, null);
	}
	
	/**
	 * Cannot create a bean property change with <code>null</code> for second argument.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullArg2() throws Exception {
		new BeanPropertyChange(new Object(), null);
	}	
}
