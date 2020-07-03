#!/bin/bash
echo diverting traffic to $1
../util/swimcmd.sh divert_traffic $1
