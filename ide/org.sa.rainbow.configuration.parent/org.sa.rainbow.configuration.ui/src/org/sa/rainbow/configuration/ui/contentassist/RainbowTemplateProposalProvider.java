package org.sa.rainbow.configuration.ui.contentassist;

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
