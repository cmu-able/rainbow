#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
sleep_time=$1

while [ 1 ]; do
  i=$(rosnode list)
  echo $i
  sleep $sleep_time
done
