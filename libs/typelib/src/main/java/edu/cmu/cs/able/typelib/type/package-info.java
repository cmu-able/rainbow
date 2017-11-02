/**
<p>The <code>type</code> package contains the infrastructure for the type
system in <em>typelib</em>.</p>

<p>While discussing
the type hierarchy, it is important to keep separate the <em>Java</em> type
hierarchy from the <em>typelib</em> type hierarchy. If <em>typelib</em>'s type
<code>X</code> is a sub type of type <code>Y</code>, then the <em>class</em>
used to implement <code>X</code> may be the same <em>class</em> used to
implement <code>Y</code>, they may be different, unrelated classes or one may
be a sub class of the other. When we refer to <em>class</em> we refer to Java
classes, when we refer to <em>type</em> we refer to <em>typelib</em>'s types.
When we refer to <em>instance</em> we refer to a Java object and when we refer
to <em>value</em> we refer to a Java object that is part of a <em>type</em>.
</p>

<p>In <em>typelib</em>, each type is represented by an instance of
{@link edu.cmu.cs.able.typelib.type.DataType} or one of its sub classes.
Types in <em>typelib</em> may form
an arbitrary, non-cyclic graph (multiple inheritance is supported). However, in
every value there exists a single piece of data of every type. Inheritance is,
using <em>C++</em> terminology, <em>virtual</em>.</p>

<p>Although the <code>type</code> package supports defining types and
hierarchies, it does not specify how to build objects of the types: each type
will define its own way of doing so.</p>

<p>Data types are defined in <em>scopes</em> (see <code>scope</code>
package for details on scopes). Scopes that contain data types are sub
classes of {@link edu.cmu.cs.able.typelib.type.DataTypeScope}.</p>

<p>Data type implementation should be thread-safe but data value
implementations do not need to be. Users of data values should ensure only
one thread is manipulating each value at every time.</p>
*/
package edu.cmu.cs.able.typelib.type;
