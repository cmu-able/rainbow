/**
<h1>Overview</h1>

<p>The <code>rpc</code> package allows RPC invocation over eseb. There are two
methods to perform RPCs, one using meta data and another one using Java
interfaces. Since the latter is based on the former, we'll describe meta
data RPCs first.</p>

<h1>Main concepts</h1>

<p>The main concept in the <code>rpc</code> package is an <em>operation</em>.
An <em>operation</em> has a signature: a set of typed, named parameters which
define its input and output. Differently from Java methods, operations can
have multiple inputs and multiple outputs.</p>

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
remotely. The object is published in an
{@link edu.cmu.cs.able.eseb.rpc.OperationRegistry}. Generally only one
registry will exist per server.</p>

<p>To execute an operation, an instance of
{@link edu.cmu.cs.able.eseb.rpc.RemoteOperationStub} is created where
information about the operation is provided as well as identification of
the destination participant and arguments for execution. A timeout may also be
specified. An execution receives a listener (see
{@link edu.cmu.cs.able.eseb.rpc.OperationExecutionListener}) that is
informed when the operation starts and when it ends, either with a result or
with a timeout. Operation execution is, therefore, asynchronous.</p>

<p>Synchronous execution is possible using the
{@link edu.cmu.cs.able.eseb.rpc.SynchronousOperationExecution} class. This
class is based on the asynchronous execution but will block the invoking
thread during execution until completion or timeout.</p>

<p>Besides allowing calling the RPCs, it is also possible, naturally, to
provide the RPCs to be called. This is done using the
{@link edu.cmu.cs.able.eseb.rpc.OperationRegistry} class where operation
information is registered together with a callback which is invoked every
time an operation is executed.</p>

<h1>RPC using Java classes</h1>

<p>RPCs using Java classes is done by using reflection and annotations.
The {@link edu.cmu.cs.able.eseb.rpc.RemoteRpcFactory} can create stubs that
allow invocation of remote RPCs using Java-like interfaces. These stubs are
more limited than the meta data RPCs as they can only have one return type
and can will executed synchronously or asynchronously depending on the
method's return type.</p>

*/
package edu.cmu.cs.able.eseb.rpc;
