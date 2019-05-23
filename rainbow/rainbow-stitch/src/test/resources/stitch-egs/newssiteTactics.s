module newssite.tactics;

import model "ZNewsSys:Acme" { ZNewsSys as M, ZNewsFam as T};
import op "org.sa.rainbow.stitch.lib.*";
import op "org.sa.rainbow.model.acme.znn.ZNN";


/**
 * Enlist n free servers into service pool.
 * Utility: [v] R; [^] C; [<>] F
 */
tactic enlistServers (int n) {
    condition {
        // some client should be experiencing high response time
        exists c : T.ClientT in M.components | c.experRespTime > M.MAX_RESPTIME;
        // there should be enough available server resources
        ZNN.availableServices(M, T.ServerT) >= n;
    }
    action {
        set servers = Set.randomSubset(ZNN.findServices(M, T.ServerT), n);
        for (T.ServerT freeSvr : servers) {
  	      M.enableServer (freeSvr, true);
        }
    }
    effect {
        // response time decreasing below threshold should result
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

/**
 * Deactivate n servers from service pool into free pool.
 * Utility: [^] R; [v] C; [<>] F
 */
tactic dischargeServers (int n) {
    condition {
        // there should be NO client with high response time
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
        // there should be enough servers to discharge
        Set.size({ select s : T.ServerT in M.components | s.load < M.MIN_UTIL }) >= n;
    }
    action {
        set lowUtilSvrs = { select s : T.ServerT in M.components | s.load < M.MIN_UTIL };
        set subLowUtilSvrs = Set.randomSubset(lowUtilSvrs, n);
        for (T.ServerT s : subLowUtilSvrs) {
            M.enableServer (s, false);
        }
    }
    effect {
        // still NO client with high response time
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

/**
 * Lowers fidelity by integral steps for percent of requests.
 * Utility: [v] R; [v] C; [v] F
 */
tactic lowerFidelity (int step, float fracReq) {
    condition {
        // some client should be experiencing high response time
        exists c : T.ClientT in M.components | c.experRespTime > M.MAX_RESPTIME;
        // exists server with fidelity to lower
        exists s : T.ServerT in M.components | s.fidelity > step;
    }
    action {
        // retrieve set of servers who still have enough fidelity grade to lower
        set servers = { select s : T.ServerT in M.components | s.fidelity > step };
        for (T.ServerT s : servers) {
            M.setFidelity(s, s.fidelity - step);
        }
    }
    effect {
        // response time decreasing below threshold should result
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

/**
 * Raises fidelity by integral steps for percent of requests.
 * Utility: [^] R; [^] C; [^] F
 */
tactic raiseFidelity (int step, float fracReq) {
    condition {
        // there should be NO client with high response time
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
        // there exists some client with below low-threshold response time
        exists c : T.ClientT in M.components | c.experRespTime < M.MIN_RESPTIME;
    }
    action {
        // first find the lowest fidelity set
        set servers = { select s : T.ServerT in M.components | s.fidelity <= M.MAX_FIDELITY_LEVEL - step};
/* smarter way of doing things...
        int lowestFidelity = M.MAX_FIDELITY_LEVEL;
        for (T.ServerT s : servers) {
            if (s.fidelity < lowestFidelity) {
                lowestFidelity = s.fidelity;
            }
        }
        // find only servers with this lowest fidelity setting, and raise fidelity
        servers = { select s : T.ServerT in M.components | s.fidelity <= lowestFidelity};
*/
        for (T.ServerT s : servers) {
            M.setFidelity(s, java.lang.Math.min(s.fidelity + step, M.MAX_FIDELITY_LEVEL));
        }
    }
    effect {
        // still NO client with high response time
        forall c : T.ClientT in M.components | c.experRespTime <= M.MAX_RESPTIME;
    }
}

/*
  T5) delayClients(set clients)
    utility: [^] R; [v] C; [v] F
  T6) blockClients(set clients)
    utility: [^] R; [v] C; [v] F
*/
