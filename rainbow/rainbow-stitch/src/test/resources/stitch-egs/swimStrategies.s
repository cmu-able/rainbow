module swim.strategies;
import op "org.sa.rainbow.stitch.lib.*"; 
import lib "swimTactics.t.s";

strategy IncDimmer
[ true ] {
  t1: (true) -> TIncDimmer() { 
      t1a: (default) -> done;
  }
}

strategy DecDimmer
[ true ] {
  t1: (true) -> TDecDimmer() { 
      t1a: (default) -> done;
  }
}

strategy AddServer
[ true ] {
  t1: (true) -> TAddServer() @[25000 /*ms*/] { 
      t1a: (default) -> done;
  }
}

strategy RemoveServer
[ true ] {
  t1: (true) -> TRemoveServer() { 
      t1a: (default) -> done;
  }
}

