/**
<p>The <code>vtscope</code> implements two sub types of scopes: <em>typed
scopes</em> and <em>valued scopes</em>. The first type of scope is a
scope containing <em>named types</em>. The second type of scope is a scope
containing <em>named values</em>, one value per name in the named type.</p>

<p>To illustrate, a typed scope can contain name <em>foo</em> of type
<code>bool</code> and a name <em>bar</em> of type <code>int32</code>. Note
that this scope only provides a linkage between names and types. It can be
used to define a type of structure or record or to define variable
declarations in a code segment.</p>

<p>Each typed scope can have many <em>valued scopes</em> associated with. Each
valued scope will be associated to the typed scope. The term used is that the
valued scope is <em>typed</em> by the typed scope. The valued scope provides
concrete values to the types defined in the typed scope. If the typed scope
represents a structure, the valued scope represents an <em>instance</em> of
the structure.</p>
*/
package edu.cmu.cs.able.typelib.vtscope;
