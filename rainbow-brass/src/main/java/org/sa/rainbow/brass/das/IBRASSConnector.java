package org.sa.rainbow.brass.das;

public interface IBRASSConnector {

    public static enum DASStatusT {
        PERTURBATION_DETECTED, //  indicates that a change to the ecosystem was detected that has the potential to or has already affected intent satisfaction
        MISSION_SUSPENDED, // indicates that input processing is suspended while an adaptation response is selected/generated and deployed
        MISSION_RESUMED, // indicates that input processing is resumed, presumably because an adaptive version of the target software system is in place
        MISSION_HALTED, // indicates that an adaptation response cannot be performed online—a specified adaptation process must be invoked in an offline environment to select/generate an adaptive version of the target software system
        MISSION_ABORTED, // indicates that an adaptive response is deemed to be infeasible
        ADAPTING, //  indicates that the selection/generation of an adaptive version of the target software system has begun or is in progress
        ADAPTATION_COMPLETED, //  indicates that the adaptation response is done, and the adaptive version of the target software system has been deployed
        ERROR
    }

    void reportReady (boolean ready);

    void reportStatus (DASStatusT status, String message);

    void reportDone (boolean failed, String message);
}
