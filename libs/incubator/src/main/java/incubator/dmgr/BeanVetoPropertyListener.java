package incubator.dmgr;

import java.beans.PropertyVetoException;

/**
 * The interface that all objects must implement to be listeners of
 * a DataManager. The listeners that implement this interface 
 * receive requests for add, remove and update and can veto these
 * updates to the list of objects.
 * The classes implementing this interface should raise a veto 
 * exception.
 */
public interface BeanVetoPropertyListener {
	/**
	 * This method is called when a object is going to be changed.
	 * 
	 * @param changes set of properties that are going to be changed 
	 * (old and new)
	 * 
	 * @throws PropertyVetoException throws an exception if it veto's the change
	 */
	void propertiesChanged(BeanPropertyChange changes) 
		throws PropertyVetoException;
	/**
	 * This method is called when a object is added.
	 * 
	 * @param object that is going to be added
	 * 
	 * @throws PropertyVetoException throws an exception if it veto's the change
	 */
	void objectAdded(Object object) throws PropertyVetoException;
	/**
	 * This method is called when a object is removed.
	 * 
	 * @param object that is going to be removed
	 * 
	 * @throws PropertyVetoException throws an exception if it veto's the change
	 */
	void objectRemoved(Object object) throws PropertyVetoException;
}
