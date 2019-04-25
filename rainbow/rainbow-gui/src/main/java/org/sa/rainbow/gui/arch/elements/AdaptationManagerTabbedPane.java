package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;

// Be careful, this is stitch specific
public class AdaptationManagerTabbedPane extends JTabbedPane {

	private UtilityFunctionPane m_utilityFunctionPanel;

	public AdaptationManagerTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		m_utilityFunctionPanel = new UtilityFunctionPane();

//		addTab("Utilities");
		addTab("Utility Functions", m_utilityFunctionPanel);
		addTab("Stitch Scripts", new StitchDetailPane());

//		File stitchPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
//				Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
//		if (stitchPath != null) {
//			FilenameFilter ff = new FilenameFilter() { // find only ".s" files
//				@Override
//				public boolean accept(File dir, String name) {
//					return name.endsWith(".s");
//				}
//			};
//			for (File f : stitchPath.listFiles(ff)) {
//				RSyntaxTextArea textArea = new RSyntaxTextArea();
//				textArea.setCodeFoldingEnabled(true);
//				RTextScrollPane sp = new RTextScrollPane(textArea);
//				addTab(f.getName(), null, sp, null);
//				textArea.setSyntaxEditingStyle("text/stitch");
//				textArea.setEditable(false);
//				
//				try {
//					String contents = new String(Files.readAllBytes(f.toPath()));
//					textArea.setText(contents);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//		}
	}

	public void initBindings(RainbowArchAdapationManagerModel amModel) {
		ModelReference managedModel = amModel.getAdaptationManager().getManagedModel();
		IModelInstance<Object> utilityModel = Rainbow.instance().getRainbowMaster().modelsManager()
				.getModelInstance(new ModelReference(managedModel.getModelName(), "UtilityModel"));
		if (utilityModel != null) {
			UtilityPreferenceDescription upd = (UtilityPreferenceDescription) utilityModel.getModelInstance();
			m_utilityFunctionPanel.initBindings(upd);
		}
		else {
			removeTabAt(indexOfComponent(m_utilityFunctionPanel));
		}

	}
}
