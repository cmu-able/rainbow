#!/bin/bash
echo "$1" > /tmp/ig
. /opt/ros/kinetic/setup.bash
. ~/catkin_ws/devel/setup.bash
export ROS_HOSTNAME=cp3_ta
export ROS_MASTER_URI=http://cp3_ta:11311
echo "Canceling task" >> /tmp/setInstructions.log
python ~/catkin_ws/src/ig-interpreter/ig_action_client/src/ig_client.py CANCEL
echo "Starting new IG" >> /tmp/setInstructions.log
python ~/catkin_ws/src/ig-interpreter/ig_action_client/src/ig_client.py /tmp/ig
echo "Done" >> /tmp/setInstructions.log
# Wait for ig_action_server to start the graph
#rostopic echo -n 1 --filter "'Executing the graph' in m.feedback.sequence" /ig_action-server/feedback/sequence
#rm -f /tmp/ig 
