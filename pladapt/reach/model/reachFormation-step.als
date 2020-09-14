open util/ordering[F] as formation
open util/ordering[TightP] as tightPeriods
//open util/ordering[TAP] as progress // tactic add progress

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
	tightPeriodsLeft : TightP
}

pred equals[c, c2 : C] {
	all f : C$.fields | c.(f.value) = c2.(f.value) 
}

pred equalsExcept[c, c2 : C, ef : univ] {
	all f : C$.fields | f=ef or c.(f.value) = c2.(f.value) 
}

fact uniqueInstances { all disj c, c2 : CP | !equals[c, c2] /*or c.p != c2.p */}

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


pred oneStepProgress[c, c' : CP] { // is c' reachable from config c in one evaluation period?
	//addServerTacticProgress[c, c'] // this should be the composition of the progress predicate for all the tactics
	//system evolution regardless of running tactics
	equalsExcept[c, c', C$tightPeriodsLeft]
	(c.f=formation/last // tight
		and c.tightPeriodsLeft!=tightPeriods/first
		and c'.tightPeriodsLeft=c.tightPeriodsLeft.prev)
 	or (c.f=formation/first and c'.tightPeriodsLeft=c.tightPeriodsLeft)
}


sig Result { reachable : CP->CP } // why does prefixing this with "one" produces more instances?

pred show {
	one r : Result | all c1,c2 : CP | c1->c2 in r.reachable <=> oneStepProgress[c1,c2]
}


run show for exactly 2 F, exactly 4 TightP, exactly 8 C, exactly 8 CP, exactly 1 Result

