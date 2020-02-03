package org.sa.rainbow.configuration.ui;

import org.eclipse.swt.SWT;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

public class RclHighlightingConfiguration extends DefaultHighlightingConfiguration {
	public static final String SOFT_KEYWORD_ID = "SoftKeyword";
	public static final String PROPERTY_REFERENCE_ID = "PropertyReference";

	@Override
	public void configure(IHighlightingConfigurationAcceptor acceptor) {
		super.configure(acceptor);
		acceptor.acceptDefaultHighlighting(PROPERTY_REFERENCE_ID, "Property Reference", propertyReferenceStyle());
		acceptor.acceptDefaultHighlighting(SOFT_KEYWORD_ID, "Soft Keyword", softKeywordStyle());
	}

	private TextStyle propertyReferenceStyle() {
		TextStyle s = stringTextStyle();
		s.setStyle(SWT.ITALIC);
		return s;
	}
	
	private TextStyle softKeywordStyle() {
		TextStyle s = keywordTextStyle();
		s.setStyle(SWT.NORMAL);
		return s;
	}
}
