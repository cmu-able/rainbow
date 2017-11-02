/**
<p>The <code>parser</code> package provides support for parsing TDL (type
definition language). Three different, but related, types of text may be
parsed by this package:</p>
<ul>
	<li>Data type names: done by the
	{@link edu.cmu.cs.able.typelib.parser.DataTypeNameParser} class, this
	allows parsing of a string as a data type name and allows finding and
	creating the data type in a scope. Creating the data type automatically
	is only possible for some data types.</li>
	<li>General TDL type definitions: done by the
	{@link edu.cmu.cs.able.typelib.parser.TypelibDelParser} this is the main
	TDL parser. This parser will parse TDL statements and depends on a
	structure parser to parse structures.</li>
	<li>Structure TDL definitions: done by the
	{@link edu.cmu.cs.able.typelib.parser.StructureDelParser} this is usually
	used coupled to a general TDL parser. It is provided separately to allow
	users of the package to add additional delegate parsers to structure
	definitions.
</ul>
 */
package edu.cmu.cs.able.typelib.parser;