#!/bin/bash
set TARGET=znews-ss
set DEBUG=""
set GUI=""

:GETOPTS
if /I %1 == -d set DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044"
if /I %1 == -h set GUI="-nogui"
if /I %1 == -r set ADDR=%2& shift
shift
if not (%1)==() or %1:~0,1% == - goto GETOPTS

if not (%1)==() set TARGET=%1

java -classpath ".;lib/*" -XX:+HeapDumpOnOutOfMemoryError -Drainbow.target=znews-ss -Djava.rmi.server.hostname=10.0.2.2 org.sa.rainbow.core.RainbowMaster 
