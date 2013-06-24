package incubator.rmi.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.rmi.RmiScanner;

/**
 * Action that starts a scan.
 */
public class StartScanAction extends ContextualAction {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new action.
	 */
	public StartScanAction() {
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
		
		if (state.intValue() == RmiScanner.STOPPED ||
				state.intValue() == RmiScanner.FINISHED) {
			return true;
		}
		
		return false;
	}

	@Override
	public void perform(ActionContext context) {
		RmiScanner scanner;
		scanner = (RmiScanner) context.get(RmiScannerIFrame.SCANNER_KEY);
		scanner.start();
	}
}
