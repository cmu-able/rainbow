open util/ordering[F] as formation
open util/ordering[TightP] as tightPeriods
//open util/ordering[TAP] as progress // tactic add progress
open util/ordering[TraceElement] as trace

//abstract sig TP {} // tactic progress
//sig TAP extends TP {} // one sig for each tactic with latency

abstract sig T {} // tactics
//abstract sig LT extends T {} // tactics with latency
one sig SwitchToLoose, SwitchToTight extends T {} // tactics with no latency
//one sig AddServer extends LT {} // tactics with latency

// define configuration properties
sig F {} // the different formations
sig TightP{} // the number of periods flown tight

/* each element of C represents a configuration */
sig C {
	f : F, // the formation
	tightPeriodsLeft: TightP
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
	//p: LT -> TP
} {
	//~p.p in iden // functional (i.e., p maps each tactic to at most one progress)
	//#p = #LT // every tactic in LT has a mapping in p
	//p.univ = LT // every tactic in LT has a mapping in p (p.univ is domain(p) )
//	p[AddServer] in TAP // restrict each tactic to its own progress class
}


// for the timestep there's no need for a trace, just a predicate similar to what it is now
// where all the tactics in LT have a chance to make progress.
// after that, the trace is for collecting the tactic starts


sig TraceElement {
	cp : CP,
	starts : set T // tactic started
}

// do not generate atoms that do not belong to the trace
fact {
	CP in TraceElement.cp
	#C = #CP
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
	} or 	switchToLooseTactic[e, e'] or switchToTightTactic[e, e']
}

pred switchToTightCompatible[e : TraceElement] {
	!(SwitchToTight in e.starts) and !(SwitchToLoose in e.starts)
}

pred switchToTightTactic[e, e' : TraceElement] {
	switchToTightCompatible[e] and e.cp.f != formation/last
	e'.starts = e.starts + SwitchToTight

	let c = e.cp, c'=e'.cp | {
		c'.f = c.f.next

		// nothing else changes
		equalsExcept[c, c', C$f]
		//c'.p = c.p
	}
}

pred switchToLooseCompatible[e : TraceElement] {
	!(SwitchToLoose in e.starts) and !(SwitchToTight in e.starts)
}

pred switchToLooseTactic[e, e' : TraceElement] {
	switchToLooseCompatible[e] and e.cp.f != formation/first
	e'.starts = e.starts + SwitchToLoose

	let c = e.cp, c'=e'.cp | {
		c'.f = c.f.prev

		// nothing else changes
		equalsExcept[c, c', C$f]
		//c'.p = c.p
	}
}

pred show {
	//!equals[trace/first, trace/first.next]
}

// the scope for TraceElement has to be one more than the number of tactics
// the scope for TraceElement is not set in the Java program since it depends on the number
// of tactics. It has to be generated here.
run show for exactly 2 F, exactly 4 TightP, 8 C, 8 CP,  3 TraceElement
//run traces for exactly 3 S, exactly 3 TAP, exactly 2 TRP, 6 C, 36 CP

