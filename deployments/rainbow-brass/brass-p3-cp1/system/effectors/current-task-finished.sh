#!/usr/bin/env bash

FILE=~/cp1/current-task-finished
DONEMSG="DONE"
FAILEDMSG="FAILED"

if [ -e $FILE ]
then
    if [ $1 != 0 ]
    then
        echo $DONEMSG > $FILE
    else
        echo $FAILEDMSG > $FILE
    fi
else
    echo "file does not exists"
fi
