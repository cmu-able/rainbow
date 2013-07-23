package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.parsec.BlockHeaderParseException;
import edu.cmu.cs.able.parsec.BlockTextParseException;
import edu.cmu.cs.able.parsec.DelegateParser;
import edu.cmu.cs.able.parsec.LCCoord;
import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.Parsec;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.CyclicScopeLinkageException;
import edu.cmu.cs.able.typelib.struct.InvalidTypeDefinitionException;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Delegation parser that parses typelib code.
 */
public class TypelibDelParser implements DelegateParser<TypelibParsingContext> {
	/**
	 * The parser used to parse content of structures.
	 */
	private Parsec<StructureParsingContext> m_structure_parser;
	
	/**
	 * The parser used to parse content of namespaces.
	 */
	private Parsec<TypelibParsingContext> m_namespace_parser;
	
	/**
	 * Creates a new parser.
	 * @param namespace_parser the parser used to parse namespaces; this will
	 * generally be the same parser this delegate is added to
	 * @param structure_parser the parser used to parse content of
	 * structures; this parser will generally include
	 * {@link StructureDelParser} as a delegate
	 */
	public TypelibDelParser(Parsec<TypelibParsingContext> namespace_parser,
			Parsec<StructureParsingContext> structure_parser) {
		Ensure.not_null(structure_parser);
		Ensure.not_null(namespace_parser);
		m_structure_parser = structure_parser;
		m_namespace_parser = namespace_parser;
	}

	@Override
	public void parse_statement(String statement, TypelibParsingContext ctx)
			throws LocalizedParseException {
		throw new LocalizedParseException("Unexpected text.",
				new LCCoord(1, 1));
	}

	@Override
	public void parse_block(String block_header, String block_text,
			TypelibParsingContext ctx) throws BlockHeaderParseException,
			BlockTextParseException {
		Ensure.not_null(block_header);
		Ensure.not_null(block_text);
		Ensure.not_null(ctx);
		
		TypelibParser tp = new TypelibParser(new StringReader(block_header));
		TypelibBlockDeclaration tbdel = null;
		try {
			tbdel = tp.TypelibBlock();
		} catch (ParseException e) {
			throw new BlockHeaderParseException(new LocalizedParseException(
					e.getMessage(), new LCCoord(e.currentToken.beginLine,
							e.currentToken.beginColumn)));
		}
		
		StructureDeclaration sdel = tbdel.structure_declaration();
		String nsdel = tbdel.namespace_declaration();
		
		if (sdel != null) {
			try {
				if (ctx.scope().find(sdel.name()) != null) {
					throw new BlockHeaderParseException(
							new LocalizedParseException(
							"Duplicate data type name: '" + sdel.name()
							+ "'.", new LCCoord(1, 1)));
				}
			} catch (AmbiguousNameException e) {
				/*
				 * This is OK. If the structure's name is ambiguous it is
				 * because it is not defined in the scope and, therefore, we
				 * can set it.
				 */
			}
			
			StructureParsingContext spc = new StructureParsingContext(ctx,
					sdel);
			try {
				m_structure_parser.parse(new ParsecFileReader().read_memory(
						block_text), spc);
			} catch (LocalizedParseException e) {
				throw new BlockTextParseException(e);
			}
			
			DataTypeNameParser dtnp = new DataTypeNameParser();
			Set<StructureDataType> parents = new HashSet<>();
			for (String s : sdel.parents()) {
				DataType p_dt;
				try {
					p_dt = dtnp.parse(s, ctx.primitive_scope(), ctx.scope());
				} catch (ParseException e) {
					throw new BlockHeaderParseException(
							new LocalizedParseException("Failed to parse type '"
							+ s + "': " + e.getMessage(), new LCCoord(1, 1)));
				}
				
				if (p_dt == null) {
					throw new BlockHeaderParseException(
							new LocalizedParseException("Failed to find type '"
							+ s + "'.", new LCCoord(1, 1)));
				}
				
				if (!(p_dt instanceof StructureDataType)) {
					throw new BlockHeaderParseException(
							new LocalizedParseException("Type '" + s + "' is "
									+ "not a structure.", new LCCoord(1, 1)));
				}
				
				parents.add((StructureDataType) p_dt);
			}
			
			StructureDataType new_structure;
			try {
				if (parents.size() == 0) {
					new_structure = new StructureDataType(sdel.name(),
							sdel.is_abstract(), sdel.fields(),
							ctx.primitive_scope().any());
				} else {
					new_structure = new StructureDataType(sdel.name(),
							sdel.is_abstract(), sdel.fields(), parents);
				}
			} catch (InvalidTypeDefinitionException e) {
				throw new BlockHeaderParseException(
						new LocalizedParseException(e.getMessage(),
						new LCCoord(1, 1)));
			}
			
			ctx.scope().add(new_structure);
		}
		
		if (nsdel != null) {
			DataTypeScope sub_scope;
			
			try {
				sub_scope = (DataTypeScope) ctx.scope().find_scope(
					nsdel);
			} catch (AmbiguousNameException e) {
				/*
				 * If the name is ambiguous, then the subscope does not exist.
				 */
				sub_scope = null;
			}
			
			if (sub_scope == null) {
				sub_scope = new DataTypeScope(nsdel);
				ctx.scope().add(sub_scope);
				
				try {
					sub_scope.link(ctx.scope());
				} catch (CyclicScopeLinkageException e) {
					throw new BlockTextParseException(
							new LocalizedParseException("Cyclic namespace "
									+ "graph detected.", new LCCoord(1, 1),
									e));
				}
			}
			
			TypelibParsingContext sub_ctx = new TypelibParsingContext(
					ctx.primitive_scope(), sub_scope);
			try {
				m_namespace_parser.parse(new ParsecFileReader().read_memory(
						block_text), sub_ctx);
			} catch (LocalizedParseException e) {
				throw new BlockTextParseException(e);
			}
		}
	}
}
