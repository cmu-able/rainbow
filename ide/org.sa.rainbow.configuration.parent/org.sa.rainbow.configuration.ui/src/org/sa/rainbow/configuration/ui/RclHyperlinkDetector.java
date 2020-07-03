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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.ISourceViewerAware;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkHelper;
import org.eclipse.xtext.ui.editor.model.XtextDocumentUtil;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;

public class RclHyperlinkDetector implements IHyperlinkDetector {
	@Inject
	private IHyperlinkHelper helper;
	
	/**
	 * @since 2.19
	 */
	@Inject
	private XtextDocumentUtil xtextDocumentUtil;

	public RclHyperlinkDetector() {
	}
	
	@Override
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, final boolean canShowMultipleHyperlinks) {
		return xtextDocumentUtil.getXtextDocument(textViewer).tryReadOnly(new IUnitOfWork<IHyperlink[],XtextResource>() {
			@Override
			public IHyperlink[] exec(XtextResource resource) throws Exception {
				if (helper instanceof ISourceViewerAware && textViewer instanceof ISourceViewer) {
					((ISourceViewerAware) helper).setSourceViewer((ISourceViewer) textViewer);
				}
				return helper.createHyperlinksByOffset(resource, region.getOffset(), canShowMultipleHyperlinks);
			}
		});
	}

	public void setHelper(IHyperlinkHelper helper) {
		this.helper = helper;
	}

	public IHyperlinkHelper getHelper() {
		return helper;
	}
}
