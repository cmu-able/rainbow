#!/bin/bash
# sends a command to the SWIM waits for a
# single-line response
CMDHELPER=`dirname $0`/cmdhelper.sh
socat TCP:localhost:4243 exec:${CMDHELPER}\ "$*",fdout=4



