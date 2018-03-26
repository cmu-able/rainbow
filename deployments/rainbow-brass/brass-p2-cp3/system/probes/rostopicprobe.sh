#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
./ros_topic_probe.py $@
