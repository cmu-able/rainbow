module dos.strategies;
	
import model "ZNewsSys:Acme" { ZNewsSys as M, ZNewsFam as T, ZNewsDosFam as D} ;
import op "org.sa.rainbow.stitch.lib.*";

//--------------------------------------------------------------------------------------------------
// ! exists c : ClientT in self.components | (c.isMalicious and !c.is_blackholed)
//
// Failing this would cause the strategy to fire
// In the long run, isMalicious could be a float that captures the probability 
// that a client is malicious -- in this case, we would have a threshold value
//	
// Countermeasure #3: Black Hole the attacker(s)
// Blackholing means replying with ICMP giving reason for Black hole the attacking
// client by redirecting connections from a client 
// for some period of time (i.e. black hole the traffic).
// 
tactic blackholeAttacker () {
 condition { 
   exists c:D.ZNewsClientT in M.components | ((c.maliciousness > M.MALICIOUS_THRESHOLD) && !(exists b:D.ZNewsLBT in M.components | M.contains(c.deploymentLocation, b.blackholed)));
   
   // currently just checking if the client is identified as malicious.
   // exists c:D.ZNewsClientT in M.components | c.maliciousness > M.MALICIOUS_THRESHOLD;
 }
 action { 
  // Find the attacker. Currently, this is done by checking if client is identified as an attacker
  // the blackHole() routine re-routes all traffic from a client away from the backed servers
  // thereby preventing attackers from consuming ZNN resources.
  //
  set evilClients = { select c : D.ZNewsClientT in M.components | c.maliciousness > M.MALICIOUS_THRESHOLD};  
  set blackholedClients = M.LB0.blackholed;
  for (T.ClientT target : evilClients) {
    blackholedClients = Set.add (blackholedClients, target.deploymentLocation);
  }
  M.setBlackholed (M.LB0, blackholedClients);
 }
 effect { 
  // all the clients that were malicious are now blackholed. Perhaps the "and" is too strong here?
  // Do we want to have clients blackholed that are not malicious? This isBlackHoled() routuine returns true
  // if the client is blackholed and false otherwise
  forall c:D.ZNewsClientT in M.components | ((c.maliciousness > M.MALICIOUS_THRESHOLD) -> (exists b:D.ZNewsLBT in M.components | M.contains (c.deploymentLocation,b.blackholed)));
  //forall c:T.ClientT in M.components | (c.maliciousness > M.MALICIOUS_THRESHOLD) -> !c.isBlackholed));
 }
}

tactic unblackholeAttacker () {
  condition {
     exists c:D.ZNewsClientT in M.components | exists l:D.ZNewsLBT in M.components | (M.contains(c.deploymentLocation, l.blackholed) && c.maliciousness <= M.MALICIOUS_THRESHOLD);
  }
  action {
  	set reformedClients = {select c : D.ZNewsClientT in M.components | c.maliciousness <= M.MALICIOUS_THRESHOLD};
  	set lbs = {select l : D.ZNewsLBT in M.components | M.size (l.blackholed) > 0};
  	for (D.ZNewsLBT l : lbs) {
	    set blackholedClients = l.blackholed;
  		for (D.ZNewsClientT c : reformedClients) {
  		    // Curiously, if the unblackhole is only in the op.map and
  		    // not in the the EffectOp class, then the parameters need
  		    // to be paired with their parameter name
  		    // Thus, (...,"client", c.deploymentLocation) here
  		    // compared to blackhole above, which is actually
  		    // a method provided in the EffectOp class
			blackholedClients = Set.remove (blackholedClients, c.deploymentLocation);
  		}
		M.setBlackholed (l, blackholedClients);
  	}
  }
  effect {
  	forall lb : D.ZNewsLBT in M.components | forall ip : string in lb.blackholed | forall c : D.ZNewsClientT in M.components | ((c.deploymentLocation == ip) -> (c.maliciousness > M.MALICIOUS_THRESHOLD));
  }
}

tactic throttleSuspicious () {
  condition {
    exists c:D.ZNewsClientT in M.components | ((c.maliciousness > M.SUSCPICIOUS_THRESHOLD) && !(exists b:D.ZNewsLBT in M.components | M.contains(c.deploymentLocation, b.throttled)));
  }
  action { 
  // Find the attacker. Currently, this is done by checking if client is identified as an attacker
  // the blackHole() routine re-routes all traffic from a client away from the backed servers
  // thereby preventing attackers from consuming ZNN resources.
  //
  set suspciousClients = { select c : D.ZNewsClientT in M.components | c.maliciousness > M.SUSCPICIOUS_THRESHOLD};  
  set throttledClients = M.LB0.throttled;
  for (T.ClientT target : suspciousClients) {
    throttledClients = Set.add (throttledClients, target.deploymentLocation);
  }
  M.setThrottled (M.LB0, throttledClients);
 }
 effect { 
  // all the clients that were malicious are now blackholed. Perhaps the "and" is too strong here?
  // Do we want to have clients blackholed that are not malicious? This isBlackHoled() routuine returns true
  // if the client is blackholed and false otherwise
  forall c:D.ZNewsClientT in M.components | ((c.maliciousness > M.SUSCPICIOUS_THRESHOLD) -> (exists b:D.ZNewsLBT in M.components | M.contains (c.deploymentLocation,b.throttled)));
 }
}

tactic unthrottle () {
  condition {
     exists c:D.ZNewsClientT in M.components | exists l:D.ZNewsLBT in M.components | (M.contains(c.deploymentLocation, l.throttled) && c.maliciousness <= M.SUSCPICIOUS_THRESHOLD);
  }
  action {
  	set reformedClients = {select c : D.ZNewsClientT in M.components | c.maliciousness <= M.SUSCPICIOUS_THRESHOLD};
  	set lbs = {select l : D.ZNewsLBT in M.components | M.size (l.throttled) > 0};
  	for (D.ZNewsLBT l : lbs) {
	    set throttledClients = l.throttled;
  		for (D.ZNewsClientT c : reformedClients) {
			throttledClients = Set.remove (throttledClients, c.deploymentLocation);
  		}
		M.setThrottled (l, throttledClients);
  	}
  }
  effect {
  	forall lb : D.ZNewsLBT in M.components | forall ip : string in lb.throttled | forall c : D.ZNewsClientT in M.components | ((c.deploymentLocation == ip) -> (c.maliciousness > M.SUSCPICIOUS_THRESHOLD));
  }
}

tactic forceReauthentication () {
  condition {
    true;
  }
  action {
    for (ProxyT lb : M.components) {
	  M.forceReauthentication (lb);
	}
  }
  effect {
    true;
  }
}

tactic addCaptcha () {
  condition {
     exists lb:D.ZNewsLBT in M.components | !lb.captchaEnabled;
  }
  action {
    set lbs = {select l : D.ZNewsLBT in M.components | !l.captchaEnabled};
	for (D.ZNewsLBT l : lbs) {
	    M.setCaptchaEnabled (l, true);
	}
  }
  effect {
    forall lb:D.ZNewsLBT in M.components | lb.captchaEnabled;
  }
}

tactic removeCaptcha () {
  condition {
       exists lb:D.ZNewsLBT in M.components | lb.captchaEnabled;
  }
  action {
  set lbs = {select l : D.ZNewsLBT in M.components | l.captchaEnabled};
	for (D.ZNewsLBT l : lbs) {
	    M.setCaptchaEnabled (l, false);
	}
  }
  effect {
    forall lb:D.ZNewsLBT in M.components | !lb.captchaEnabled;
  }
}
  
  
