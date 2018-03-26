#!/bin/bash 
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
rostopic pub -1 /sensor/kinect/onoff std_msgs/String \"$1\"