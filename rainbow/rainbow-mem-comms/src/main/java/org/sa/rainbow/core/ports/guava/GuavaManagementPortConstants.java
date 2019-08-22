package org.sa.rainbow.core.ports.guava;

public interface GuavaManagementPortConstants {
    String SEND_CONFIGURATION_INFORMATION = "sendConfigurationInformationMsg";
    String REQUEST_CONFIG_INFORMATION = "requestConfigurationInformationMsg";
    String RECEIVE_HEARTBEAT = "receiveHeartBeatMsg";
    String START_DELEGATE = "startDelegateMsg";
    String TERMINATE_DELEGATE = "terminateDelegateMsg";
    String PAUSE_DELEGATE = "pauseDelegateMsg";
    String START_PROBES = "startProbesMsg";
    String KILL_PROBES = "killProbesMsg";
}
