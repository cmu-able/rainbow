/*
 * Adaptation script for security attacks
 */
 
module dos.strategies;
import op "org.sa.rainbow.stitch.lib.*"; 
import op "org.sa.rainbow.model.acme.znn.ZNN";
import lib "dosTactics.s";
import lib "newssiteTactics.s";

//define boolean styleApplies = Model.hasType(M, "ClientT") && Model.hasType(M, "ServerT");

define boolean haveMaliciousAndSuspicious = exists c : D.ZNewsClientT in M.components | (c.maliciousness >= M.SUSPICIOUS_THRESHOLD);

define boolean unhandledMalicious = exists c : D.ZNewsClientT in M.components | ((c.maliciousness >= M.MALICIOUS_THRESHOLD) && !((exists b:D.ZNewsLBT in M.components | M.contains (c.deploymentLocation, b.blackholed)) || c.captcha == -1 || c.authenticate == -1));

// There are people who are suspicious but haven't been throttled
define boolean unhandledSuspicious = exists c : D.ZNewsClientT in M.components | ((c.maliciousness < M.MALICIOUS_THRESHOLD && c.maliciousness > M.SUSPICIOUS_THRESHOLD) && !((exists b : D.ZNewsLBT in M.components | M.contains (c.deploymentLocation, b.throttled)) || c.captcha == -1 || c.authenticate == -1));

// There are clients blackholed that are not malicious
define boolean cUnfairlyPenalizedClients = !(forall lb : D.ZNewsLBT in M.components | forall ip : string in lb.blackholed | forall c : D.ZNewsClientT in M.components | ((c.deploymentLocation == ip) -> (c.maliciousness > M.MALICIOUS_THRESHOLD)));

// There are clients throttled that are not suspicous
define boolean cUnfairlyThrottledClients = !(forall lb : D.ZNewsLBT in M.components | forall ip : string in lb.throttled | forall c : D.ZNewsClientT in M.components | ((c.deploymentLocation == ip) -> (c.maliciousness > M.SUSPICOUS_THRESHOLD && c.maliciousness <= M.SUSPICIOUS_THRESHOLD)));

// We haven't captchaed
define boolean cNotChallenging = exists c : D.ZNewsLBT in M.components | (c.captchaEnabled == false);

// Non-malicious clients are suffering high response times 
define boolean cHiRespTime = exists c : T.ClientT/*,D.ZNewsClientT*/ in M.components | (c.experRespTime > M.MAX_RESPTIME);

// There is atleast one reserve server available
define boolean serversAvailable = ZNN.availableServices(M,T.ServerT) > 0;
	
/* Stop the attack by placing attackers into a blackhole.  This strategy is triggered
 * by the presence of an attacker and an impact on response time
 */
strategy EliminateStrategy
[ cHiRespTime && (unhandledMalicious || unhandledSuspicious) ] {
  t0: (unhandledMalicious || unhandledSuspicious) -> blackholeAttacker() @[5000 /*ms*/] { 
      t1: (!unhandledMalicious) -> done;	
	  t1a: (unhandledSuspicious) -> throttleSuspicious () @[5000] {
        t2a: (default) -> TNULL;  // in this case, we have no more steps to take
		t2b: (success) -> done;
      }
  }
  t3: (default) -> TNULL;
}

strategy RestoreStrategy
[cUnfairlyPenalizedClients || cUnfairlyThrottledClients] {
  t0: (cUnfairlyPenalizedClients) -> unblackholeAttacker () @[2000 /*ms*/] {
    t1 : (!cUnfairlyPenalizedClients) -> done;
    t1a: (default) -> TNULL;
  }
  t2 : (cUnfairlyThrottledClients) -> unthrottle () @[2000] {
    t2a: (!cUnfairlyThrottledClients) -> done;
	t2b: (default) -> TNULL;
  }
}

strategy Challenge [cHiRespTime && (unhandledMalicious || unhandledSuspicious)] {
  t0: (cNotChallenging) -> addCaptcha () @[5000] {
      t1: (success) -> done;
	  t1a: (default) -> TNULL;
  }
  t2: (!cNotChallenging) -> forceReauthentication () @[5000] {
      t3: (success) -> done;
	  t3a: (default) -> TNULL;
	  
  }
}



strategy DisableChallenges [!haveMaliciousAndSuspicious && !cNotChallenging] {
   t0: (!cNotChallenging) -> removeCaptcha () @[1000] {
       t1: (cNotChallenging) -> done;
   }
}


strategy SimpleReduceResponseTime
[cHiRespTime && (unhandledMalicious || unhandledSuspicious)] {
  t0: (/*hiLoad*/ cHiRespTime) -> enlistServers(1) @[1000 /*ms*/] {
    t1: (!cHiRespTime) -> done;
    t2: (cHiRespTime) -> lowerFidelity(2, 100) @[3000 /*ms*/] {
      t2a: (!cHiRespTime) -> done;
      t2b: (default) -> TNULL;  // in this case, we have no more steps to take
    }
  }
}

/* This strategy allocates more capacity to counter the impact of a DoS attack. 
 */
//strategy IncreaseCapacityStrategy
//[ styleApplies && cHiRespTime && serversAvailable] {
//  t0: (/* under attack */ cHiRespTime) -> enlistServer(1) @[1000 /*ms*/] {
//      t1: (!cHiRespTime) -> done;	
//      t1a: (default) -> TNULL;  // in this case, we have no more steps to take
//  }
//}


