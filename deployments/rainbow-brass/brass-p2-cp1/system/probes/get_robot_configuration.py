#! /usr/bin/env python

import rospy
from brass_gazebo_config_manager.srv import *

ros_node = '/battery_monitor_client'
model_name = '/battery_demo_model'

get_configuration_srv = rospy.ServiceProxy(ros_node + model_name + '/get_robot_configuration', GetConfig)


def get_current_configuration(current_or_historical):
    res = get_configuration_srv(current_or_historical)
    return res.result


if __name__ == '__main__':
    print("cp1 configuration: %s" %get_current_configuration(1))