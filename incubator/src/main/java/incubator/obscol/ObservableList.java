package incubator.obscol;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface ObservableList<E> extends List<E> {
	public void addObservableListListener(ObservableListListener<? super E> l);

	public void removeObservableListListener(ObservableListListener<? super E> l);

	/**
	 * Adds a property change listener.
	 * 
	 * @param pcl the property change listener to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener pcl);

	/**
	 * Removes a property change listener.
	 * 
	 * @param pcl the property change listener to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener pcl);
}
