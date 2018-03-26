#!/bin/bash 
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
python ~/catkin_ws/src/mars_notifications/src/mars_notification_publisher.py $1 "I will arrive at $1"