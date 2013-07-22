/**
<h1>Overview</h1>

<p>The <code>rpc</code> package allows RPC invocation over eseb. There are two
methods to perform RPCs, one using meta data and another one using Java
interfaces. Since the latter is based on the former, we'll describe meta
data RPCs first.</p>

<h1>Meta data RPCs</h1>

<p>Meta data RPCs are based on participant meta data (see
{@link edu.cmu.cs.able.eseb.filter.participant.ParticipantIdentifier} for
information about participants and meta data). Meta data with a specific key
publishes information on which operations are available on a participant.
Each operation is uniquely identified by a name. Operations have several
parametes which may be either input or output, but not both. Information about
operations is kept in the {@link edu.cmu.cs.able.eseb.rpc.OperationInformation}
class.</p>

<p>To execute an operation, an instance of
{@link edu.cmu.cs.able.eseb.rpc.OperationExecution} is created where
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
