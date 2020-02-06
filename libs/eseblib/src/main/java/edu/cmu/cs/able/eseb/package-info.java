/**
<p><code>eseblib</code> provides implementation of an event bus with a single
channel. All events sent to the bus are sent to all connections. Exceptions
may be made by adding <em>filters</em>.</p>

<p>The event bus is implemented in the <code>bus</code> package and connections
to the bus should be made using the <code>conn</code> package. The server
provides a remote control interface which is implemented in the
<code>bus.rci</code> package.</p>

<p>A user interface for server control is implemented in the
<code>ui.bus</code> package. A sample client user interface is implemented
in the <code>ui.cli</code> package.</p>

<p>The <code>filter</code> package defines filters and filter chains as well
as some basic filters. Filters can be used both on the connection or on the
bus. The <code>filter.participant</code> package defines filters that allow
participants to identify themselves and build a <em>model</em> of all the
participants in the event bus.</p>
 */
package edu.cmu.cs.able.eseb;
