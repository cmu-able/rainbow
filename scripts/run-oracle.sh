#!/bin/bash

TARGET=znews-ss
DEBUG=""
GUI=""
WD="."

while getopts :dhr:w: opt; do
  case $opt in
    d)
	  DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"
	  ;;
	h)
	  GUI="-nogui"
	  ;;
	r)
	  ADDR=$OPTARG
	  ;;
  w)
    WD=$OPTARG
    ;;
   esac
done
shift $((OPTIND-1))

if [[ "$#" -ge 1 ]]; then
  TARGET=$1
  shift
else
  echo "Warning: target not passed as a parameter, using $TARGET as default"
fi

cd $WD

BACKUP_IP=10.0.2.2

# Check if rainbow.properties file exists
TARGET_DIR="targets/$TARGET"
if [ ! -d "$TARGET_DIR" ]; then
    echo "Error: target directory $TARGET_DIR" does not exist.
    exit 1
fi



if [ -z "$ADDR" ]; then
    ADDR=$BACKUP_IP
fi

if [ "$(uname)" == "Darwin" ]; then
  delim=":"
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  delim=":"
elif [ "$(expr substr $(uname -o) 1 6)" == "Cygwin" ]; then
  delim=";"
fi

echo "java -classpath .${delim}lib/*  -Drainbow.target=$TARGET -Djava.rmi.server.hostname=$ADDR $DEBUG $* org.sa.rainbow.core.RainbowMaster"

java -classpath ".${delim}lib/*"  -Drainbow.target=$TARGET -Djava.rmi.server.hostname=$ADDR $DEBUG $* org.sa.rainbow.core.RainbowMaster $GUI

 
