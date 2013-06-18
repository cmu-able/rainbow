/**
<p>The <code>prim</code> package contains the implementation of all primitive
data types available. It also defines a scope that contains all the primitive
data types. This scope, the
{@link edu.cmu.cs.able.typelib.prim.PrimitiveScope}, is usually the top-most
scope in the type hierarchy.</p>

<p>The primitive types provided are:</p>

<dl>
	<dt><code>boolean</code></dt>
	<dd>A boolean data type main contain one of two values <code>true</code>
	or <code>false</code>
	({@link edu.cmu.cs.able.typelib.prim.BooleanType}).</dd>
	<dt><code>int8</code>, <code>int16</code>, <code>int32</code> and
	<code>int64</code></dt>
	<dd>Signed integers which contains values with various ranges
	([-2^8,2^8[, [-2^16,2^16[, ...)
	({@link edu.cmu.cs.able.typelib.prim.Int8Type},
	{@link edu.cmu.cs.able.typelib.prim.Int16Type},
	{@link edu.cmu.cs.able.typelib.prim.Int32Type},
	{@link edu.cmu.cs.able.typelib.prim.Int64Type}).</dd>
	<dt><code>ascii</code> and <code>string</code></dt>
	<dd>Types containing arbitrary-length strings. <code>ascii</code>
	only allows ASCII characters while <code>string</code> supports
	unicode characters
	({@link edu.cmu.cs.able.typelib.prim.AsciiType},
	{@link edu.cmu.cs.able.typelib.prim.StringType}).</dd>
	<dt><code>time</code></dt>
	<dd>A time reference as the number of microseconds since the epoch in the
	GMT timezone ({@link edu.cmu.cs.able.typelib.prim.TimeType}).</dd>
	<dt><code>period</code></dt>
	<dd>A positive or negative time span (or interval), in microseconds
	(@link {@link edu.cmu.cs.able.typelib.prim.PeriodType}).</dd>
	<dt><code>type></code></dt>
	<dd>A data type which refers to another data types, a reference to a data
	type ({@link edu.cmu.cs.able.typelib.prim.TypeType}).</dd>
</dl>
*/
package edu.cmu.cs.able.typelib.prim;
