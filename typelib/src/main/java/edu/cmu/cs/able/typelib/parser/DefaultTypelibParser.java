package edu.cmu.cs.able.typelib.parser;

import edu.cmu.cs.able.parsec.Parsec;

/**
 * Default parser that can parse all typelib constructs.
 */
public class DefaultTypelibParser extends Parsec<TypelibParsingContext> {
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
		
		Parsec<StructureParsingContext> structure_parsec = new Parsec<>();
		structure_parsec.add(new StructureDelParser());
		
		Parsec<EnumerationParsingContext> enum_parsec = new Parsec<>();
		enum_parsec.add(new EnumerationDelParser());
		
		p.add(new TypelibDelParser(p, structure_parsec, enum_parsec));
		
		return p;
	}
}
