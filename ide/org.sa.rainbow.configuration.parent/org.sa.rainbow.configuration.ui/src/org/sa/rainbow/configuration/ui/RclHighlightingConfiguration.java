package org.sa.rainbow.configuration.ui;
/*
Copyright 2020 Carnegie Mellon University

Permission is hereby granted, free of charge, to any person obtaining a copy of this 
software and associated documentation files (the "Software"), to deal in the Software 
without restriction, including without limitation the rights to use, copy, modify, merge,
 publish, distribute, sublicense, and/or sell copies of the Software, and to permit 
 persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.
 */
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
