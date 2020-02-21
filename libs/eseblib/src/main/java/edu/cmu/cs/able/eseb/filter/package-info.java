/**
<p>The <code>filter</code> package provides support for filters and filter
chains. Event filters are implemented in the
{@link edu.cmu.cs.able.eseb.filter.EventFilter} class and send events to
an event sink ({@link edu.cmu.cs.able.eseb.filter.EventSink}). A filter is
itself a sink making possible to build filter chains
({@link edu.cmu.cs.able.eseb.filter.EventFilterChain}).</p>

<p>Filters provide some basic information so that information about them
can be displayed in the server using the <em>remote control interface</em>.</p>

<p>Some elementary filters are provided by this package.</p>
 */
package edu.cmu.cs.able.eseb.filter;
