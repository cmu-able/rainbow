// this is a model in which formation change also has latency
open util/ordering[F] as FO
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
	p[GoLoose] in TPGL
	p[GoTight] in TPGT
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

pred goTightTacticProgress[c, c' : CP] {
	c.p[GoTight] != TPGTO/last implies { // tactic is running
		c'.p[GoTight] = TPGTO/next[c.p[GoTight]]
		c'.p[GoTight] = TPGTO/last implies c'.f =  FO/last else c'.f = c.f
	} else {
		c'.p[GoTight] = TPGTO/last // stay in not running state
		c'.f = c.f
	}

	// nothing else changes other than s and the progress
	equalsExcept[c, c', C$f]
	(LT - GoTight) <: c.p in c'.p
}

pred goLooseTacticProgress[c, c' : CP] {
	c.p[GoLoose] != TPGLO/last implies { // tactic is running
		c'.p[GoLoose] = TPGLO/next[c.p[GoLoose]]
		c'.p[GoLoose] = TPGLO/last implies c'.f =  FO/first else c'.f = c.f
	} else {
		c'.p[GoLoose] = TPGLO/last // stay in not running state
		c'.f = c.f
	}

	// nothing else changes other than s and the progress
	equalsExcept[c, c', C$f]
	(LT - GoLoose) <: c.p in c'.p
}

pred oneStepProgress[c, c' : CP] { // is c' reachable from config c in one evaluation period?
	some tc, tc2, tc3 : CP | incAltTacticProgress[c, tc] and decAltTacticProgress[tc, tc2] and goTightTacticProgress[tc2, tc3] and goLooseTacticProgress[tc3, c']
}

sig Result {
	c, c' : CP
} {
	oneStepProgress[c, c']
}

// this reduces the number of unused configurations, but not all
fact reduceUsedConfigs {
	all c : CP | {some r : Result | r.c' = c }
		or {some c' : CP | incAltTacticProgress[c, c'] or decAltTacticProgress[c, c']
			or goTightTacticProgress[c, c'] or goLooseTacticProgress[c, c']
		}
}


// this reduces the number of unused configurations
// each cp in CP is either in a pair in a result, or an intermediate one needed for that pair
// TODO this reduction has not been tested. Test by removing predicate and dumping relation in adapt mgr,
// and then adding again the predicate to compare the final result
fact reduceUsedConfigs {
	all cp : CP | {some r : Result | r.c = cp or r.c' = cp 
			or {some tc2, tc3 : CP |
				(incAltTacticProgress[r.c, cp] and decAltTacticProgress[cp, tc2] and goTightTacticProgress[tc2, tc3] and goLooseTacticProgress[tc3, r.c'])
				or (incAltTacticProgress[r.c, tc2] and decAltTacticProgress[tc2, cp] and goTightTacticProgress[cp, tc3] and goLooseTacticProgress[tc3, r.c'])
				or (incAltTacticProgress[r.c, tc2] and decAltTacticProgress[tc2, tc3] and goTightTacticProgress[tc3, cp] and goLooseTacticProgress[cp, r.c'])
			}
		}
}

pred show {
}

/*
 * (numOfTacticsWithLatency + 1) CP and C to allow the progress for all the tactics with latency + the initial state
 * These are not set in Java because they depend only on the number of tactics, so it they
 * are generated here.
 */
run show for exactly 3 A, exactly 2 TPIA, exactly 2 TPDA,  exactly 2 TPGL,  exactly 2 TPGT, exactly 2 F,  3 C, 3 CP, exactly 1 Result


