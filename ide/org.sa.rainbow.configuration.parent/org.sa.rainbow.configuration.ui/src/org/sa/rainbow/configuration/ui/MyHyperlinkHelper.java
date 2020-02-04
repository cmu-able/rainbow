package org.sa.rainbow.configuration.ui;

import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.HyperlinkHelper;

public class MyHyperlinkHelper extends HyperlinkHelper {
	@Override
	public IHyperlink[] createHyperlinksByOffset(XtextResource resource, int offset, boolean createMultipleHyperlinks) {
		return super.createHyperlinksByOffset(resource, offset, createMultipleHyperlinks);
	}
}
