#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
python ~/catkin_ws/src/ig-interpreter/ig_action_client/src/ig_client.py CANCEL
