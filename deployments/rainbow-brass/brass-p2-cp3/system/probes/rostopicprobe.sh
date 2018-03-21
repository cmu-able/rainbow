#!/bin/bash
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
./ros_topic_probe.py $@
