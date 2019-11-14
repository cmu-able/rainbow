package org.sa.rainbow.configuration.ui.contentassist;

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
