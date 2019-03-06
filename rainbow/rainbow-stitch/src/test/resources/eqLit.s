module rubis.strategies;
import op "org.sa.rainbow.stitch.lib.Set";
import acme "src/test/resources/swim.acme" { SwimSys as M, SwimFam as T};

define boolean allEnabled = forall c:T.ServerT in M.components | c.isArchEnabled==true;
define set id = /M.components:!T.ServerT[isArchEnabled==true];

strategy TestEq [ Set.contains(id,M.server1)] {
	t1: (true) -> done;
}