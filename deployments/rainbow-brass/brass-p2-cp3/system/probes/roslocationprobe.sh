#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
rosrun tf tf_echo $1 $2