package incubator.polling;

import java.util.List;

/**
 * <p>
 * The interface responsible to obtain specific data. The interface that all
 * objects must implement to retrieve data related with the user interface.
 * </p>
 * 
 * @param <T> The type of data obtained from the data source
 * 
 */
public interface PollingDataSource<T> {
	/**
	 * This method is called when we want to obtain the most up to date data.
	 * 
	 * @return a list with specific type of object with the most up to date
	 * data. When there is no data to retrieve (empty) an empty list is returned
	 */
	List<T> getPollingData();
}
