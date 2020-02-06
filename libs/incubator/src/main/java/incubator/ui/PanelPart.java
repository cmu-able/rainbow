package incubator.ui;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;

import javax.swing.JPanel;

/**
 * Part in a {@link MultipartStatusPanel}.
 */
@SuppressWarnings("serial")
public class PanelPart extends JPanel {
	/**
	 * The event dispatcher.
	 */
	protected LocalDispatcher<PanelPartListener> m_dispatcher;
	
	/**
	 * Creates a new part.
	 */
	public PanelPart() {
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * The part dispatcher.
	 * @return the dispatcher
	 */
	public Dispatcher<PanelPartListener> dispatcher() {
		return m_dispatcher;
	}
	
	/**
	 * Dismisses the part.
	 */
	protected void dismiss() {
		m_dispatcher.dispatch(new DispatcherOp<PanelPartListener>() {
			@Override
			public void dispatch(PanelPartListener l) {
				l.part_dismissed();
			}
		});
	}
}
