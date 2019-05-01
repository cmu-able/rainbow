package org.sa.rainbow.stitch.gui.manager;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.gui.arch.elements.AdaptationManagerTabbedPane;
import org.sa.rainbow.gui.arch.elements.ReportHistoryPane;
import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;

// Be careful, this is stitch specific
public class StitchAdaptationManagerTabbedPane extends AdaptationManagerTabbedPane {

	private UtilityFunctionPane m_utilityFunctionPanel;
	private UtilityModelPane m_utilityModelPanel;

	public StitchAdaptationManagerTabbedPane() {
		super();
		m_utilityFunctionPanel = new UtilityFunctionPane();
		m_utilityModelPanel = new UtilityModelPane();
		
		addTab("Utilities", m_utilityModelPanel);
		addTab("Utility Functions", m_utilityFunctionPanel);
		addTab("Stitch Scripts", new StitchDetailPane());
	}

	public void initBindings(RainbowArchAdapationManagerModel amModel) {
		super.initBindings(amModel);
		ModelReference managedModel = amModel.getAdaptationManager().getManagedModel();
		IModelInstance<Object> utilityModel = Rainbow.instance().getRainbowMaster().modelsManager()
				.getModelInstance(new ModelReference(managedModel.getModelName(), "UtilityModel"));
		if (utilityModel != null) {
			UtilityPreferenceDescription upd = (UtilityPreferenceDescription) utilityModel.getModelInstance();
			m_utilityFunctionPanel.initBindings(upd);
			m_utilityModelPanel.initBindings(upd);
		}
		else {
			removeTabAt(indexOfComponent(m_utilityFunctionPanel));
			removeTabAt(indexOfComponent(m_utilityModelPanel));
		}

	}
}
