// this model considers a state with tactic latency progress, which is used to determine
// tactic applicability, but it assumes tactic executions are instantaneous
open util/ordering[S] as servers
open util/ordering[TAP] as progress // tactic add progress
open util/ordering[TraceElement] as trace
open util/ordering[D] as dimmer
open util/ordering[T] as TO

abstract sig TP {} // tactic progress
sig TAP extends TP {} // one sig for each tactic with latency

abstract sig T {} // tactics
abstract sig LT extends T {} // tactics with latency
one sig IncDimmer, DecDimmer, RemoveServer extends T {} // tactics with no latency
one sig AddServer extends LT {} // tactics with latency

// define configuration properties
sig S {} // the different number of active servers
sig D {} // the different dimmer levels

/* each element of C represents a configuration */
abstract sig C {
	s : S, // the number of active servers
	d : D // dimmer level
}

pred equals[c, c2 : C] {
	all f : C$.fields | c.(f.value) = c2.(f.value) 
}

pred equalsExcept[c, c2 : C, ef : univ] {
	all f : C$.fields | f=ef or c.(f.value) = c2.(f.value) 
}

/*
 * this sig is a config extended with the progress of each tactic with latency
 */
sig CP extends C {
	p: LT -> TP
} {
	~p.p in iden // functional (i.e., p maps each tactic to at most one progress)
	//#p = #LT // every tactic in LT has a mapping in p
	p.univ = LT // every tactic in LT has a mapping in p (p.univ is domain(p) )
	p[AddServer] in TAP // restrict each tactic to its own progress class
}

fact tacticOrdering {
	TO/first = AddServer
	AddServer.next = RemoveServer
	RemoveServer.next = IncDimmer
	IncDimmer.next = DecDimmer
}

sig TraceElement {
	cp : CP,
	starts : set T // tactic started
}

// do not generate atoms that do not belong to the trace
fact {
	CP in TraceElement.cp
}

pred equals[e, e2 : TraceElement] {
	all f : TraceElement$.subfields | e.(f.value) = e2.(f.value) 
}

fact traces {
	let fst = trace/first |  fst.starts = none
	all e : TraceElement - last | let e' = next[e] | {
		// noop as a trace suffix
		// note: this works only because it is not possible to go back to the same state in the same trace
		equals[e, e'] 
		equals[e', trace/last]
	} or 	((addServerTactic[e, e']  or removeServerTactic[e, e'] or decDimmerTactic[e, e'] or incDimmerTactic[e, e']) and
		(let s = e'.starts - e.starts | all t : s | validOrder[t, e]))
}

pred validOrder[t : T, e : TraceElement] {
	all s : T | s in e.starts => !(s in t.nexts)
}

pred addServerCompatible[e : TraceElement] {
	e.cp.p[AddServer] = progress/last
	!(AddServer in e.starts) and 	!(RemoveServer in e.starts)
}

pred addServerTactic[e, e' : TraceElement] {
	addServerCompatible[e] and e.cp.s != servers/last
	e'.starts = e.starts + AddServer
	let c = e.cp, c'=e'.cp | {
		c'.s = servers/next[c.s]

		// nothing else changes
		equalsExcept[c, c', C$s]
		c.p = c'.p
	}
}


pred removeServerCompatible[e : TraceElement] {
	!(AddServer in e.starts) and 	!(RemoveServer in e.starts)
	e.cp.p[AddServer] = progress/last // add server tactic not running
}

pred removeServerTactic[e, e' : TraceElement] {
	removeServerCompatible[e] and e.cp.s != servers/first
	e'.starts = e.starts + RemoveServer
	let c = e.cp, c'=e'.cp | {
		c'.s = servers/prev[c.s]

		// nothing else changes
		equalsExcept[c, c', C$s]
		c'.p = c.p
	}
}

pred incDimmerCompatible[e : TraceElement] {
	!(IncDimmer in e.starts) and !(DecDimmer in e.starts)
}

pred incDimmerTactic[e, e' : TraceElement] {
	incDimmerCompatible[e] and e.cp.d != dimmer/last
	e'.starts = e.starts + IncDimmer

	let c = e.cp, c'=e'.cp | {
		c'.d = c.d.next

		// nothing else changes
		equalsExcept[c, c', C$d]
		c'.p = c.p
	}
}

pred decDimmerCompatible[e : TraceElement] {
	!(DecDimmer in e.starts) and !(IncDimmer in e.starts)
}

pred decDimmerTactic[e, e' : TraceElement] {
	decDimmerCompatible[e] and e.cp.d != dimmer/first
	e'.starts = e.starts + DecDimmer

	let c = e.cp, c'=e'.cp | {
		c'.d = c.d.prev

		// nothing else changes
		equalsExcept[c, c', C$d]
		c'.p = c.p
	}
}


pred show {
}

// the scope for TraceElement, C and CP has to be one more than the maximum
// number of tactics that could be started concurrently
// These are not set in the Java program since they depend on the number
// of tactics. It has to be generated here.
run show for exactly 3 S, exactly 3 TAP, 2 D, 3 C, 3 CP, 3 TraceElement

