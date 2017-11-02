/**
<h1>Introduction</h1>

<p>The <em>typelib</em> project provides data types and ways to use them at
runtime, encode and decode of data types and parsing data types using a
language known as TDL (type definition language).</p>

<p>The general contract for all data types and main support is done in the
<code>type</code> package.</p>

<p>Primitive data types such as <code>boolean</code>, <code>int32</code>
and <code>string</code> are defined in the <code>prim</code> package.</p>

<p><Composite data types which support complex data types such as sets and
lists as well as support for optional data types is provided in the
<code>comp</code> package.</p>

<p>Structures are composite, complex data types, whose support is provided
in the <code>struct</code> package.</p>

<p>When working with data types, it is common to need to work with scopes
in which data types are defined which relate to scopes in which values are
defined. For example, when declaring a variable of type <code>int32</code>,
we want to create a scope in which there is a type associated with the name
of the variable and, at run time, we want to associate a value with that name.
Value/type scopes are supported by the <code>vtscope</code> package.</p>

<p>Encoding of data types is provided, in a general, abstract form, by the
<code>enc</code> package. The <code>txtenc</code> package provides support
for encoding data types into text with abstract delegates which can encode
and decode specific data types. The <code>txtenc.typelib</code> package
provides delegates for all data types provided by <code>typelib</code>.</p>

<p>The <code>alg</code> package provides some algorithms and utilities
that work on data types.</p>
*/
package edu.cmu.cs.able.typelib;
