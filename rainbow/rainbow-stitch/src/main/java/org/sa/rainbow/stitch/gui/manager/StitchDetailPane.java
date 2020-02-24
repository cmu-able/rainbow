package org.sa.rainbow.stitch.gui.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.util.Util;

public class StitchDetailPane extends JPanel {
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();

	
	private Map<String,String> m_stitchData = new HashMap<>();
	private RSyntaxTextArea m_textArea;
	
	static {
		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping("text/stitch", StitchTokenMaker.class.getCanonicalName());
	}
	/**
	 * Create the panel.
	 */
	public StitchDetailPane() {
		
		File stitchPath = Util.getRelativeToPath(m_rainbowEnvironment.getTargetPath(),
				m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
		if (stitchPath != null) {
			FilenameFilter ff = new FilenameFilter() { // find only ".s" files
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".s");
				}
			};
			for (File f : stitchPath.listFiles(ff)) {
				try {
					String contents = new String(Files.readAllBytes(f.toPath()));
					m_stitchData.put(f.getName(), contents);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		setLayout(new BorderLayout(0, 0));
		
		final JList list = new JList(new ArrayList<String>(m_stitchData.keySet()).toArray(new String[0]));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(list, BorderLayout.WEST);
		list.setPreferredSize(new Dimension(200,300));
		list.addListSelectionListener(e->{
			if (!e.getValueIsAdjusting()) {
				String key = (String )list.getSelectedValue();
				m_textArea.setText(m_stitchData.get(key));
				m_textArea.setCaretPosition(0);
			}
		});
		
		m_textArea = new RSyntaxTextArea();
		RTextScrollPane sp = new RTextScrollPane(m_textArea);
		m_textArea.setCodeFoldingEnabled(true);
		m_textArea.setSyntaxEditingStyle("text/stitch");
		m_textArea.setEditable(false);
		add(sp, BorderLayout.CENTER);

		
	
	}

}
