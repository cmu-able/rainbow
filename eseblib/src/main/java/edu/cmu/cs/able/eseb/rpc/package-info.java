/**
<h1>Overview</h1>

<p>The <code>rpc</code> package allows RPC invocation over eseb. There are two
methods to perform RPCs, one using meta data and another one using Java
interfaces. Since the latter is based on the former, we'll describe meta
data RPCs first although the most common use is probably the latter.</p>

<h1>Main concepts</h1>

<p>The main concept in the <code>rpc</code> package is an <em>operation</em>.
An <em>operation</em> has a signature: a set of typed, named parameters which
define its input and output. Differently from Java methods, operations can
have multiple inputs and multiple outputs. (Note: if using the Java RPC
mechanism, the multiple outputs are not possible.)</p>

<p>Operations are grouped in <em>groups</em>. A <em>group</em> represents a
set of related operations.</p>

<p>Operations are invoked on <em>objects</em>. Objects are identified through
an ID. Servers and clients must agree on the object IDs they will use. If
which objects exist at runtime cannot be agreed in advance, another object,
which will work as a registry, whose ID is known to both server and clients,
can be used to keep track of other object IDs.</p>

<p>Each object in the server is published with a specific ID and operation
group identifying which operations can be executed in the given object.</p>

<p>Servers and clients rely on the <code>participant</code> package to
be able to identify each other and direct their messages.</p>

<h1>Meta data RPCs</h1>

<p>Meta data RPCs are based on meta data that describes the operations that
are available.</p>

<p>To publish a service, a
{@link edu.cmu.cs.able.eseb.rpc.ServiceOperationExecuter} should be created
that is able to execute all operations of an object to make available remotely.
For each object, a
{@link edu.cmu.cs.able.eseb.rpc.ServiceObjectRegistration} should be used.
This registration object is used to publish the object and make it available
remotely.</p>

<p>An {@link edu.cmu.cs.able.eseb.rpc.RemoteOperationStub} represents an
operation in a remote server. Each remote operation stub refers to a specific
operation in a specific object in a specific participant. The stub is the
local representation of the remote operation. It does not hold any reference
to the remote object.</p>

<p>Execution of a remote sub yields a
{@link edu.cmu.cs.able.eseb.rpc.RemoteExecution} object which can then
be waited for synchronously or asynchronously.</p>

<h1>RPC using Java classes</h1>

<p>RPCs using Java classes is done by using reflection and annotations.
The {@link edu.cmu.cs.able.eseb.rpc.JavaRpcFactory} can create stubs that
allow invocation of remote RPCs using Java-like interfaces. These stubs are
more limited than the meta data RPCs as they can only have one return type
and can will executed synchronously or asynchronously depending on the
method's return type.</p>

<p>To declare an interface to be made available remotely, a Java interface
should be created and its method annotated with the
{@link edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping} annotation if they
receive any parameter and annotated with the
{@link edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping} annotation if they
return a value. These annotations are used to map the Java data types into
<em>typelib</em> data types.</p>

<p>The {@link edu.cmu.cs.able.eseb.rpc.JavaRpcFactory} can be used to
create a service from an object that implements an annotated interface
and to create a stub to access a remote object that implements that interface.
</p>
*/
package edu.cmu.cs.able.eseb.rpc;
