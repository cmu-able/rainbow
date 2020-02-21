package org.sa.rainbow.configuration.ui;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider;

public class AutoEditStrategyProvider extends DefaultAutoEditStrategyProvider {
	


	@Override
	protected void configure(IEditStrategyAcceptor acceptor) {
		super.configure(acceptor);
		configurePropertyReference(acceptor);
	}

	private void configurePropertyReference(IEditStrategyAcceptor acceptor) {
		acceptor.accept(singleLineTerminals.newInstance("\u00AB\u00AB", "\u00BB\u00BB"), IDocument.DEFAULT_CONTENT_TYPE);
		acceptor.accept(partitionDeletion.newInstance("\\u00AB\\u00AB", "u00BB\u00BB"), IDocument.DEFAULT_CONTENT_TYPE);
	}
	
	@Override
	protected void configureStringLiteral(IEditStrategyAcceptor acceptor) {
//		super.configureStringLiteral(acceptor);
		acceptor.accept(partitionInsert.newInstance("\"","\""), TokenTypeToPartitionMapper.RICH_STRING_LITERAL_PARTITION);
		acceptor.accept(partitionInsert.newInstance("\u00AB","\u00BB"), TokenTypeToPartitionMapper.RICH_STRING_LITERAL_PARTITION);
		acceptor.accept(partitionDeletion.newInstance("\u00AB","\u00BB"), TokenTypeToPartitionMapper.RICH_STRING_LITERAL_PARTITION);
		acceptor.accept(singleLineTerminals.newInstance("\u00AB","\u00BB"), IDocument.DEFAULT_CONTENT_TYPE);
	}

	
}
