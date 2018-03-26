#!/bin/bash
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash

sensor=$1
if [ "$sensor" == "HEADLAMP" ]; then
	if [ "$2" == "on" -o "$2" == "true" ]; then
		rosservice call /mobile_base/headlamp True
	else
		rosservice call /mobile_base/headlamp False
	fi
fi
