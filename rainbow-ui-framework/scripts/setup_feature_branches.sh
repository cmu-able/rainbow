#!/bin/bash
if [ $# -eq 0 ]
then
    echo "You should provide feature name"
    exit
fi
git checkout integration_testing
if [ $? -eq 0 ]
then
    git checkout -b $1
    git push --set-upstream origin $1
    git checkout -b $1-dev
    git push --set-upstream origin $1-dev
fi
