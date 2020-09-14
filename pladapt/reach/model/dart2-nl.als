open util/ordering[F] as FO
open util/ordering[TraceElement] as trace
open util/ordering[A] as AO

abstract sig TP {} // tactic progress

abstract sig T {} // tactics
abstract sig LT extends T {} // tactics with latency
one sig IncAlt, DecAlt, GoLoose, GoTight extends T {} // tactics with no latency

// define configuration properties
sig F {} // the different formations
sig A {} // the different altitude levels

/* each element of C represents a configuration */
abstract sig C {
	f : F, // formation
	a : A // altitude level
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
	//p[AddServer] in TAP // restrict each tactic to its own progress class
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
	} or 	incAltTactic[e, e']  or decAltTactic[e, e'] or goLooseTactic[e, e'] or goTightTactic[e, e']
}

pred incAltCompatible[e : TraceElement] {
	!(IncAlt in e.starts) and !(DecAlt in e.starts)
}

pred incAltTactic[e, e' : TraceElement] {
	incAltCompatible[e] and e.cp.a != AO/last
	e'.starts = e.starts + IncAlt
	let c = e.cp, c'=e'.cp | {
		c'.a = AO/next[c.a]

		// nothing else changes
		equalsExcept[c, c', C$a]
		c.p = c'.p
	}
}

pred decAltCompatible[e : TraceElement] {
	!(DecAlt in e.starts) and !(IncAlt in e.starts)
}

pred decAltTactic[e, e' : TraceElement] {
	decAltCompatible[e] and e.cp.a != AO/first
	e'.starts = e.starts + DecAlt
	let c = e.cp, c'=e'.cp | {
		c'.a = AO/prev[c.a]

		// nothing else changes
		equalsExcept[c, c', C$a]
		c.p = c'.p
	}
}

pred goLooseCompatible[e : TraceElement] {
	!(GoLoose in e.starts) and !(GoTight in e.starts)
}

pred goLooseTactic[e, e' : TraceElement] {
	goLooseCompatible[e] and e.cp.f != FO/first
	e'.starts = e.starts + GoLoose

	let c = e.cp, c'=e'.cp | {
		c'.f = FO/first

		// nothing else changes
		equalsExcept[c, c', C$f]
		c'.p = c.p
	}
}

pred goTightCompatible[e : TraceElement] {
	!(GoTight in e.starts) and !(GoLoose in e.starts)
}

pred goTightTactic[e, e' : TraceElement] {
	goTightCompatible[e] and e.cp.f != FO/last
	e'.starts = e.starts + GoTight

	let c = e.cp, c'=e'.cp | {
		c'.f = FO/last

		// nothing else changes
		equalsExcept[c, c', C$f]
		c'.p = c.p
	}
}


pred show {
}

// the scope for TraceElement, C and CP has to be one more than the maximum
// number of tactics that could be started concurrently
// These are not set in the Java program since they depend on the number
// of tactics. It has to be generated here.
run show for exactly 3 A, exactly 2 F, 3 C, 3 CP, 3 TraceElement, 0 TP, 4 T

