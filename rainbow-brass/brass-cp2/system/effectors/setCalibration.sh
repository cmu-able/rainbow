#!/bin/bash
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
echo "Attempting to recalibrate"
rostopic pub -1 /calibration/commands std_msgs/String \"recalibrate\"