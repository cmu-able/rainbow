package org.sa.rainbow.configuration.ui;

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
		System.out.println(helper);
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
