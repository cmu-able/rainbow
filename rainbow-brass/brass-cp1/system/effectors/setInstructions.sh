#!/bin/bash
echo "$@" >> /tmp/ig
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
python ~/catkin_ws/src/ig_action_server/ig_action_client/src/ig_client.py /tmp/ig
rm -f /tmp/ig 