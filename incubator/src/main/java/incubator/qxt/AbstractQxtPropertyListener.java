package incubator.qxt;

/**
 * Class that implements a listener on abstract QXT properties.
 */
public interface AbstractQxtPropertyListener {
	/**
	 * The description of the property has changed.
	 * 
	 * @param property the property
	 */
	void propertyDescriptionChanged (AbstractQxtProperty<?> property);
}
