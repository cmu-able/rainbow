package incubator.dmgr;

/**
 * This class represents the changes on the properties values. In the object 
 * that is going to be changed if all veto listeners agree to.
 */
public class BeanPropertyChange {
	/** 
	 * Internal variable that stores the old value.
	 */
	private Object oldValue;
	/**
	 * Internal variable that stores the new value.
	 */
	private Object newValue;
	
	/**
	 * Constructor of the class that receives the initial values of the 
	 * objects to be changed.
	 * 
	 * @param oldValue The old object before the change
	 * @param newValue The new object after the change
	 */
	public BeanPropertyChange(Object oldValue, Object newValue) {
		// Check if the old value is not null
		if (oldValue == null) {
			throw new IllegalArgumentException("oldValue==null");
		}
		
		// Check if the new value is not null
		if (newValue == null) {
			throw new IllegalArgumentException("newValue==null");
		}		
		
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	/**
	 * Getter method that returns the value of the old object.
	 * 
	 * @return oldValue the old value is returned
	 */
	public final Object getOldValue() {
		return oldValue;
	}
	
	/**
	 * Getter method that returns the value of the new object.
	 * 
	 * @return newValue the new value is returned
	 */
	public final Object getNewValue() {
		return newValue;
	}
}
