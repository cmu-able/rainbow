package org.sa.rainbow.gui.arch.elements;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;

public interface IUIReporter {

	public void processReport(ReportType type, String message);

}
