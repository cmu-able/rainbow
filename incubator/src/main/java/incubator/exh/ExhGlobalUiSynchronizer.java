package incubator.exh;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Set;

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
		Ensure.not_null(ui, "ui == null");
		GlobalCollector.instance().collectors().addObservableSetListener(
				new ObservableSetListener<ThrowableCollector>() {
			@Override
			public void setCleared() {
				Ensure.unreachable();
			}
			
			@Override
			public void elementRemoved(final ThrowableCollector e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						ui.remove_collector(e);
					}
				});
			}
			
			@Override
			public void elementAdded(final ThrowableCollector e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						ui.add_collector(e);
					}
				});
			}
		});
		
		final Set<ThrowableCollector> initial = new HashSet<>(
				GlobalCollector.instance().collectors());
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (ThrowableCollector tc : initial) {
					ui.add_collector(tc);
				}
			}
		});
	}
}
