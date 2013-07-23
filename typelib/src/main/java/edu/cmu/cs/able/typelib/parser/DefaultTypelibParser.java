package edu.cmu.cs.able.typelib.parser;

import edu.cmu.cs.able.parsec.Parsec;

/**
 * Default parser that can parse all typelib constructs.
 */
public class DefaultTypelibParser extends Parsec<TypelibParsingContext> {
	/**
	 * The parser that parses the contents of a structure.
	 */
	private Parsec<StructureParsingContext> m_structure_parsec;
	
	/**
	 * Creates a new parser.
	 */
	private DefaultTypelibParser() {
		/*
		 * Nothing to do.
		 */
	}
	
	/**
	 * Creates a new parser with the default delegate parsers.
	 * @return the parser
	 */
	public static DefaultTypelibParser make() {
		DefaultTypelibParser p = new DefaultTypelibParser();
		p.m_structure_parsec = new Parsec<>();
		
		p.add(new TypelibDelParser(p, p.m_structure_parsec));
		p.m_structure_parsec.add(new StructureDelParser());
		
		return p;
	}
}
