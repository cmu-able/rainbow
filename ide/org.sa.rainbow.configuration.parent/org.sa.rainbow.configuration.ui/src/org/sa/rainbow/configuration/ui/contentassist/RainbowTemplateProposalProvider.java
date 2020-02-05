package org.sa.rainbow.configuration.ui.contentassist;
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
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ITemplateAcceptor;
import org.eclipse.xtext.ui.editor.templates.ContextTypeIdHelper;
import org.eclipse.xtext.ui.editor.templates.DefaultTemplateProposalProvider;
import org.sa.rainbow.configuration.services.RclGrammarAccess;

import com.google.inject.Inject;

public class RainbowTemplateProposalProvider extends DefaultTemplateProposalProvider {

	private ContextTypeIdHelper helper;

	@Inject
	public RainbowTemplateProposalProvider(TemplateStore templateStore, ContextTypeRegistry registry,
			ContextTypeIdHelper helper) {
		super(templateStore, registry, helper);
		this.helper = helper;
	}
	
	@Inject
	RclGrammarAccess ga;
	
	@Override
	protected void createTemplates(TemplateContext templateContext, ContentAssistContext context,
			ITemplateAcceptor acceptor) {
		super.createTemplates(templateContext, context, acceptor);
		
		if (templateContext.getContextType().getId().equals("org.sa.rainbow.configuration.rcl.PropertyReference")) {
			Template template = new Template("name::type",
					"Old style model reference",
					"modeelRefNameAndType",
					"${modelName}::${modeltype}",
					false);
			TemplateProposal tp = createProposal(template, templateContext, context, null, getRelevance(template));
			
			acceptor.accept(tp);
		}
	}

}
