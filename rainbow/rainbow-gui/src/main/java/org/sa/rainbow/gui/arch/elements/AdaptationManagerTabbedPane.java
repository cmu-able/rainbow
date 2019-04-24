package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;

// Be careful, this is stitch specific
public class AdaptationManagerTabbedPane extends JTabbedPane {

	public AdaptationManagerTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		UtilityModelPane ump = new UtilityModelPane();
		UtilityPreferenceDescription upd = (UtilityPreferenceDescription) Rainbow.instance().getRainbowMaster().modelsManager().getModelInstance(new ModelReference("SwimSys", "UtilityModel")).getModelInstance();
		addTab("Utilities", ump);
		addTab("Stitch Scripts", new StitchDetailPane());
		ump.initBindings(upd);
		
		
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
}
