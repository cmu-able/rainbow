module swim.strategies;
import op "org.sa.rainbow.stitch.lib.*"; 
import op "org.sa.rainbow.model.acme.swim.Swim";
import lib "swimTactics.t.s";

define int numberOfServers = Set.size(select s : T.ServerT in M.components | true);

strategy AddServer
[ Swim.availableServices(M, T.ServerT) > 0 ] {
  t1: (true) -> TAddServer() @[180000 /*ms*/] { 
      t1a: (default) -> done;
  }
}


//strategy ReduceContentWhileAddingServer
//[ Swim.availableServices(M, T.ServerT) > 0 && M.LB0.dimmer > M.DIMMER_MARGIN] {
//  t1: (true) -> TSetMinDimmer() { 
//      t1a: (true) -> TAddServer() @[180000 /*ms*/] {
//	t1a1: (true) -> TSetMaxDimmer() { 
//		t1a1a: (default) -> done;
//	}
//      }
//  }
//}

strategy ReduceContentAndAddServer
[ Swim.availableServices(M, T.ServerT) > 0 && M.LB0.dimmer > M.DIMMER_MARGIN] {
  t1: (true) -> TDecDimmer() { 
      t1a: (true) -> TAddServer() @[180000 /*ms*/] {
	t1a1: (default) -> done;
      }
  }
}

strategy RestoreFullContent
[ M.LB0.dimmer < 1 - M.DIMMER_MARGIN] {
  t1: (true) -> TSetMaxDimmer() { 
      t1a: (default) -> done;
  }
}


strategy MinimizeContent
[ M.LB0.dimmer > M.DIMMER_MARGIN] {
  t1: (true) -> TSetMinDimmer() { 
      t1a: (default) -> done;
  }
}


strategy RemoveServer
[ numberOfServers > 1 ] {
  t1: (true) -> TRemoveServer() { 
      t1a: (default) -> done;
  }
}

strategy NoOp
[ true ] {
  t1: (true) -> done;
}




