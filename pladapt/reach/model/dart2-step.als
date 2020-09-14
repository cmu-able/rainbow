open util/ordering[F] as FO
open util/ordering[A] as AO
open util/ordering[TPIA] as TPIAO
open util/ordering[TPDA] as TPDAO

abstract sig TP {} // tactic progress
sig TPIA extends TP {} // one sig for each tactic with latency
sig TPDA extends TP {} // one sig for each tactic with latency

abstract sig T {} // tactics
abstract sig LT extends T {} // tactics with latency
one sig GoLoose, GoTight extends T {} // tactics with no latency
one sig IncAlt, DecAlt extends LT {} // tactics with latency

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

fact uniqueInstances { all disj c, c2 : CP | !equals[c, c2] or c.p != c2.p}

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
}

pred incAltTacticProgress[c, c' : CP] {
	c.p[IncAlt] != TPIAO/last implies { // tactic is running
		c'.p[IncAlt] = TPIAO/next[c.p[IncAlt]]
		c'.p[IncAlt] = TPIAO/last implies c'.a = AO/next[c.a] else c'.a = c.a
	} else {
		c'.p[IncAlt] = TPIAO/last // stay in not running state
		c'.a = c.a
	}

	// nothing else changes other than s and the progress
	equalsExcept[c, c', C$a]
	(LT - IncAlt) <: c.p in c'.p
}

pred decAltTacticProgress[c, c' : CP] {
	c.p[DecAlt] != TPDAO/last implies { // tactic is running
		c'.p[DecAlt] = TPDAO/next[c.p[DecAlt]]
		c'.p[DecAlt] = TPDAO/last implies c'.a = AO/prev[c.a] else c'.a = c.a
	} else {
		c'.p[DecAlt] = TPDAO/last // stay in not running state
		c'.a = c.a
	}

	// nothing else changes other than s and the progress
	equalsExcept[c, c', C$a]
	(LT - DecAlt) <: c.p in c'.p
}


pred oneStepProgress[c, c' : CP] { // is c' reachable from config c in one evaluation period?
	some tc : CP | incAltTacticProgress[c, tc] and decAltTacticProgress[tc, c']
}

sig Result {
	c, c' : CP
} {
	oneStepProgress[c, c']
}

// this reduces the number of unused configurations
// each cp in CP is either in a pair in a result, or an intermediate one needed for that pair
fact reduceUsedConfigs {
	all cp : CP | {some r : Result | r.c = cp or r.c' = cp 
			or (incAltTacticProgress[r.c, cp] and decAltTacticProgress[cp, r.c'])
		}
}

pred show {
}

/*
 * (numOfTacticsWithLatency + 1) for CP and C to allow the progress for all the tactics with latency + the initial state
 * These are not set in Java because they depend only on the number of tactics, so they
 * are generated here.
 */
run show for exactly 3 A, exactly 2 TPIA, exactly 2 TPDA, exactly 2 F,  3 C, 3 CP, exactly 1 Result


