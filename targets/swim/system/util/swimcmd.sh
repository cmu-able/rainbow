#!/bin/bash
# sends a command to the SWIM waits for a
# single-line response
if [ -z "$SOCAT_PORT" ]; then
  SOCAT_PORT=4243
fi
CMDHELPER=`dirname $0`/cmdhelper.sh
HOST=`hostname`
socat TCP:${HOST}:${SOCAT_PORT} exec:${CMDHELPER}\ "$*",fdout=4



