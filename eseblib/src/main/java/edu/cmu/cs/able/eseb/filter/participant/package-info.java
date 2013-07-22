/**
The <code>clientid</code> package provides filters for client identification.
The package provides two filters, one that works as an output filter
and one that works as an input filter. When the output filter is created, a
random ID is generated which is attributed to the participant. Afterwards,
regularly, the output filter sends a message containing the ID and some
optional information. The input filter will remove these messages and will
interpret them providing an interface to query which are the existing
participants in the bus.
*/
package edu.cmu.cs.able.eseb.filter.participant;
