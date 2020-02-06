#!/bin/sh
LIB=`dirname $0`/lib
CP=`dirname $0`/bin:$LIB/*
SATLIBPATH=amd64-linux

# extract SAT solver libraries if not already extracted
if [ ! -e $LIB/$SATLIBPATH ]; then
    echo Extracting SAT solver libs
    (cd $LIB; jar xf alloy4.2.jar $SATLIBPATH)
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$LIB/$SATLIBPATH
java -classpath $CP reach.Reach "$@"
