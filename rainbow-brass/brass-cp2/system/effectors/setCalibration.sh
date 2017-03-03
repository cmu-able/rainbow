#!/bin/bash
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
rostopic pub /calibration/recalibrate std_msgs/String \"recalibrate\"