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
