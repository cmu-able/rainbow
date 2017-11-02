#!/bin/bash
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
echo "Attempting to recalibrate"
python ~/catkin_ws/src/ig_action_server/ig_action_client/src/ig_client.py CANCEL
rostopic pub -1 /calibration/commands std_msgs/String \"recalibrate\"