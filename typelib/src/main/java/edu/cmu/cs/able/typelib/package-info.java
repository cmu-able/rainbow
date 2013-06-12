/**
<h1>Introduction</h1>

<p>
The <em>typelib</em> project provides a common set of types and ways to
use them at runtime, as well as ways to encode and decode them. It also
supports parsing.
</p>

<h1>Base types</h1>

<p>
Base types are the most basic types supported by <em>typelib</em>. They are:
</p>

<dl>
	<dt><code>int</code></dt>
	<dd>A 64-bit signed integer which contains values within the range
	[-2^63,2^63-1]</dd>
	<dt><code>ascii</code></dt>
	<dd>A sequence of 0 or more characters which can be encoded using the ASCII
	table.</dd>
	<dt><code>string</code></dt>
	<dd>A sequence of 0 or more characters which can be encoded using
	UTF-8.</dd>
	<dt><code>list</code></dt>
	<dd>A list, possibility empty, of other data types.
</dl>

<p>
Base types can be encoded into bits to be sent into a byte stream. TODO:
describe how data types are encoded.
</p>

<h1>Standard types</h1>

<p>
<em>typelib</em> provides a library of types which can be used and manipulated.
There are primitive and composite standard types. All standard types come in
both nullable and non-nullable variants (the nullable ones also accept
<em>null</em> as a value while the non-nullable do not). The primitive
standard types provided are:
</p>

<dl>
	<dt><code>boolean</code></dt>
	<dd>A boolean data type main contain one of two values <code>true</code>
	or <code>false</code>.</dd>
	<dt><code>int8</code>, <code>int16</code>, <code>int32</code> and
	<code>int64</code></dt>
	<dd>Signed integers which contains values with various ranges
	([-2^8,2^8[, [-2^16,2^16[, ...)</dd>
	<dt><code>ascii</code> and <code>string</code></dt>
	<dd>Equivalent to the base types with the same name.</dd>
	<dt><code>time</code></dt>
	<dd>A time reference as the number of microseconds since the epoch in the
	GMT timezone.</dd>
	<dt><code>period</code></dt>
	<dd>A positive or negative time span (or interval), in microseconds.</dd>
</dl>

<p>The composite standard types provided are:</p>

<dl>
	<dt><code>set(X)</code></dt>
	<dd>A, possibly empty, set of distinct values of type <code>X</code> where
	<code>X</code> may be any type.</dd>
	<dt><code>sequence(X)</code></dt>
	<dd>An ordered, possibly empty, sequence of distinct values of type
	<code>X</code> where <code>X</code> may be any type.</dd>
	<dt><code>bag(X)</code></dt>
	<dd>An unordered, possibly empty, set of values of type <code>X</code> in
	which repeated values are allowed.</dd>
	<dt><code>map(X,Y)</code></dt>
	<dd>A set of pairs in which the first element is of type <code>X</code>
	and the second of type <code>Y</code>. No two pairs may have the same
	first element.</dd>
	<dt><code>rel(X,Y)</code></dt>
	<dd>A set of pairs in which the first element is of type <code>X</code>
	and the second element of type <code>Y</code>. Multiple repeated entries
	are allowed.</dd>
	<dt><code>structure</code></dt>
	<dd>A complex composite data type. Structures are discussed below in their
	own section.</dd>
</dl>

<h1>Structures</h1>

<h1>User-defined types</h1>
*/
package edu.cmu.cs.able.typelib;
