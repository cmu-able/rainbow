package org.sa.rainbow.configuration.ui.contentassist;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor.Delegate;

public class NonContinuingAcceptor extends Delegate {

	private boolean m_canAcceptMoreProposals;

	public NonContinuingAcceptor(ICompletionProposalAcceptor delegate) {
		super(delegate);
		m_canAcceptMoreProposals = true;
	}

	@Override
	public void accept(ICompletionProposal proposal) {
		if (proposal != null)
			super.accept(proposal);
	}

	@Override
	public boolean canAcceptMoreProposals() {
		return m_canAcceptMoreProposals;
	}

	public void setAcceptMoreProposals(boolean accept) {
		m_canAcceptMoreProposals = accept;
	}

}
