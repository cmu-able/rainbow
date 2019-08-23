#!/bin/bash
uuid=$(cat /proc/sys/kernel/random/uuid)
echo "$1" > ~/instructions/${uuid}.ig
