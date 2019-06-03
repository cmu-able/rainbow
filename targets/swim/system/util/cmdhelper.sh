#!/bin/bash
# this is a helper program intended to be used as follows:
# socat TCP:localhost:4243 exec:./tcpcommand.sh\ get_utilization\ 1,fdout=4
echo $* >&4
read REPLY
echo $REPLY

