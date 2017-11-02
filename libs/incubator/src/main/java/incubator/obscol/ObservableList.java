package incubator.obscol;

import java.beans.PropertyChangeListener;
import java.util.List;

public interface ObservableList<E> extends List<E> {
	void addObservableListListener (ObservableListListener<? super E> l);

	void removeObservableListListener (ObservableListListener<? super E> l);

	/**
	 * Adds a property change listener.
	 * 
	 * @param pcl the property change listener to add
	 */
	void addPropertyChangeListener (PropertyChangeListener pcl);

	/**
	 * Removes a property change listener.
	 * 
	 * @param pcl the property change listener to remove
	 */
	void removePropertyChangeListener (PropertyChangeListener pcl);
}
