// this is a model in which formation change also has latency
open util/ordering[F] as FO
open util/ordering[TraceElement] as trace
open util/ordering[A] as AO
open util/ordering[TPIA] as TPIAO
open util/ordering[TPDA] as TPDAO
open util/ordering[TPGT] as TPGTO
open util/ordering[TPGL] as TPGLO

abstract sig TP {} // tactic progress
sig TPIA extends TP {} // one sig for each tactic with latency
sig TPDA extends TP {} // one sig for each tactic with latency
sig TPGT extends TP {} // one sig for each tactic with latency
sig TPGL extends TP {} // one sig for each tactic with latency

abstract sig T {} // tactics
abstract sig LT extends T {} // tactics with latency
one sig GoLoose, GoTight, IncAlt, DecAlt extends LT {} // tactics with latency

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
	p[IncAlt] in TPIA // restrict each tactic to its own progress class
	p[DecAlt] in TPDA
	p[GoLoose] in TPGL
	p[GoTight] in TPGT
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
	} or 	incAltTacticStart[e, e']  or decAltTacticStart[e, e'] or goLooseTacticStart[e, e'] or goTightTacticStart[e, e']
}

pred incAltCompatible[e : TraceElement] {
	e.cp.p[IncAlt] = TPIAO/last // IncAlt tactic not running
	e.cp.p[DecAlt] = TPDAO/last // DecAlt tactic not running
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

pred goLooseCompatible[e : TraceElement] {
	e.cp.p[GoLoose] = TPGLO/last
	e.cp.p[GoTight] = TPGTO/last
}

pred goLooseTacticStart[e, e' : TraceElement] {
	goLooseCompatible[e] and e.cp.f != FO/first
	e'.starts = e.starts + GoLoose

	let c = e.cp, c'=e'.cp | {
		c'.p[GoLoose] = TPGLO/first

		// nothing else changes
		equals[c, c']
		(LT - GoLoose) <: c.p in c'.p
	}
}

pred goTightCompatible[e : TraceElement] {
	e.cp.p[GoTight] = TPGTO/last
	e.cp.p[GoLoose] = TPGLO/last
}

pred goTightTacticStart[e, e' : TraceElement] {
	goTightCompatible[e] and e.cp.f != FO/last
	e'.starts = e.starts + GoTight

	let c = e.cp, c'=e'.cp | {
		c'.p[GoTight] = TPGTO/first

		// nothing else changes
		equals[c, c']
		(LT - GoTight) <: c.p in c'.p
	}
}


pred show {
}

// the scope for TraceElement, C and CP has to be one more than the maximum
// number of tactics that could be started concurrently
// These are not set in the Java program since they depend on the number
// of tactics. It has to be generated here.
run show for exactly 3 A, exactly 2 TPIA, exactly 2 TPDA, exactly 2 F,  exactly 2 TPGL,  exactly 2 TPGT, 3 C, 3 CP, 3 TraceElement
