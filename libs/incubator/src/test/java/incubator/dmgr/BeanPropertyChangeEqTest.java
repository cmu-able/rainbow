package incubator.dmgr;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Equivalence class testing for the {@link BeanPropertyChange} class.
 */
public class BeanPropertyChangeEqTest extends Assert {
	/**
	 * Data manager to be used in the tests.
	 */
	private BeanPropertyChange bPropChange;
	
	/**
	 * Creates a new bean property and verifies that the arguments
	 * were added correctly.
	 */
	@Test
	public final void createBeanPropertyChange() {
		final int sizeString = 10;
		
		String auxOld = RandomStringUtils.randomAlphabetic(sizeString);
		String auxNew = RandomStringUtils.randomAlphabetic(sizeString);
		bPropChange = new BeanPropertyChange(auxOld, auxNew);
		
		assert (bPropChange.getOldValue().equals(auxOld));
		assert (bPropChange.getNewValue().equals(auxNew));
	}
}
