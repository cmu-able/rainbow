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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.xtext.ui.JdtTypesProposalProvider;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractValueConverter;
import org.eclipse.xtext.conversion.impl.QualifiedNameValueConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalFactory;

import com.google.inject.Inject;

public class RainbowJdtTypesProposalProvider extends JdtTypesProposalProvider {

	@Inject
	private QualifiedNameValueConverter qnValueConverter;
	
	@Override
    public void createSubTypeProposals(JvmType superType, ICompletionProposalFactory proposalFactory, ContentAssistContext context, EReference typeReference, Filter filter, ICompletionProposalAcceptor acceptor) {
		createSubTypeProposals(superType, proposalFactory, context, typeReference, filter, getConverter(), acceptor);
    }
	
	/**
     * Overridden to pass a default value converter
     */
    @Override
    public void createTypeProposals(ICompletionProposalFactory proposalFactory, ContentAssistContext context, EReference typeReference, Filter filter, ICompletionProposalAcceptor acceptor) {
        createTypeProposals(proposalFactory, context, typeReference, filter, getConverter(), acceptor);
    }
	
    
    private AbstractValueConverter<String> getConverter() {
        return new AbstractValueConverter<String>() {
            /**
             * Remove ^ character from escaped segments
             */
            @Override
            public String toValue(String string, INode node) throws ValueConverterException {
                return string.replace("^", "");
            }
 
            /**
             * Escape segments colliding with keywords with ^ character
             */
            @Override
            public String toString(String value) throws ValueConverterException {
                // this converter will escape keywords with ^
                return qnValueConverter.toString(value);
            }
        };
    }
}
