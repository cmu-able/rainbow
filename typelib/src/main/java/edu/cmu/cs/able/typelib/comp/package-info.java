/**
<p>The <code>comp</code> package provides general support for composite data
types and some of the main composite data types.</p>

<p>The composite standard types provided are:</p>

<dl>
	<dt><code>set&lt;X&gt;</code></dt>
	<dd>A, possibly empty, set of distinct values of type <code>X</code> where
	<code>X</code> may be any type
	({@link edu.cmu.cs.able.typelib.comp.SetDataType}).</dd>
	<dt><code>list&lt;X&gt;</code></dt>
	<dd>An ordered, possibly empty, sequence of distinct values of type
	<code>X</code> where <code>X</code> may be any type
	({@link edu.cmu.cs.able.typelib.comp.ListDataType}).</dd>
	<dt><code>bag&lt;X&gt;</code></dt>
	<dd>An unordered, possibly empty, set of values of type <code>X</code> in
	which repeated values are allowed
	({@link edu.cmu.cs.able.typelib.comp.BagDataType}).</dd>
	<dt><code>tuple&lt;X,Y,...&gt;</code></dt>
	<dd>A tuple which contains one element of each data type provides in the
	defintiion ({@link edu.cmu.cs.able.typelib.comp.TupleDataType}).</dd>
	<dt><code>map&lt;X,Y&gt;</code></dt>
	<dd>A set of pairs in which the first element is of type <code>X</code>
	and the second of type <code>Y</code>. No two pairs may have the same
	first element ({@link edu.cmu.cs.able.typelib.comp.MapDataType}).</dd>
	<dt><code>structure</code></dt>
	<dd>A complex composite data type. Structures are discussed below in their
	own section
	({@link edu.cmu.cs.able.typelib.struct.StructureDataType}).</dd>
</dl>

<p>Support for optional data types is made using a special composite data
type, the {@link edu.cmu.cs.able.typelib.comp.OptionalDataType}. An optional
data type encapsulates another data type allowing its value to be defined
or not. So, technically, the optional data type is a type itself and it is
a composite data type.</p>

<p>Composite data types are generally uniquely defined by their type and their
inner types (the types they depend on). For example, a data type which is a
set of values of type <code>A</code> is uniquely defined by the type
<code>A</code>. Because of this, composite data types can be created
dynamically and classes implementing composite data types contain a static
method to find or create if necessary the composite data type based on its
inner types.</p> 

<p>This also means there are conventions for the scope in which the
composite data types are created. In general composite data types are created
in the same scope of their inner types. If the inner types are defined in
multiple scopes, the composite data types will be created in the inner-most
parent common to all inner types.</p>
*/
package edu.cmu.cs.able.typelib.comp;
