package org.sa.rainbow.gui.arch.model;

import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;


public interface IReportingModel {

	List<ReportDatum> getReports();

	void addReport(String me);

	String REPORT_PROP = "report";

	public static class ReportDatum {
		public long time;
		public String message;
		
		public ReportDatum(String message) {
			time = new Date().getTime();
			this.message = message;
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);

}
