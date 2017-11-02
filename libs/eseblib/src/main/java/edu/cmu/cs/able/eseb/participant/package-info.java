/**
<p>
The <code>participant</code> package provides a mechanism for unique client
identification and for clients to learn about each other.
</p>
<p>
The key idea behind the participant mechanism is that each bus connection
may be a <em>participant</em>. A participant has a unique ID (normally
randomly generated) and announces it regularly in the event bus along with
some optionally provided metadata. Participants are implemented with the
{@link edu.cmu.cs.able.eseb.participant.ParticipantIdentificationTest} class.
Participants do not offer any functionality other than announcing themselves.
</p>
<p>
The <code>participant</code> package also provides an event filter,
the {@link edu.cmu.cs.able.eseb.participant.ParticipantModelFilter} that
listens to events from participants and maintains a model of all participants
that have announced in the bus. This filter can be used to build a list of all
participants connected to the bus.
</p>
<p>
The relation between connections and participants is not made automatically.
Participants and participant model filters build on top of connections. A
single connection may host multiple participants and mutliple model filters.
</p>
*/
package edu.cmu.cs.able.eseb.participant;
