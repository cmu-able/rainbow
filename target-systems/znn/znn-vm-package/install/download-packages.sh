#!/bin/bash
START_DIR="$(pwd)"
[ -d "sw-src" -a -e "sw-src/sw-list.txt" ] && {
  readarray archives < sw-src/sw-list.txt
  for i in ${archives[@]}; do
    echo $i
    wget --directory-prefix=sw-src http://acme.able.cs.cmu.edu/public/rainbow/znn/packages/$i 
  done
} || {
  echo "There is no sw-src directory or no sw-list.txt file in sw-src/. Are you running from the right directory?"
}