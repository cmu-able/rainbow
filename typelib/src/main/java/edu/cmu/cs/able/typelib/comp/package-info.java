/**
<p>The <code>comp</code> package provides general support for composite data
types and some of the main composite data types.</p>

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
