package org.sa.rainbow.gui.arch.model;

import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.gui.arch.controller.RainbowAdaptationManagerController;

public class RainbowArchAdapationManagerModel extends RainbowArchModelElement implements IReportingModel {

	private IAdaptationManager<?> m_manager;
	
	private List<ReportDatum> m_reports = new LinkedList<>();

	public RainbowArchAdapationManagerModel(IAdaptationManager<?> manager) {
		super();
		m_manager = manager;
	}

	@Override
	public String getId() {
		return m_manager.id();
	}
	
	
	public IAdaptationManager getAdaptationManager() {
		return m_manager;
	}
	
	@Override
	public RainbowAdaptationManagerController getController() {
		return (RainbowAdaptationManagerController) super.getController();
	}

	@Override
	public void addReport(String me) {
		ReportDatum rd = new ReportDatum(me);
		m_reports.add(0,rd);
		pcs.firePropertyChange(REPORT_PROP, null, rd);
	}
	
	@Override
	public List<ReportDatum> getReports() {
		return m_reports;
	}

	@Override
	public AbstractRainbowRunnable getRunnable() {
		return (AbstractRainbowRunnable )getAdaptationManager();
	}

}
