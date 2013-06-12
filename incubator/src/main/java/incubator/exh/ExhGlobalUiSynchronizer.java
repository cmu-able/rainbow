package incubator.exh;

import incubator.obscol.ObservableSetListener;
import incubator.pval.Ensure;

/**
 * Synchronizer that shows all global collectors in the user interface.
 */
public class ExhGlobalUiSynchronizer {
	/**
	 * Creates a new synchronizer.
	 * @param ui the user interface
	 */
	public ExhGlobalUiSynchronizer(final ExhUi ui) {
		Ensure.notNull(ui);
		GlobalCollector.instance().collectors().addObservableSetListener(
				new ObservableSetListener<ThrowableCollector>() {
			@Override
			public void setCleared() {
				Ensure.isTrue(false);
			}
			
			@Override
			public void elementRemoved(ThrowableCollector e) {
				ui.remove_collector(e);
			}
			
			@Override
			public void elementAdded(ThrowableCollector e) {
				ui.add_collector(e);
			}
		});
	}
}
