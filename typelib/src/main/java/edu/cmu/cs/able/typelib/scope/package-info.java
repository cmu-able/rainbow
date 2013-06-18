/**
<p>The <code>scope</code> package provides support for the concept of
<em>scopes</em>, a fundamental concept in <em>typelib</em>.</p>

<p>A scope is named collection of named objects, meaning each object has a
name and the scope itself also has a name. All objects in a scope must have
a unique name. Scopes can themselves be placed into other scopes building
a hierarchical tree. The most elementary use of scopes is to build name spaces
(or packages) of types. Scopes are implemented in the 
{@link edu.cmu.cs.able.typelib.scope.Scope} class
and scoped objects in the {@link edu.cmu.cs.able.typelib.scope.ScopedObject}
class.</p>

<p>Scopes can also be composed into more complex structures by <em>linking</em>
them. If scope A links to scope B then a search for an object in A will also
search for the object B. An obvious usage of linking is to allow importing
a name space into another.</p>

<p>Scopes and linking can also be used to define complex data types such as
structures: a structure defines a scope with named objects which represents its
fields. In inheritance scenarios, structures can link to their parents so that
a search for a field in a structure will find a parent's field.</p>

<p>More information on linking scopes and when searching is considered
ambiguous can be found in {@link edu.cmu.cs.able.typelib.scope.Scope}.</p>

<p>Scopes are thread-safe although the objects placed in the scopes may
not be.</p>
*/
package edu.cmu.cs.able.typelib.scope;
