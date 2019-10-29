// $ANTLR 3.5.1 C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g 2019-10-23 16:03:35

package org.sa.rainbow.configuration.parser.antlr.lexer;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;
import org.sa.rainbow.configuration.parser.antlr.lexer.InternalConfigModelLexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class InternalConfigModelCustomLexer extends Lexer {
	public static final int EOF=-1;
	public static final int FRAGMENT_ALL=4;
	public static final int FRAGMENT_Apostrophe=5;
	public static final int FRAGMENT_DEBUG=6;
	public static final int FRAGMENT_DollarSignLeftCurlyBracket=7;
	public static final int FRAGMENT_ERROR=8;
	public static final int FRAGMENT_EqualsSign=9;
	public static final int FRAGMENT_FATAL=10;
	public static final int FRAGMENT_False=11;
	public static final int FRAGMENT_FullStop=12;
	public static final int FRAGMENT_FullStopAsterisk=13;
	public static final int FRAGMENT_Gauge=14;
	public static final int FRAGMENT_HyphenMinus=15;
	public static final int FRAGMENT_HyphenMinusGreaterThanSign=16;
	public static final int FRAGMENT_INFO=17;
	public static final int FRAGMENT_Import=18;
	public static final int FRAGMENT_LeftCurlyBracket=19;
	public static final int FRAGMENT_OFF=20;
	public static final int FRAGMENT_PlusSign=21;
	public static final int FRAGMENT_Probe=22;
	public static final int FRAGMENT_QuotationMark=23;
	public static final int FRAGMENT_RULE_ANY_OTHER=24;
	public static final int FRAGMENT_RULE_ID=25;
	public static final int FRAGMENT_RULE_INT=26;
	public static final int FRAGMENT_RULE_ML_COMMENT=27;
	public static final int FRAGMENT_RULE_SL_COMMENT=28;
	public static final int FRAGMENT_RULE_WS=29;
	public static final int FRAGMENT_ReverseSolidusApostrophe=30;
	public static final int FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket=31;
	public static final int FRAGMENT_ReverseSolidusQuotationMark=32;
	public static final int FRAGMENT_ReverseSolidusReverseSolidus=33;
	public static final int FRAGMENT_RightCurlyBracket=34;
	public static final int FRAGMENT_TRACE=35;
	public static final int FRAGMENT_Target=36;
	public static final int FRAGMENT_True=37;
	public static final int FRAGMENT_Type=38;
	public static final int FRAGMENT_Var=39;
	public static final int FRAGMENT_WARN=40;
	public static final int RULE_ANY_OTHER=41;
	public static final int RULE_ID=42;
	public static final int RULE_INT=43;
	public static final int RULE_ML_COMMENT=44;
	public static final int RULE_SL_COMMENT=45;
	public static final int RULE_WS=46;
	public static final int SYNTHETIC_ALL_KEYWORDS=47;

		private boolean singleQuotedString = false;
		private boolean doubleQuotedString = false;
		private boolean stringVariable = false;
		
		private boolean keywordNotInString() {
			return !singleQuotedString && !doubleQuotedString || stringVariable;
		}


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public InternalConfigModelCustomLexer() {} 
	public InternalConfigModelCustomLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public InternalConfigModelCustomLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g"; }

	// $ANTLR start "SYNTHETIC_ALL_KEYWORDS"
	public final void mSYNTHETIC_ALL_KEYWORDS() throws RecognitionException {
		try {
			int _type = SYNTHETIC_ALL_KEYWORDS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:33:24: ( ( FRAGMENT_Import )=> FRAGMENT_Import | ( FRAGMENT_Target )=> FRAGMENT_Target | ( FRAGMENT_DEBUG )=> FRAGMENT_DEBUG | ( FRAGMENT_ERROR )=> FRAGMENT_ERROR | ( FRAGMENT_FATAL )=> FRAGMENT_FATAL | ( FRAGMENT_TRACE )=> FRAGMENT_TRACE | ( FRAGMENT_False )=> FRAGMENT_False | ( FRAGMENT_Gauge )=> FRAGMENT_Gauge | ( FRAGMENT_Probe )=> FRAGMENT_Probe | ( FRAGMENT_INFO )=> FRAGMENT_INFO | ( FRAGMENT_WARN )=> FRAGMENT_WARN | ( FRAGMENT_True )=> FRAGMENT_True | ( FRAGMENT_Type )=> FRAGMENT_Type | ( FRAGMENT_ALL )=> FRAGMENT_ALL | ( FRAGMENT_OFF )=> FRAGMENT_OFF | ( FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket )=> FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket | ( FRAGMENT_Var )=> FRAGMENT_Var | ( FRAGMENT_DollarSignLeftCurlyBracket )=> FRAGMENT_DollarSignLeftCurlyBracket | ( FRAGMENT_HyphenMinusGreaterThanSign )=> FRAGMENT_HyphenMinusGreaterThanSign | ( FRAGMENT_FullStopAsterisk )=> FRAGMENT_FullStopAsterisk | ( FRAGMENT_ReverseSolidusQuotationMark )=> FRAGMENT_ReverseSolidusQuotationMark | ( FRAGMENT_ReverseSolidusApostrophe )=> FRAGMENT_ReverseSolidusApostrophe | ( FRAGMENT_ReverseSolidusReverseSolidus )=> FRAGMENT_ReverseSolidusReverseSolidus | ( FRAGMENT_QuotationMark )=> FRAGMENT_QuotationMark | ( FRAGMENT_Apostrophe )=> FRAGMENT_Apostrophe | ( FRAGMENT_PlusSign )=> FRAGMENT_PlusSign | ( FRAGMENT_HyphenMinus )=> FRAGMENT_HyphenMinus | ( FRAGMENT_FullStop )=> FRAGMENT_FullStop | ( FRAGMENT_EqualsSign )=> FRAGMENT_EqualsSign | ( FRAGMENT_LeftCurlyBracket )=> FRAGMENT_LeftCurlyBracket | ( FRAGMENT_RightCurlyBracket )=> FRAGMENT_RightCurlyBracket | ( FRAGMENT_RULE_ID )=> FRAGMENT_RULE_ID | ( FRAGMENT_RULE_INT )=> FRAGMENT_RULE_INT | ( FRAGMENT_RULE_ML_COMMENT )=> FRAGMENT_RULE_ML_COMMENT | ( FRAGMENT_RULE_SL_COMMENT )=> FRAGMENT_RULE_SL_COMMENT | ( FRAGMENT_RULE_WS )=> FRAGMENT_RULE_WS | ( FRAGMENT_RULE_ANY_OTHER )=> FRAGMENT_RULE_ANY_OTHER )
			int alt1=37;
			alt1 = dfa1.predict(input);
			switch (alt1) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:34:2: ( FRAGMENT_Import )=> FRAGMENT_Import
					{
					mFRAGMENT_Import(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Import; }
					}
					break;
				case 2 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:35:2: ( FRAGMENT_Target )=> FRAGMENT_Target
					{
					mFRAGMENT_Target(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Target; }
					}
					break;
				case 3 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:36:2: ( FRAGMENT_DEBUG )=> FRAGMENT_DEBUG
					{
					mFRAGMENT_DEBUG(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.DEBUG; }
					}
					break;
				case 4 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:37:2: ( FRAGMENT_ERROR )=> FRAGMENT_ERROR
					{
					mFRAGMENT_ERROR(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ERROR; }
					}
					break;
				case 5 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:38:2: ( FRAGMENT_FATAL )=> FRAGMENT_FATAL
					{
					mFRAGMENT_FATAL(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.FATAL; }
					}
					break;
				case 6 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:39:2: ( FRAGMENT_TRACE )=> FRAGMENT_TRACE
					{
					mFRAGMENT_TRACE(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.TRACE; }
					}
					break;
				case 7 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:40:2: ( FRAGMENT_False )=> FRAGMENT_False
					{
					mFRAGMENT_False(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.False; }
					}
					break;
				case 8 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:41:2: ( FRAGMENT_Gauge )=> FRAGMENT_Gauge
					{
					mFRAGMENT_Gauge(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Gauge; }
					}
					break;
				case 9 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:42:2: ( FRAGMENT_Probe )=> FRAGMENT_Probe
					{
					mFRAGMENT_Probe(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Probe; }
					}
					break;
				case 10 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:43:2: ( FRAGMENT_INFO )=> FRAGMENT_INFO
					{
					mFRAGMENT_INFO(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.INFO; }
					}
					break;
				case 11 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:44:2: ( FRAGMENT_WARN )=> FRAGMENT_WARN
					{
					mFRAGMENT_WARN(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.WARN; }
					}
					break;
				case 12 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:45:2: ( FRAGMENT_True )=> FRAGMENT_True
					{
					mFRAGMENT_True(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.True; }
					}
					break;
				case 13 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:46:2: ( FRAGMENT_Type )=> FRAGMENT_Type
					{
					mFRAGMENT_Type(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Type; }
					}
					break;
				case 14 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:47:2: ( FRAGMENT_ALL )=> FRAGMENT_ALL
					{
					mFRAGMENT_ALL(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ALL; }
					}
					break;
				case 15 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:48:2: ( FRAGMENT_OFF )=> FRAGMENT_OFF
					{
					mFRAGMENT_OFF(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.OFF; }
					}
					break;
				case 16 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:49:2: ( FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket )=> FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket
					{
					mFRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ReverseSolidusDollarSignLeftCurlyBracket; }
					}
					break;
				case 17 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:50:2: ( FRAGMENT_Var )=> FRAGMENT_Var
					{
					mFRAGMENT_Var(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Var; }
					}
					break;
				case 18 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:51:2: ( FRAGMENT_DollarSignLeftCurlyBracket )=> FRAGMENT_DollarSignLeftCurlyBracket
					{
					mFRAGMENT_DollarSignLeftCurlyBracket(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.DollarSignLeftCurlyBracket; }
					}
					break;
				case 19 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:52:2: ( FRAGMENT_HyphenMinusGreaterThanSign )=> FRAGMENT_HyphenMinusGreaterThanSign
					{
					mFRAGMENT_HyphenMinusGreaterThanSign(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.HyphenMinusGreaterThanSign; }
					}
					break;
				case 20 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:53:2: ( FRAGMENT_FullStopAsterisk )=> FRAGMENT_FullStopAsterisk
					{
					mFRAGMENT_FullStopAsterisk(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.FullStopAsterisk; }
					}
					break;
				case 21 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:54:2: ( FRAGMENT_ReverseSolidusQuotationMark )=> FRAGMENT_ReverseSolidusQuotationMark
					{
					mFRAGMENT_ReverseSolidusQuotationMark(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ReverseSolidusQuotationMark; }
					}
					break;
				case 22 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:55:2: ( FRAGMENT_ReverseSolidusApostrophe )=> FRAGMENT_ReverseSolidusApostrophe
					{
					mFRAGMENT_ReverseSolidusApostrophe(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ReverseSolidusApostrophe; }
					}
					break;
				case 23 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:56:2: ( FRAGMENT_ReverseSolidusReverseSolidus )=> FRAGMENT_ReverseSolidusReverseSolidus
					{
					mFRAGMENT_ReverseSolidusReverseSolidus(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.ReverseSolidusReverseSolidus; }
					}
					break;
				case 24 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:57:2: ( FRAGMENT_QuotationMark )=> FRAGMENT_QuotationMark
					{
					mFRAGMENT_QuotationMark(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.QuotationMark; }
					}
					break;
				case 25 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:58:2: ( FRAGMENT_Apostrophe )=> FRAGMENT_Apostrophe
					{
					mFRAGMENT_Apostrophe(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.Apostrophe; }
					}
					break;
				case 26 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:59:2: ( FRAGMENT_PlusSign )=> FRAGMENT_PlusSign
					{
					mFRAGMENT_PlusSign(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.PlusSign; }
					}
					break;
				case 27 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:60:2: ( FRAGMENT_HyphenMinus )=> FRAGMENT_HyphenMinus
					{
					mFRAGMENT_HyphenMinus(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.HyphenMinus; }
					}
					break;
				case 28 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:61:2: ( FRAGMENT_FullStop )=> FRAGMENT_FullStop
					{
					mFRAGMENT_FullStop(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.FullStop; }
					}
					break;
				case 29 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:62:2: ( FRAGMENT_EqualsSign )=> FRAGMENT_EqualsSign
					{
					mFRAGMENT_EqualsSign(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.EqualsSign; }
					}
					break;
				case 30 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:63:2: ( FRAGMENT_LeftCurlyBracket )=> FRAGMENT_LeftCurlyBracket
					{
					mFRAGMENT_LeftCurlyBracket(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.LeftCurlyBracket; }
					}
					break;
				case 31 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:64:2: ( FRAGMENT_RightCurlyBracket )=> FRAGMENT_RightCurlyBracket
					{
					mFRAGMENT_RightCurlyBracket(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RightCurlyBracket; }
					}
					break;
				case 32 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:65:2: ( FRAGMENT_RULE_ID )=> FRAGMENT_RULE_ID
					{
					mFRAGMENT_RULE_ID(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_ID; }
					}
					break;
				case 33 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:66:2: ( FRAGMENT_RULE_INT )=> FRAGMENT_RULE_INT
					{
					mFRAGMENT_RULE_INT(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_INT; }
					}
					break;
				case 34 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:67:2: ( FRAGMENT_RULE_ML_COMMENT )=> FRAGMENT_RULE_ML_COMMENT
					{
					mFRAGMENT_RULE_ML_COMMENT(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_ML_COMMENT; }
					}
					break;
				case 35 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:68:2: ( FRAGMENT_RULE_SL_COMMENT )=> FRAGMENT_RULE_SL_COMMENT
					{
					mFRAGMENT_RULE_SL_COMMENT(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_SL_COMMENT; }
					}
					break;
				case 36 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:69:2: ( FRAGMENT_RULE_WS )=> FRAGMENT_RULE_WS
					{
					mFRAGMENT_RULE_WS(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_WS; }
					}
					break;
				case 37 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:70:2: ( FRAGMENT_RULE_ANY_OTHER )=> FRAGMENT_RULE_ANY_OTHER
					{
					mFRAGMENT_RULE_ANY_OTHER(); if (state.failed) return;

					if ( state.backtracking==0 ) {_type = InternalConfigModelLexer.RULE_ANY_OTHER; }
					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SYNTHETIC_ALL_KEYWORDS"

	// $ANTLR start "FRAGMENT_Import"
	public final void mFRAGMENT_Import() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:73:26: ({...}? => 'import' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:73:28: {...}? => 'import'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Import", "keywordNotInString()");
			}
			match("import"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Import"

	// $ANTLR start "FRAGMENT_Target"
	public final void mFRAGMENT_Target() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:75:26: ({...}? => 'target' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:75:28: {...}? => 'target'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Target", "keywordNotInString()");
			}
			match("target"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Target"

	// $ANTLR start "FRAGMENT_DEBUG"
	public final void mFRAGMENT_DEBUG() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:77:25: ({...}? => 'DEBUG' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:77:27: {...}? => 'DEBUG'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_DEBUG", "keywordNotInString()");
			}
			match("DEBUG"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_DEBUG"

	// $ANTLR start "FRAGMENT_ERROR"
	public final void mFRAGMENT_ERROR() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:79:25: ({...}? => 'ERROR' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:79:27: {...}? => 'ERROR'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_ERROR", "keywordNotInString()");
			}
			match("ERROR"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ERROR"

	// $ANTLR start "FRAGMENT_FATAL"
	public final void mFRAGMENT_FATAL() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:81:25: ({...}? => 'FATAL' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:81:27: {...}? => 'FATAL'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_FATAL", "keywordNotInString()");
			}
			match("FATAL"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_FATAL"

	// $ANTLR start "FRAGMENT_TRACE"
	public final void mFRAGMENT_TRACE() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:83:25: ({...}? => 'TRACE' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:83:27: {...}? => 'TRACE'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_TRACE", "keywordNotInString()");
			}
			match("TRACE"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_TRACE"

	// $ANTLR start "FRAGMENT_False"
	public final void mFRAGMENT_False() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:85:25: ({...}? => 'false' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:85:27: {...}? => 'false'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_False", "keywordNotInString()");
			}
			match("false"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_False"

	// $ANTLR start "FRAGMENT_Gauge"
	public final void mFRAGMENT_Gauge() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:87:25: ({...}? => 'gauge' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:87:27: {...}? => 'gauge'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Gauge", "keywordNotInString()");
			}
			match("gauge"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Gauge"

	// $ANTLR start "FRAGMENT_Probe"
	public final void mFRAGMENT_Probe() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:89:25: ({...}? => 'probe' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:89:27: {...}? => 'probe'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Probe", "keywordNotInString()");
			}
			match("probe"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Probe"

	// $ANTLR start "FRAGMENT_INFO"
	public final void mFRAGMENT_INFO() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:91:24: ({...}? => 'INFO' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:91:26: {...}? => 'INFO'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_INFO", "keywordNotInString()");
			}
			match("INFO"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_INFO"

	// $ANTLR start "FRAGMENT_WARN"
	public final void mFRAGMENT_WARN() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:93:24: ({...}? => 'WARN' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:93:26: {...}? => 'WARN'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_WARN", "keywordNotInString()");
			}
			match("WARN"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_WARN"

	// $ANTLR start "FRAGMENT_True"
	public final void mFRAGMENT_True() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:95:24: ({...}? => 'true' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:95:26: {...}? => 'true'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_True", "keywordNotInString()");
			}
			match("true"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_True"

	// $ANTLR start "FRAGMENT_Type"
	public final void mFRAGMENT_Type() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:97:24: ({...}? => 'type' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:97:26: {...}? => 'type'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Type", "keywordNotInString()");
			}
			match("type"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Type"

	// $ANTLR start "FRAGMENT_ALL"
	public final void mFRAGMENT_ALL() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:99:23: ({...}? => 'ALL' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:99:25: {...}? => 'ALL'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_ALL", "keywordNotInString()");
			}
			match("ALL"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ALL"

	// $ANTLR start "FRAGMENT_OFF"
	public final void mFRAGMENT_OFF() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:101:23: ({...}? => 'OFF' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:101:24: {...}? => 'OFF'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_OFF", "keywordNotInString()");
			}
			match("OFF"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_OFF"

	// $ANTLR start "FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket"
	public final void mFRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:103:60: ( '\\\\${' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:103:62: '\\\\${'
			{
			match("\\${"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket"

	// $ANTLR start "FRAGMENT_Var"
	public final void mFRAGMENT_Var() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:105:23: ({...}? => 'var' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:105:25: {...}? => 'var'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Var", "keywordNotInString()");
			}
			match("var"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Var"

	// $ANTLR start "FRAGMENT_DollarSignLeftCurlyBracket"
	public final void mFRAGMENT_DollarSignLeftCurlyBracket() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:107:46: ( '${' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:107:48: '${'
			{
			match("${"); if (state.failed) return;

			if ( state.backtracking==0 ) {stringVariable=true;}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_DollarSignLeftCurlyBracket"

	// $ANTLR start "FRAGMENT_HyphenMinusGreaterThanSign"
	public final void mFRAGMENT_HyphenMinusGreaterThanSign() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:109:46: ({...}? => '->' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:109:48: {...}? => '->'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_HyphenMinusGreaterThanSign", "keywordNotInString()");
			}
			match("->"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_HyphenMinusGreaterThanSign"

	// $ANTLR start "FRAGMENT_FullStopAsterisk"
	public final void mFRAGMENT_FullStopAsterisk() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:111:36: ({...}? => '.*' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:111:38: {...}? => '.*'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_FullStopAsterisk", "keywordNotInString()");
			}
			match(".*"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_FullStopAsterisk"

	// $ANTLR start "FRAGMENT_ReverseSolidusQuotationMark"
	public final void mFRAGMENT_ReverseSolidusQuotationMark() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:113:47: ( '\\\\\"' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:113:49: '\\\\\"'
			{
			match("\\\""); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ReverseSolidusQuotationMark"

	// $ANTLR start "FRAGMENT_ReverseSolidusApostrophe"
	public final void mFRAGMENT_ReverseSolidusApostrophe() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:115:44: ( '\\\\\\'' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:115:46: '\\\\\\''
			{
			match("\\'"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ReverseSolidusApostrophe"

	// $ANTLR start "FRAGMENT_ReverseSolidusReverseSolidus"
	public final void mFRAGMENT_ReverseSolidusReverseSolidus() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:117:48: ( '\\\\\\\\' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:117:50: '\\\\\\\\'
			{
			match("\\\\"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_ReverseSolidusReverseSolidus"

	// $ANTLR start "FRAGMENT_QuotationMark"
	public final void mFRAGMENT_QuotationMark() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:119:33: ({...}? => '\"' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:119:35: {...}? => '\"'
			{
			if ( !((!singleQuotedString || stringVariable)) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_QuotationMark", "!singleQuotedString || stringVariable");
			}
			match('\"'); if (state.failed) return;
			if ( state.backtracking==0 ) { if (!singleQuotedString) { doubleQuotedString = !doubleQuotedString; } }
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_QuotationMark"

	// $ANTLR start "FRAGMENT_Apostrophe"
	public final void mFRAGMENT_Apostrophe() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:121:30: ({...}? => '\\'' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:121:32: {...}? => '\\''
			{
			if ( !((!doubleQuotedString || stringVariable)) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_Apostrophe", "!doubleQuotedString || stringVariable");
			}
			match('\''); if (state.failed) return;
			if ( state.backtracking==0 ) { if (!doubleQuotedString) { singleQuotedString = !singleQuotedString; } }
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_Apostrophe"

	// $ANTLR start "FRAGMENT_PlusSign"
	public final void mFRAGMENT_PlusSign() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:123:28: ({...}? => '+' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:123:30: {...}? => '+'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_PlusSign", "keywordNotInString()");
			}
			match('+'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_PlusSign"

	// $ANTLR start "FRAGMENT_HyphenMinus"
	public final void mFRAGMENT_HyphenMinus() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:125:31: ({...}? => '-' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:125:33: {...}? => '-'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_HyphenMinus", "keywordNotInString()");
			}
			match('-'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_HyphenMinus"

	// $ANTLR start "FRAGMENT_FullStop"
	public final void mFRAGMENT_FullStop() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:127:28: ({...}? => '.' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:127:30: {...}? => '.'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_FullStop", "keywordNotInString()");
			}
			match('.'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_FullStop"

	// $ANTLR start "FRAGMENT_EqualsSign"
	public final void mFRAGMENT_EqualsSign() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:129:30: ({...}? => '=' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:129:32: {...}? => '='
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_EqualsSign", "keywordNotInString()");
			}
			match('='); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_EqualsSign"

	// $ANTLR start "FRAGMENT_LeftCurlyBracket"
	public final void mFRAGMENT_LeftCurlyBracket() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:131:36: ({...}? => '{' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:131:38: {...}? => '{'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_LeftCurlyBracket", "keywordNotInString()");
			}
			match('{'); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_LeftCurlyBracket"

	// $ANTLR start "FRAGMENT_RightCurlyBracket"
	public final void mFRAGMENT_RightCurlyBracket() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:133:37: ({...}? => '}' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:133:39: {...}? => '}'
			{
			if ( !((keywordNotInString())) ) {
				if (state.backtracking>0) {state.failed=true; return;}
				throw new FailedPredicateException(input, "FRAGMENT_RightCurlyBracket", "keywordNotInString()");
			}
			match('}'); if (state.failed) return;
			if ( state.backtracking==0 ) {stringVariable=false;}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RightCurlyBracket"

	// $ANTLR start "RULE_ID"
	public final void mRULE_ID() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:138:18: ( FRAGMENT_RULE_ID )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:138:20: FRAGMENT_RULE_ID
			{
			mFRAGMENT_RULE_ID(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_ID"

	// $ANTLR start "FRAGMENT_RULE_ID"
	public final void mFRAGMENT_RULE_ID() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:139:27: ( ( 'a' .. 'z' | 'A' .. 'Z' ) ( ( 'a' .. 'z' | 'A' .. 'Z' ) | '_' | '-' | '0' .. '9' )* )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:139:29: ( 'a' .. 'z' | 'A' .. 'Z' ) ( ( 'a' .. 'z' | 'A' .. 'Z' ) | '_' | '-' | '0' .. '9' )*
			{
			if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:139:49: ( ( 'a' .. 'z' | 'A' .. 'Z' ) | '_' | '-' | '0' .. '9' )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0=='-'||(LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||LA2_0=='_'||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:
					{
					if ( input.LA(1)=='-'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop2;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_ID"

	// $ANTLR start "RULE_INT"
	public final void mRULE_INT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:141:19: ( FRAGMENT_RULE_INT )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:141:21: FRAGMENT_RULE_INT
			{
			mFRAGMENT_RULE_INT(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_INT"

	// $ANTLR start "FRAGMENT_RULE_INT"
	public final void mFRAGMENT_RULE_INT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:142:28: ( ( '0' .. '9' )+ )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:142:30: ( '0' .. '9' )+
			{
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:142:30: ( '0' .. '9' )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:
					{
					if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_INT"

	// $ANTLR start "RULE_ML_COMMENT"
	public final void mRULE_ML_COMMENT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:144:26: ( FRAGMENT_RULE_ML_COMMENT )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:144:28: FRAGMENT_RULE_ML_COMMENT
			{
			mFRAGMENT_RULE_ML_COMMENT(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_ML_COMMENT"

	// $ANTLR start "FRAGMENT_RULE_ML_COMMENT"
	public final void mFRAGMENT_RULE_ML_COMMENT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:145:35: ( '/*' ( options {greedy=false; } : . )* '*/' )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:145:37: '/*' ( options {greedy=false; } : . )* '*/'
			{
			match("/*"); if (state.failed) return;

			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:145:42: ( options {greedy=false; } : . )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='*') ) {
					int LA4_1 = input.LA(2);
					if ( (LA4_1=='/') ) {
						alt4=2;
					}
					else if ( ((LA4_1 >= '\u0000' && LA4_1 <= '.')||(LA4_1 >= '0' && LA4_1 <= '\uFFFF')) ) {
						alt4=1;
					}

				}
				else if ( ((LA4_0 >= '\u0000' && LA4_0 <= ')')||(LA4_0 >= '+' && LA4_0 <= '\uFFFF')) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:145:70: .
					{
					matchAny(); if (state.failed) return;
					}
					break;

				default :
					break loop4;
				}
			}

			match("*/"); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_ML_COMMENT"

	// $ANTLR start "RULE_SL_COMMENT"
	public final void mRULE_SL_COMMENT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:147:26: ( FRAGMENT_RULE_SL_COMMENT )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:147:28: FRAGMENT_RULE_SL_COMMENT
			{
			mFRAGMENT_RULE_SL_COMMENT(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_SL_COMMENT"

	// $ANTLR start "FRAGMENT_RULE_SL_COMMENT"
	public final void mFRAGMENT_RULE_SL_COMMENT() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:35: ( '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )? )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:37: '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )?
			{
			match("//"); if (state.failed) return;

			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:42: (~ ( ( '\\n' | '\\r' ) ) )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( ((LA5_0 >= '\u0000' && LA5_0 <= '\t')||(LA5_0 >= '\u000B' && LA5_0 <= '\f')||(LA5_0 >= '\u000E' && LA5_0 <= '\uFFFF')) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop5;
				}
			}

			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:58: ( ( '\\r' )? '\\n' )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0=='\n'||LA7_0=='\r') ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:59: ( '\\r' )? '\\n'
					{
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:59: ( '\\r' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0=='\r') ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:148:59: '\\r'
							{
							match('\r'); if (state.failed) return;
							}
							break;

					}

					match('\n'); if (state.failed) return;
					}
					break;

			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_SL_COMMENT"

	// $ANTLR start "RULE_WS"
	public final void mRULE_WS() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:150:18: ( FRAGMENT_RULE_WS )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:150:20: FRAGMENT_RULE_WS
			{
			mFRAGMENT_RULE_WS(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_WS"

	// $ANTLR start "FRAGMENT_RULE_WS"
	public final void mFRAGMENT_RULE_WS() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:151:27: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:151:29: ( ' ' | '\\t' | '\\r' | '\\n' )+
			{
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:151:29: ( ' ' | '\\t' | '\\r' | '\\n' )+
			int cnt8=0;
			loop8:
			while (true) {
				int alt8=2;
				int LA8_0 = input.LA(1);
				if ( ((LA8_0 >= '\t' && LA8_0 <= '\n')||LA8_0=='\r'||LA8_0==' ') ) {
					alt8=1;
				}

				switch (alt8) {
				case 1 :
					// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt8 >= 1 ) break loop8;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(8, input);
					throw eee;
				}
				cnt8++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_WS"

	// $ANTLR start "RULE_ANY_OTHER"
	public final void mRULE_ANY_OTHER() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:153:25: ( FRAGMENT_RULE_ANY_OTHER )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:153:27: FRAGMENT_RULE_ANY_OTHER
			{
			mFRAGMENT_RULE_ANY_OTHER(); if (state.failed) return;

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RULE_ANY_OTHER"

	// $ANTLR start "FRAGMENT_RULE_ANY_OTHER"
	public final void mFRAGMENT_RULE_ANY_OTHER() throws RecognitionException {
		try {
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:154:34: ( . )
			// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:154:36: .
			{
			matchAny(); if (state.failed) return;
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FRAGMENT_RULE_ANY_OTHER"

	@Override
	public void mTokens() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:1:8: ( SYNTHETIC_ALL_KEYWORDS )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:1:10: SYNTHETIC_ALL_KEYWORDS
		{
		mSYNTHETIC_ALL_KEYWORDS(); if (state.failed) return;

		}

	}

	// $ANTLR start synpred1_InternalConfigModelCustomLexer
	public final void synpred1_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:34:2: ( FRAGMENT_Import )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:34:3: FRAGMENT_Import
		{
		mFRAGMENT_Import(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred1_InternalConfigModelCustomLexer

	// $ANTLR start synpred2_InternalConfigModelCustomLexer
	public final void synpred2_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:35:2: ( FRAGMENT_Target )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:35:3: FRAGMENT_Target
		{
		mFRAGMENT_Target(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred2_InternalConfigModelCustomLexer

	// $ANTLR start synpred3_InternalConfigModelCustomLexer
	public final void synpred3_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:36:2: ( FRAGMENT_DEBUG )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:36:3: FRAGMENT_DEBUG
		{
		mFRAGMENT_DEBUG(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred3_InternalConfigModelCustomLexer

	// $ANTLR start synpred4_InternalConfigModelCustomLexer
	public final void synpred4_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:37:2: ( FRAGMENT_ERROR )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:37:3: FRAGMENT_ERROR
		{
		mFRAGMENT_ERROR(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred4_InternalConfigModelCustomLexer

	// $ANTLR start synpred5_InternalConfigModelCustomLexer
	public final void synpred5_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:38:2: ( FRAGMENT_FATAL )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:38:3: FRAGMENT_FATAL
		{
		mFRAGMENT_FATAL(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred5_InternalConfigModelCustomLexer

	// $ANTLR start synpred6_InternalConfigModelCustomLexer
	public final void synpred6_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:39:2: ( FRAGMENT_TRACE )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:39:3: FRAGMENT_TRACE
		{
		mFRAGMENT_TRACE(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred6_InternalConfigModelCustomLexer

	// $ANTLR start synpred7_InternalConfigModelCustomLexer
	public final void synpred7_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:40:2: ( FRAGMENT_False )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:40:3: FRAGMENT_False
		{
		mFRAGMENT_False(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred7_InternalConfigModelCustomLexer

	// $ANTLR start synpred8_InternalConfigModelCustomLexer
	public final void synpred8_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:41:2: ( FRAGMENT_Gauge )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:41:3: FRAGMENT_Gauge
		{
		mFRAGMENT_Gauge(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred8_InternalConfigModelCustomLexer

	// $ANTLR start synpred9_InternalConfigModelCustomLexer
	public final void synpred9_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:42:2: ( FRAGMENT_Probe )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:42:3: FRAGMENT_Probe
		{
		mFRAGMENT_Probe(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred9_InternalConfigModelCustomLexer

	// $ANTLR start synpred10_InternalConfigModelCustomLexer
	public final void synpred10_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:43:2: ( FRAGMENT_INFO )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:43:3: FRAGMENT_INFO
		{
		mFRAGMENT_INFO(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred10_InternalConfigModelCustomLexer

	// $ANTLR start synpred11_InternalConfigModelCustomLexer
	public final void synpred11_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:44:2: ( FRAGMENT_WARN )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:44:3: FRAGMENT_WARN
		{
		mFRAGMENT_WARN(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_InternalConfigModelCustomLexer

	// $ANTLR start synpred12_InternalConfigModelCustomLexer
	public final void synpred12_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:45:2: ( FRAGMENT_True )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:45:3: FRAGMENT_True
		{
		mFRAGMENT_True(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_InternalConfigModelCustomLexer

	// $ANTLR start synpred13_InternalConfigModelCustomLexer
	public final void synpred13_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:46:2: ( FRAGMENT_Type )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:46:3: FRAGMENT_Type
		{
		mFRAGMENT_Type(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_InternalConfigModelCustomLexer

	// $ANTLR start synpred14_InternalConfigModelCustomLexer
	public final void synpred14_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:47:2: ( FRAGMENT_ALL )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:47:3: FRAGMENT_ALL
		{
		mFRAGMENT_ALL(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_InternalConfigModelCustomLexer

	// $ANTLR start synpred15_InternalConfigModelCustomLexer
	public final void synpred15_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:48:2: ( FRAGMENT_OFF )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:48:3: FRAGMENT_OFF
		{
		mFRAGMENT_OFF(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred15_InternalConfigModelCustomLexer

	// $ANTLR start synpred16_InternalConfigModelCustomLexer
	public final void synpred16_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:49:2: ( FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:49:3: FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket
		{
		mFRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred16_InternalConfigModelCustomLexer

	// $ANTLR start synpred17_InternalConfigModelCustomLexer
	public final void synpred17_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:50:2: ( FRAGMENT_Var )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:50:3: FRAGMENT_Var
		{
		mFRAGMENT_Var(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred17_InternalConfigModelCustomLexer

	// $ANTLR start synpred18_InternalConfigModelCustomLexer
	public final void synpred18_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:51:2: ( FRAGMENT_DollarSignLeftCurlyBracket )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:51:3: FRAGMENT_DollarSignLeftCurlyBracket
		{
		mFRAGMENT_DollarSignLeftCurlyBracket(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred18_InternalConfigModelCustomLexer

	// $ANTLR start synpred19_InternalConfigModelCustomLexer
	public final void synpred19_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:52:2: ( FRAGMENT_HyphenMinusGreaterThanSign )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:52:3: FRAGMENT_HyphenMinusGreaterThanSign
		{
		mFRAGMENT_HyphenMinusGreaterThanSign(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred19_InternalConfigModelCustomLexer

	// $ANTLR start synpred20_InternalConfigModelCustomLexer
	public final void synpred20_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:53:2: ( FRAGMENT_FullStopAsterisk )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:53:3: FRAGMENT_FullStopAsterisk
		{
		mFRAGMENT_FullStopAsterisk(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred20_InternalConfigModelCustomLexer

	// $ANTLR start synpred21_InternalConfigModelCustomLexer
	public final void synpred21_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:54:2: ( FRAGMENT_ReverseSolidusQuotationMark )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:54:3: FRAGMENT_ReverseSolidusQuotationMark
		{
		mFRAGMENT_ReverseSolidusQuotationMark(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred21_InternalConfigModelCustomLexer

	// $ANTLR start synpred22_InternalConfigModelCustomLexer
	public final void synpred22_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:55:2: ( FRAGMENT_ReverseSolidusApostrophe )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:55:3: FRAGMENT_ReverseSolidusApostrophe
		{
		mFRAGMENT_ReverseSolidusApostrophe(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred22_InternalConfigModelCustomLexer

	// $ANTLR start synpred23_InternalConfigModelCustomLexer
	public final void synpred23_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:56:2: ( FRAGMENT_ReverseSolidusReverseSolidus )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:56:3: FRAGMENT_ReverseSolidusReverseSolidus
		{
		mFRAGMENT_ReverseSolidusReverseSolidus(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred23_InternalConfigModelCustomLexer

	// $ANTLR start synpred24_InternalConfigModelCustomLexer
	public final void synpred24_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:57:2: ( FRAGMENT_QuotationMark )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:57:3: FRAGMENT_QuotationMark
		{
		mFRAGMENT_QuotationMark(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred24_InternalConfigModelCustomLexer

	// $ANTLR start synpred25_InternalConfigModelCustomLexer
	public final void synpred25_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:58:2: ( FRAGMENT_Apostrophe )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:58:3: FRAGMENT_Apostrophe
		{
		mFRAGMENT_Apostrophe(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred25_InternalConfigModelCustomLexer

	// $ANTLR start synpred26_InternalConfigModelCustomLexer
	public final void synpred26_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:59:2: ( FRAGMENT_PlusSign )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:59:3: FRAGMENT_PlusSign
		{
		mFRAGMENT_PlusSign(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred26_InternalConfigModelCustomLexer

	// $ANTLR start synpred27_InternalConfigModelCustomLexer
	public final void synpred27_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:60:2: ( FRAGMENT_HyphenMinus )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:60:3: FRAGMENT_HyphenMinus
		{
		mFRAGMENT_HyphenMinus(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred27_InternalConfigModelCustomLexer

	// $ANTLR start synpred28_InternalConfigModelCustomLexer
	public final void synpred28_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:61:2: ( FRAGMENT_FullStop )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:61:3: FRAGMENT_FullStop
		{
		mFRAGMENT_FullStop(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred28_InternalConfigModelCustomLexer

	// $ANTLR start synpred29_InternalConfigModelCustomLexer
	public final void synpred29_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:62:2: ( FRAGMENT_EqualsSign )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:62:3: FRAGMENT_EqualsSign
		{
		mFRAGMENT_EqualsSign(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred29_InternalConfigModelCustomLexer

	// $ANTLR start synpred30_InternalConfigModelCustomLexer
	public final void synpred30_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:63:2: ( FRAGMENT_LeftCurlyBracket )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:63:3: FRAGMENT_LeftCurlyBracket
		{
		mFRAGMENT_LeftCurlyBracket(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred30_InternalConfigModelCustomLexer

	// $ANTLR start synpred31_InternalConfigModelCustomLexer
	public final void synpred31_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:64:2: ( FRAGMENT_RightCurlyBracket )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:64:3: FRAGMENT_RightCurlyBracket
		{
		mFRAGMENT_RightCurlyBracket(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred31_InternalConfigModelCustomLexer

	// $ANTLR start synpred32_InternalConfigModelCustomLexer
	public final void synpred32_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:65:2: ( FRAGMENT_RULE_ID )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:65:3: FRAGMENT_RULE_ID
		{
		mFRAGMENT_RULE_ID(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred32_InternalConfigModelCustomLexer

	// $ANTLR start synpred33_InternalConfigModelCustomLexer
	public final void synpred33_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:66:2: ( FRAGMENT_RULE_INT )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:66:3: FRAGMENT_RULE_INT
		{
		mFRAGMENT_RULE_INT(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred33_InternalConfigModelCustomLexer

	// $ANTLR start synpred34_InternalConfigModelCustomLexer
	public final void synpred34_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:67:2: ( FRAGMENT_RULE_ML_COMMENT )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:67:3: FRAGMENT_RULE_ML_COMMENT
		{
		mFRAGMENT_RULE_ML_COMMENT(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred34_InternalConfigModelCustomLexer

	// $ANTLR start synpred35_InternalConfigModelCustomLexer
	public final void synpred35_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:68:2: ( FRAGMENT_RULE_SL_COMMENT )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:68:3: FRAGMENT_RULE_SL_COMMENT
		{
		mFRAGMENT_RULE_SL_COMMENT(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred35_InternalConfigModelCustomLexer

	// $ANTLR start synpred36_InternalConfigModelCustomLexer
	public final void synpred36_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:69:2: ( FRAGMENT_RULE_WS )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:69:3: FRAGMENT_RULE_WS
		{
		mFRAGMENT_RULE_WS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred36_InternalConfigModelCustomLexer

	// $ANTLR start synpred37_InternalConfigModelCustomLexer
	public final void synpred37_InternalConfigModelCustomLexer_fragment() throws RecognitionException {
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:70:2: ( FRAGMENT_RULE_ANY_OTHER )
		// C:\\current-workspace\\RainbowV2Git\\org.sa.rainbow.configuration.parent\\org.sa.rainbow.configuration\\src\\org\\sa\\rainbow\\configuration\\parser\\antlr\\lexer\\InternalConfigModelCustomLexer.g:70:3: FRAGMENT_RULE_ANY_OTHER
		{
		mFRAGMENT_RULE_ANY_OTHER(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred37_InternalConfigModelCustomLexer

	public final boolean synpred19_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred19_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred28_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred28_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred24_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred24_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred4_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred20_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred20_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred33_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred33_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred16_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred16_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred37_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred37_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred8_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred25_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred25_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred30_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred30_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred29_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred29_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred5_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred21_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred21_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred17_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred17_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred34_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred34_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred9_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred9_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred31_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred31_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred10_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred10_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred22_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred22_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred26_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred26_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred14_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred14_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred6_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred18_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred18_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred35_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred35_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred32_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred32_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred23_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred23_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred27_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred27_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred36_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred36_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred7_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred11_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred11_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred15_InternalConfigModelCustomLexer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred15_InternalConfigModelCustomLexer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA1 dfa1 = new DFA1(this);
	static final String DFA1_eotS =
		"\16\uffff\1\62\1\uffff\1\62\12\uffff\1\62\47\uffff";
	static final String DFA1_eofS =
		"\103\uffff";
	static final String DFA1_minS =
		"\1\0\1\155\1\141\1\105\1\122\1\101\1\122\2\141\1\162\1\116\1\101\1\114"+
		"\1\106\1\42\1\141\1\173\1\76\1\52\10\0\1\52\1\0\46\uffff";
	static final String DFA1_maxS =
		"\1\uffff\1\155\1\171\1\105\1\122\1\101\1\122\2\141\1\162\1\116\1\101\1"+
		"\114\1\106\1\134\1\141\1\173\1\76\1\52\10\0\1\57\1\0\46\uffff";
	static final String DFA1_acceptS =
		"\35\uffff\1\45\1\1\1\40\1\2\1\14\1\15\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1"+
		"\12\1\13\1\16\1\17\1\20\1\25\1\26\1\27\1\45\1\21\1\22\1\23\1\33\1\24\1"+
		"\34\1\30\1\31\1\32\1\35\1\36\1\37\1\41\1\42\1\43\1\44";
	static final String DFA1_specialS =
		"\1\10\1\13\1\16\1\20\1\24\1\26\1\31\1\32\1\1\1\2\1\5\1\7\1\14\1\15\1\33"+
		"\1\21\1\0\1\25\1\34\1\11\1\22\1\23\1\6\1\12\1\3\1\30\1\4\1\27\1\17\46"+
		"\uffff}>";
	static final String[] DFA1_transitionS = {
			"\11\35\2\34\2\35\1\34\22\35\1\34\1\35\1\23\1\35\1\20\2\35\1\24\3\35\1"+
			"\25\1\35\1\21\1\22\1\33\12\32\3\35\1\26\3\35\1\14\2\31\1\3\1\4\1\5\2"+
			"\31\1\12\5\31\1\15\4\31\1\6\2\31\1\13\3\31\1\35\1\16\4\35\5\31\1\7\1"+
			"\10\1\31\1\1\6\31\1\11\3\31\1\2\1\31\1\17\4\31\1\27\1\35\1\30\uff82\35",
			"\1\36",
			"\1\40\20\uffff\1\41\6\uffff\1\42",
			"\1\43",
			"\1\44",
			"\1\45",
			"\1\46",
			"\1\47",
			"\1\50",
			"\1\51",
			"\1\52",
			"\1\53",
			"\1\54",
			"\1\55",
			"\1\57\1\uffff\1\56\2\uffff\1\60\64\uffff\1\61",
			"\1\63",
			"\1\64",
			"\1\65",
			"\1\67",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\100\4\uffff\1\101",
			"\1\uffff",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			""
	};

	static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
	static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
	static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
	static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
	static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
	static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
	static final short[][] DFA1_transition;

	static {
		int numStates = DFA1_transitionS.length;
		DFA1_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
		}
	}

	protected class DFA1 extends DFA {

		public DFA1(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 1;
			this.eot = DFA1_eot;
			this.eof = DFA1_eof;
			this.min = DFA1_min;
			this.max = DFA1_max;
			this.accept = DFA1_accept;
			this.special = DFA1_special;
			this.transition = DFA1_transition;
		}
		@Override
		public String getDescription() {
			return "33:1: SYNTHETIC_ALL_KEYWORDS : ( ( FRAGMENT_Import )=> FRAGMENT_Import | ( FRAGMENT_Target )=> FRAGMENT_Target | ( FRAGMENT_DEBUG )=> FRAGMENT_DEBUG | ( FRAGMENT_ERROR )=> FRAGMENT_ERROR | ( FRAGMENT_FATAL )=> FRAGMENT_FATAL | ( FRAGMENT_TRACE )=> FRAGMENT_TRACE | ( FRAGMENT_False )=> FRAGMENT_False | ( FRAGMENT_Gauge )=> FRAGMENT_Gauge | ( FRAGMENT_Probe )=> FRAGMENT_Probe | ( FRAGMENT_INFO )=> FRAGMENT_INFO | ( FRAGMENT_WARN )=> FRAGMENT_WARN | ( FRAGMENT_True )=> FRAGMENT_True | ( FRAGMENT_Type )=> FRAGMENT_Type | ( FRAGMENT_ALL )=> FRAGMENT_ALL | ( FRAGMENT_OFF )=> FRAGMENT_OFF | ( FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket )=> FRAGMENT_ReverseSolidusDollarSignLeftCurlyBracket | ( FRAGMENT_Var )=> FRAGMENT_Var | ( FRAGMENT_DollarSignLeftCurlyBracket )=> FRAGMENT_DollarSignLeftCurlyBracket | ( FRAGMENT_HyphenMinusGreaterThanSign )=> FRAGMENT_HyphenMinusGreaterThanSign | ( FRAGMENT_FullStopAsterisk )=> FRAGMENT_FullStopAsterisk | ( FRAGMENT_ReverseSolidusQuotationMark )=> FRAGMENT_ReverseSolidusQuotationMark | ( FRAGMENT_ReverseSolidusApostrophe )=> FRAGMENT_ReverseSolidusApostrophe | ( FRAGMENT_ReverseSolidusReverseSolidus )=> FRAGMENT_ReverseSolidusReverseSolidus | ( FRAGMENT_QuotationMark )=> FRAGMENT_QuotationMark | ( FRAGMENT_Apostrophe )=> FRAGMENT_Apostrophe | ( FRAGMENT_PlusSign )=> FRAGMENT_PlusSign | ( FRAGMENT_HyphenMinus )=> FRAGMENT_HyphenMinus | ( FRAGMENT_FullStop )=> FRAGMENT_FullStop | ( FRAGMENT_EqualsSign )=> FRAGMENT_EqualsSign | ( FRAGMENT_LeftCurlyBracket )=> FRAGMENT_LeftCurlyBracket | ( FRAGMENT_RightCurlyBracket )=> FRAGMENT_RightCurlyBracket | ( FRAGMENT_RULE_ID )=> FRAGMENT_RULE_ID | ( FRAGMENT_RULE_INT )=> FRAGMENT_RULE_INT | ( FRAGMENT_RULE_ML_COMMENT )=> FRAGMENT_RULE_ML_COMMENT | ( FRAGMENT_RULE_SL_COMMENT )=> FRAGMENT_RULE_SL_COMMENT | ( FRAGMENT_RULE_WS )=> FRAGMENT_RULE_WS | ( FRAGMENT_RULE_ANY_OTHER )=> FRAGMENT_RULE_ANY_OTHER );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA1_16 = input.LA(1);
						 
						int index1_16 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_16=='{') && (synpred18_InternalConfigModelCustomLexer())) {s = 52;}
						else s = 50;
						 
						input.seek(index1_16);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA1_8 = input.LA(1);
						 
						int index1_8 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_8=='a') && (((keywordNotInString())&&synpred8_InternalConfigModelCustomLexer()))) {s = 40;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_8);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA1_9 = input.LA(1);
						 
						int index1_9 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_9=='r') && (((keywordNotInString())&&synpred9_InternalConfigModelCustomLexer()))) {s = 41;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_9);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA1_24 = input.LA(1);
						 
						int index1_24 = input.index();
						input.rewind();
						s = -1;
						if ( (((keywordNotInString())&&synpred31_InternalConfigModelCustomLexer())) ) {s = 62;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_24);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA1_26 = input.LA(1);
						 
						int index1_26 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred33_InternalConfigModelCustomLexer()) ) {s = 63;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_26);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA1_10 = input.LA(1);
						 
						int index1_10 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_10=='N') && (((keywordNotInString())&&synpred10_InternalConfigModelCustomLexer()))) {s = 42;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_10);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA1_22 = input.LA(1);
						 
						int index1_22 = input.index();
						input.rewind();
						s = -1;
						if ( (((keywordNotInString())&&synpred29_InternalConfigModelCustomLexer())) ) {s = 60;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_22);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA1_11 = input.LA(1);
						 
						int index1_11 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_11=='A') && (((keywordNotInString())&&synpred11_InternalConfigModelCustomLexer()))) {s = 43;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_11);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA1_0 = input.LA(1);
						 
						int index1_0 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_0=='i') ) {s = 1;}
						else if ( (LA1_0=='t') ) {s = 2;}
						else if ( (LA1_0=='D') ) {s = 3;}
						else if ( (LA1_0=='E') ) {s = 4;}
						else if ( (LA1_0=='F') ) {s = 5;}
						else if ( (LA1_0=='T') ) {s = 6;}
						else if ( (LA1_0=='f') ) {s = 7;}
						else if ( (LA1_0=='g') ) {s = 8;}
						else if ( (LA1_0=='p') ) {s = 9;}
						else if ( (LA1_0=='I') ) {s = 10;}
						else if ( (LA1_0=='W') ) {s = 11;}
						else if ( (LA1_0=='A') ) {s = 12;}
						else if ( (LA1_0=='O') ) {s = 13;}
						else if ( (LA1_0=='\\') ) {s = 14;}
						else if ( (LA1_0=='v') ) {s = 15;}
						else if ( (LA1_0=='$') ) {s = 16;}
						else if ( (LA1_0=='-') ) {s = 17;}
						else if ( (LA1_0=='.') ) {s = 18;}
						else if ( (LA1_0=='\"') ) {s = 19;}
						else if ( (LA1_0=='\'') ) {s = 20;}
						else if ( (LA1_0=='+') ) {s = 21;}
						else if ( (LA1_0=='=') ) {s = 22;}
						else if ( (LA1_0=='{') ) {s = 23;}
						else if ( (LA1_0=='}') ) {s = 24;}
						else if ( ((LA1_0 >= 'B' && LA1_0 <= 'C')||(LA1_0 >= 'G' && LA1_0 <= 'H')||(LA1_0 >= 'J' && LA1_0 <= 'N')||(LA1_0 >= 'P' && LA1_0 <= 'S')||(LA1_0 >= 'U' && LA1_0 <= 'V')||(LA1_0 >= 'X' && LA1_0 <= 'Z')||(LA1_0 >= 'a' && LA1_0 <= 'e')||LA1_0=='h'||(LA1_0 >= 'j' && LA1_0 <= 'o')||(LA1_0 >= 'q' && LA1_0 <= 's')||LA1_0=='u'||(LA1_0 >= 'w' && LA1_0 <= 'z')) ) {s = 25;}
						else if ( ((LA1_0 >= '0' && LA1_0 <= '9')) ) {s = 26;}
						else if ( (LA1_0=='/') ) {s = 27;}
						else if ( ((LA1_0 >= '\t' && LA1_0 <= '\n')||LA1_0=='\r'||LA1_0==' ') ) {s = 28;}
						else if ( ((LA1_0 >= '\u0000' && LA1_0 <= '\b')||(LA1_0 >= '\u000B' && LA1_0 <= '\f')||(LA1_0 >= '\u000E' && LA1_0 <= '\u001F')||LA1_0=='!'||LA1_0=='#'||(LA1_0 >= '%' && LA1_0 <= '&')||(LA1_0 >= '(' && LA1_0 <= '*')||LA1_0==','||(LA1_0 >= ':' && LA1_0 <= '<')||(LA1_0 >= '>' && LA1_0 <= '@')||LA1_0=='['||(LA1_0 >= ']' && LA1_0 <= '`')||LA1_0=='|'||(LA1_0 >= '~' && LA1_0 <= '\uFFFF')) && (synpred37_InternalConfigModelCustomLexer())) {s = 29;}
						 
						input.seek(index1_0);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA1_19 = input.LA(1);
						 
						int index1_19 = input.index();
						input.rewind();
						s = -1;
						if ( ((synpred24_InternalConfigModelCustomLexer()&&(!singleQuotedString || stringVariable))) ) {s = 57;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_19);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA1_23 = input.LA(1);
						 
						int index1_23 = input.index();
						input.rewind();
						s = -1;
						if ( ((synpred30_InternalConfigModelCustomLexer()&&(keywordNotInString()))) ) {s = 61;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_23);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA1_1 = input.LA(1);
						 
						int index1_1 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_1=='m') && (((keywordNotInString())&&synpred1_InternalConfigModelCustomLexer()))) {s = 30;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_1);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA1_12 = input.LA(1);
						 
						int index1_12 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_12=='L') && (((keywordNotInString())&&synpred14_InternalConfigModelCustomLexer()))) {s = 44;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_12);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA1_13 = input.LA(1);
						 
						int index1_13 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_13=='F') && (((keywordNotInString())&&synpred15_InternalConfigModelCustomLexer()))) {s = 45;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_13);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA1_2 = input.LA(1);
						 
						int index1_2 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_2=='a') && (((keywordNotInString())&&synpred2_InternalConfigModelCustomLexer()))) {s = 32;}
						else if ( (LA1_2=='r') && (((keywordNotInString())&&synpred12_InternalConfigModelCustomLexer()))) {s = 33;}
						else if ( (LA1_2=='y') && (((keywordNotInString())&&synpred13_InternalConfigModelCustomLexer()))) {s = 34;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_2);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA1_28 = input.LA(1);
						 
						int index1_28 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred36_InternalConfigModelCustomLexer()) ) {s = 66;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_28);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA1_3 = input.LA(1);
						 
						int index1_3 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_3=='E') && (((keywordNotInString())&&synpred3_InternalConfigModelCustomLexer()))) {s = 35;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_3);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA1_15 = input.LA(1);
						 
						int index1_15 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_15=='a') && (((keywordNotInString())&&synpred17_InternalConfigModelCustomLexer()))) {s = 51;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_15);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA1_20 = input.LA(1);
						 
						int index1_20 = input.index();
						input.rewind();
						s = -1;
						if ( ((synpred25_InternalConfigModelCustomLexer()&&(!doubleQuotedString || stringVariable))) ) {s = 58;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_20);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA1_21 = input.LA(1);
						 
						int index1_21 = input.index();
						input.rewind();
						s = -1;
						if ( (((keywordNotInString())&&synpred26_InternalConfigModelCustomLexer())) ) {s = 59;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_21);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA1_4 = input.LA(1);
						 
						int index1_4 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_4=='R') && (((keywordNotInString())&&synpred4_InternalConfigModelCustomLexer()))) {s = 36;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_4);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA1_17 = input.LA(1);
						 
						int index1_17 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_17=='>') && ((synpred19_InternalConfigModelCustomLexer()&&(keywordNotInString())))) {s = 53;}
						else if ( (((keywordNotInString())&&synpred27_InternalConfigModelCustomLexer())) ) {s = 54;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_17);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA1_5 = input.LA(1);
						 
						int index1_5 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_5=='A') && (((keywordNotInString())&&synpred5_InternalConfigModelCustomLexer()))) {s = 37;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_5);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA1_27 = input.LA(1);
						 
						int index1_27 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_27=='*') && (synpred34_InternalConfigModelCustomLexer())) {s = 64;}
						else if ( (LA1_27=='/') && (synpred35_InternalConfigModelCustomLexer())) {s = 65;}
						else s = 50;
						 
						input.seek(index1_27);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA1_25 = input.LA(1);
						 
						int index1_25 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_25);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA1_6 = input.LA(1);
						 
						int index1_6 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_6=='R') && (((keywordNotInString())&&synpred6_InternalConfigModelCustomLexer()))) {s = 38;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_6);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA1_7 = input.LA(1);
						 
						int index1_7 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_7=='a') && (((keywordNotInString())&&synpred7_InternalConfigModelCustomLexer()))) {s = 39;}
						else if ( (synpred32_InternalConfigModelCustomLexer()) ) {s = 31;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 29;}
						 
						input.seek(index1_7);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA1_14 = input.LA(1);
						 
						int index1_14 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_14=='$') && (synpred16_InternalConfigModelCustomLexer())) {s = 46;}
						else if ( (LA1_14=='\"') && (synpred21_InternalConfigModelCustomLexer())) {s = 47;}
						else if ( (LA1_14=='\'') && (synpred22_InternalConfigModelCustomLexer())) {s = 48;}
						else if ( (LA1_14=='\\') && (synpred23_InternalConfigModelCustomLexer())) {s = 49;}
						else s = 50;
						 
						input.seek(index1_14);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA1_18 = input.LA(1);
						 
						int index1_18 = input.index();
						input.rewind();
						s = -1;
						if ( (LA1_18=='*') && (((keywordNotInString())&&synpred20_InternalConfigModelCustomLexer()))) {s = 55;}
						else if ( (((keywordNotInString())&&synpred28_InternalConfigModelCustomLexer())) ) {s = 56;}
						else if ( (synpred37_InternalConfigModelCustomLexer()) ) {s = 50;}
						 
						input.seek(index1_18);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 1, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
