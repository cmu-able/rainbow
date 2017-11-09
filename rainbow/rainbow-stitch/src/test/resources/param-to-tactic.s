module test.strategies;

define boolean existsIntViolation = true;

tactic setInt(int i) {
 
  int s = 0;
 
  
  condition {
    true;
  }
  
  action {
    s = s + i;
  }
  
  effect {
    s' == s + i;
  }
}

strategy handleInt[existsIntViolation] {
  t1: (true) -> setInt(2) @[2000] {
    t1a: (success) -> done;
  }
  t2: (default) -> TNULL;
}