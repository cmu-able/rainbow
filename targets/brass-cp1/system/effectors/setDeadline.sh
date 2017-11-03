#!/bin/bash 
. /opt/ros/indigo/setup.bash
. ~/catkin_ws/devel/setup.bash
#echo rostopic pub -1 /notify_user mars_notifications/UserNotification {new_deadline: '${1}', user_notification: 'Will now arrive ${1}'}
#rostopic pub -1 /notify_user mars_notifications/UserNotification {new_deadline: '${1}', user_notification: 'Will now arrive ${1}'}
#python ~/catkin_ws/src/mars_notifications/src/mars_notification_publisher.py $1 $1
rostopic pub -1 /notify_user/deadline std_msgs/Int64 $1
