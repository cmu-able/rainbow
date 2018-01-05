package org.sa.rainbow.brass.gauges.acme;

import org.acmestudio.acme.element.IAcmeComponentType;
import org.acmestudio.acme.element.IAcmeConnectorType;
import org.acmestudio.acme.element.IAcmeFamily;
import org.acmestudio.acme.element.IAcmePortType;
import org.acmestudio.acme.element.IAcmeRoleType;
import org.acmestudio.acme.model.util.UMComponentType;
import org.acmestudio.acme.model.util.UMConnectorType;
import org.acmestudio.acme.model.util.UMFamily;
import org.acmestudio.acme.model.util.UMPortType;
import org.acmestudio.acme.model.util.UMRoleType;

public interface ROSAcmeStyle {

    IAcmeFamily ROSFam = new UMFamily ("ROSFam");
    IAcmeComponentType ROSNodeCompT = new UMComponentType ("ROSNodeCompT");
    IAcmeComponentType ROSNodeManagerCompT = new UMComponentType ("ROSNodeManagerCompT");
    IAcmeComponentType ROSNodeletCompT = new UMComponentType ("ROSNodeletCompT");
    IAcmePortType ActionClientPortT = new UMPortType ("ActionClientPortT");
    IAcmePortType ActionServerPortT = new UMPortType ("ActionServerPortT");
    IAcmePortType ServiceClientPortT = new UMPortType ("ServiceClientPortT");
    IAcmePortType ServiceProviderPortT = new UMPortType ("ServiceProviderPortT");
    IAcmePortType TopicAdvertisePortT = new UMPortType ("TopicAdvertisePortT");
    IAcmePortType TopicSubscribePortT = new UMPortType ("TopicSubscribePortT");
    IAcmeConnectorType ActionServerConnT = new UMConnectorType ("ActionServerConnT");
    IAcmeConnectorType ServiceConnT = new UMConnectorType ("ServiceConnT");
    IAcmeConnectorType TopicConnectorT = new UMConnectorType ("TopicConnectorT");
    IAcmeRoleType ROSTopicAdvertiserRoleT = new UMRoleType ("ROSTopicAdvertiserRoleT");
    IAcmeRoleType ROSTopicSubscriberRoleT = new UMRoleType ("ROSTopicSubscriberRoleT");
    IAcmeRoleType ROSServiceResponderRoleT = new UMRoleType ("ROSServiceResponderRoleT");
    IAcmeRoleType ROSServiceCallerRoleT = new UMRoleType ("ROSServiceCallerRoleT");
    IAcmeRoleType ROSActionCallerRoleT = new UMRoleType ("ROSActionCallerRoleT");
    IAcmeRoleType ROSActionResponderRoleT = new UMRoleType ("ROSActionResponderRoleT");

    public static enum Operations {
        NEW_ROS_SYSTEM("create-system"), NEW_ROS_NODE("create-ros-node"), NEW_ROS_NODE_MANAGER(
                "create-ros-node-manager"), DELETE_PORT("delete-port"), CREATE_ATTACHMENT("attach"), DELETE_CONNECTOR(
                        "delete-connector"), DELETE_ROLE(
                                "delete-role"), CREATE_ACTION_PORT("create-action-port"), CREATE_ACTION_CONNECTOR(
                                        "create-action-connector"), CREATE_ACTION_ROLE(
                                                "create-action-role"), CREATE_TOPIC_PORT(
                                                        "create-topic-port"), CREATE_TOPIC_ROLE(
                                                                "create-topic-role"), CREATE_TOPIC_CONNECTOR(
                                                                        "create-topic-connector"), DELETE_COMPONENT(
                                                                                "delete-component"), CREATE_SERVICE_RESPONDER_PORT(
                                                                                        "create-service-port");

        private final String m_name;

        private Operations (String n) {
            m_name = n;
        }

        public String opname () {
            return m_name;
        }
    }

}
