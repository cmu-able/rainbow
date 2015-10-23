package incubator.rmi.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.rmi.RmiScanner;

/**
 * Action that pauses a scan.
 */
public class PauseScanAction extends ContextualAction {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new action.
	 */
	public PauseScanAction() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected boolean isValid(ActionContext context) {
		RmiScanner scanner;
		scanner = (RmiScanner) context.get(RmiScannerIFrame.SCANNER_KEY);
		if (scanner == null) {
			return false;
		}
		
		Integer state;
		state = (Integer) context.get(RmiScannerIFrame.SCANNER_STATUS_KEY);
		if (state == null) {
			return false;
		}

		return state.intValue () == RmiScanner.SCANNING;

	}

	@Override
	public void perform(ActionContext context) {
		RmiScanner scanner;
		scanner = (RmiScanner) context.get(RmiScannerIFrame.SCANNER_KEY);
		scanner.pause();
	}
}
