#!/bin/bash
if [[ "$#" != 2 ]]; then
  echo $0 {fidelity} {file}
  exit 1
fi

FIDELITY_FILE=$2

if [[ $1 -eq 1 ]]; then
  echo "low" > $FIDELITY_FILE
elif [[ $1 -eq 3 ]]; then
  echo "text" > $FIDELITY_FILE
else
  echo "high" > $FIDELITY_FILE
fi
APACHE_INSTALL/bin/httpd -k restart


