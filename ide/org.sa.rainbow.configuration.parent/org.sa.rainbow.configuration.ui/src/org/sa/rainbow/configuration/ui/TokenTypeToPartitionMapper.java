package org.sa.rainbow.configuration.ui;

import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.ui.editor.model.TerminalsTokenTypeToPartitionMapper;

import com.google.inject.Singleton;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Holger Schill
 */
@Singleton
public class TokenTypeToPartitionMapper extends TerminalsTokenTypeToPartitionMapper {

	public final static String RICH_STRING_LITERAL_PARTITION = "__rich_string";
	public static final String[] SUPPORTED_TOKEN_TYPES = new String[] { 
		COMMENT_PARTITION,
		SL_COMMENT_PARTITION, 
		STRING_LITERAL_PARTITION, 
		RICH_STRING_LITERAL_PARTITION,
		IDocument.DEFAULT_CONTENT_TYPE 
	};

	@Override
	protected String calculateId(String tokenName, int tokenType) {
		if (
				"RULE_RICH_TEXT_DQ".equals(tokenName) || 
				"RULE_RICH_TEXT_START_DQ".equals(tokenName) || 
				"RULE_RICH_TEXT_END_DQ".equals(tokenName) || 
				"RULE_RICH_TEXT_INBETWEEN_DQ".equals(tokenName)) {
			return RICH_STRING_LITERAL_PARTITION;
		}
		if ("RULE_ML_COMMENT".equals(tokenName)) {
			return COMMENT_PARTITION;
		}
		return super.calculateId(tokenName, tokenType);
	}

//	@Override
//	protected String getMappedValue(int tokenType) {
//		if(tokenType == XtendDocumentTokenSource.JAVA_DOC_TOKEN_TYPE){
//			return JAVA_DOC_PARTITION;
//		}
//		return super.getMappedValue(tokenType);
//	}

	@Override
	public String[] getSupportedPartitionTypes() {
		return SUPPORTED_TOKEN_TYPES;
	}
	
	@Override
	public boolean isMultiLineComment(String partitionType) {
		return super.isMultiLineComment(partitionType);
	}
}
