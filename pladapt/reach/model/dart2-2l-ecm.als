open util/ordering[F] as FO
open util/ordering[TraceElement] as trace
open util/ordering[A] as AO
open util/ordering[TPIA] as TPIAO
open util/ordering[TPDA] as TPDAO
open util/ordering[TPIA2] as TPIAO2
open util/ordering[TPDA2] as TPDAO2
open util/boolean
open util/ordering[T] as TO

abstract sig TP {} // tactic progress
sig TPIA extends TP {} // one sig for each tactic with latency
sig TPDA extends TP {} // one sig for each tactic with latency
sig TPIA2 extends TP {} // one sig for each tactic with latency
sig TPDA2 extends TP {} // one sig for each tactic with latency

abstract sig T {} // tactics
abstract sig LT extends T {} // tactics with latency
one sig GoLoose, GoTight, EcmOn, EcmOff extends T {} // tactics with no latency
one sig IncAlt, DecAlt, IncAlt2, DecAlt2 extends LT {} // tactics with latency

// define configuration properties
sig F {} // the different formations
sig A {} // the different altitude levels

/* each element of C represents a configuration */
abstract sig C {
	f : F, // formation
	a : A, // altitude level
	ecm : Bool // electronic countermeasures
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
	p[IncAlt] in TPIA // restrict each tactic to its own progress class
	p[DecAlt] in TPDA
	p[IncAlt2] in TPIA2 // restrict each tactic to its own progress class
	p[DecAlt2] in TPDA2
}


fact tacticOrdering {
	TO/first = GoLoose
	GoLoose.next = GoTight
	GoTight.next = IncAlt
	IncAlt.next = DecAlt
	DecAlt.next = IncAlt2
	IncAlt2.next = DecAlt2
	DecAlt2.next = EcmOn
	EcmOn.next = EcmOff
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
	} or 	((incAltTacticStart[e, e']  or decAltTacticStart[e, e'] or 
			incAlt2TacticStart[e, e']  or decAlt2TacticStart[e, e'] or
			goLooseTactic[e, e'] or goTightTactic[e, e'] or
			ecmOnTactic[e, e'] or  ecmOffTactic[e, e']) and
		(let s = e'.starts - e.starts | all t : s | validOrder[t, e]))
}

pred validOrder[t : T, e : TraceElement] {
	all s : T | s in e.starts => !(s in t.nexts)
}

pred incAltCompatible[e : TraceElement] {
	e.cp.p[IncAlt] = TPIAO/last // IncAlt tactic not running
	e.cp.p[DecAlt] = TPDAO/last // DecAlt tactic not running
	e.cp.p[IncAlt2] = TPIAO2/last // IncAlt2 tactic not running
	e.cp.p[DecAlt2] = TPDAO2/last // DecAlt2 tactic not running
}

pred incAltTacticStart[e, e' : TraceElement] {
	incAltCompatible[e] and e.cp.a != AO/last
	e'.starts = e.starts + IncAlt
	let c = e.cp, c'=e'.cp | {
		c'.p[IncAlt] = TPIAO/first

		// nothing else changes
		equals[c, c']
		(LT - IncAlt) <: c.p in c'.p
	}
}


pred decAltCompatible[e : TraceElement] {
	e.cp.p[DecAlt] = TPDAO/last // DecAlt tactic not running
	e.cp.p[IncAlt] = TPIAO/last // IncAlt tactic not running
	e.cp.p[IncAlt2] = TPIAO2/last // IncAlt2 tactic not running
	e.cp.p[DecAlt2] = TPDAO2/last // DecAlt2 tactic not running
}

pred decAltTacticStart[e, e' : TraceElement] {
	decAltCompatible[e] and e.cp.a != AO/first
	e'.starts = e.starts + DecAlt
	let c = e.cp, c'=e'.cp | {
		c'.p[DecAlt] = TPDAO/first

		// nothing else changes
		equals[c, c']
		(LT - DecAlt) <: c.p in c'.p
	}
}


pred incAlt2Compatible[e : TraceElement] {
	e.cp.p[IncAlt] = TPIAO/last // IncAlt tactic not running
	e.cp.p[DecAlt] = TPDAO/last // DecAlt tactic not running
	e.cp.p[IncAlt2] = TPIAO2/last // IncAlt2 tactic not running
	e.cp.p[DecAlt2] = TPDAO2/last // DecAlt2 tactic not running
}

pred incAlt2TacticStart[e, e' : TraceElement] {
	incAlt2Compatible[e] and e.cp.a != AO/last and e.cp.a != AO/last.prev
	e'.starts = e.starts + IncAlt2
	let c = e.cp, c'=e'.cp | {
		c'.p[IncAlt2] = TPIAO2/first

		// nothing else changes
		equals[c, c']
		(LT - IncAlt2) <: c.p in c'.p
	}
}


pred decAlt2Compatible[e : TraceElement] {
	e.cp.p[DecAlt] = TPDAO/last // DecAlt tactic not running
	e.cp.p[IncAlt] = TPIAO/last // IncAlt tactic not running
	e.cp.p[IncAlt2] = TPIAO2/last // IncAlt2 tactic not running
	e.cp.p[DecAlt2] = TPDAO2/last // DecAlt2 tactic not running
}

pred decAlt2TacticStart[e, e' : TraceElement] {
	decAlt2Compatible[e] and e.cp.a != AO/first and e.cp.a != AO/first.next
	e'.starts = e.starts + DecAlt2
	let c = e.cp, c'=e'.cp | {
		c'.p[DecAlt2] = TPDAO2/first

		// nothing else changes
		equals[c, c']
		(LT - DecAlt2) <: c.p in c'.p
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


pred ecmOnCompatible[e : TraceElement] {
	!(EcmOn in e.starts) and !(EcmOff in e.starts)
}

pred ecmOnTactic[e, e' : TraceElement] {
	ecmOnCompatible[e] and e.cp.ecm.isFalse
	e'.starts = e.starts + EcmOn

	let c = e.cp, c'=e'.cp | {
		c'.ecm = boolean/True

		// nothing else changes
		equalsExcept[c, c', C$ecm]
		c'.p = c.p
	}
}

pred ecmOffCompatible[e : TraceElement] {
	!(EcmOff in e.starts) and !(EcmOn in e.starts)
}

pred ecmOffTactic[e, e' : TraceElement] {
	ecmOffCompatible[e] and e.cp.ecm.isTrue
	e'.starts = e.starts + EcmOff

	let c = e.cp, c'=e'.cp | {
		c'.ecm = boolean/False

		// nothing else changes
		equalsExcept[c, c', C$ecm]
		c'.p = c.p
	}
}


pred show {
}

// the scope for TraceElement, C and CP has to be one more than the maximum
// number of tactics that could be started concurrently
// These are not set in the Java program since they depend on the number
// of tactics. It has to be generated here.
run show for exactly 3 A, exactly 2 TPIA, exactly 2 TPDA, exactly 2 TPIA2, exactly 2 TPDA2, exactly 2 F, 4 C, 4 CP, 4 TraceElement
