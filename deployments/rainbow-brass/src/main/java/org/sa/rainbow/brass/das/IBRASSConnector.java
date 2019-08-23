package org.sa.rainbow.brass.das;

public interface IBRASSConnector {

    public static enum DASPhase1StatusT {
        PERTURBATION_DETECTED, //  indicates that a change to the ecosystem was detected that has the potential to or has already affected intent satisfaction
        MISSION_SUSPENDED, // indicates that input processing is suspended while an adaptation response is selected/generated and deployed
        MISSION_RESUMED, // indicates that input processing is resumed, presumably because an adaptive version of the target software system is in place
        MISSION_HALTED, // indicates that an adaptation response cannot be performed online—a specified adaptation process must be invoked in an offline environment to select/generate an adaptive version of the target software system
        MISSION_ABORTED, // indicates that an adaptive response is deemed to be infeasible
        ADAPTING, //  indicates that the selection/generation of an adaptive version of the target software system has begun or is in progress
        ADAPTATION_COMPLETED, //  indicates that the adaptation response is done, and the adaptive version of the target software system has been deployed
        ADAPTATION_INITIATED,
        TEST_ERROR,
        // Rainbow specific messages for brasscomms
        RAINBOW_READY, MISSION_COMPLETED
    }
    
    public static enum DASPhase2StatusT {
		RAINBOW_READY, MISSION_SUCCEEDED, MISSION_FAILED,
    	ADAPTING, // the SUT has detected a condition that requires adaptation and the SUT is adapting. it is an error to send any perturbation to the SUT after this message is sent to the TH until the TH gets a	status message with adapted.
    	ADAPTED, // the SUT has finished adapting after	observing a need to. this means that the robot is moving along its plan again and it is no longer an error to send perturbations. if this is the status	code of the message, the fields plan, config and sensors will also be present, to describe the new state of the robot.
    	ADAPTED_FAILED, // The SUT failed to enact the adaptation, for some reason
    }
    
    public static enum Phases {Phase1,Phase2}

    void reportReady (boolean ready);

    void reportStatus (String status, String message);

    void reportDone (boolean failed, String message);
}
