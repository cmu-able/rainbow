#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
sleep_time=$1

while 1; do
  rosnode list
  sleep $sleep_time
done
