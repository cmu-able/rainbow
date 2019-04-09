package org.sa.rainbow.gui.arch.model;

import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.gui.arch.controller.IRainbowUIController;
import org.sa.rainbow.gui.arch.controller.RainbowExecutorController;

public class RainbowArchExecutorModel extends RainbowArchModelElement {

	private IAdaptationExecutor<?> m_executor;

	public RainbowArchExecutorModel(IAdaptationExecutor<?> executor) {
		super();
		m_executor = executor;
	}

	@Override
	public String getId() {
		return m_executor.id();
	}

	public IAdaptationExecutor getExecutor() {
		return m_executor;
	}

	@Override
	public RainbowExecutorController getController() {
		return (RainbowExecutorController) super.getController();
	}
}
