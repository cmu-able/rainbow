#!/usr/bin/bash
START_DIR="$(pwd)"
ZNN_BIN_DIR="$( cd "$( dirname "$0" )" && pwd)"
ZNN_BIN_DIR=$ZNN_BIN_DIR/../install
[ -a "$ZNN_BIN_DIR/../znn-config" ] \
  && source "$ZNN_BIN_DIR/../znn-config" || {
  echo "Failed to configure the environment"
  echo "  Expected to find znn-config in '$ZNN_BIN_DIR/..'"
  exit 1
}

[ -a "$ZNN_BIN_DIR/functions.sh" ] && source "$ZNN_BIN_DIR/functions.sh" || {
  echo "Failed to load general functions"
  echo "  Expected to find 'functions.sh' in '$ZNN_BIN_DIR'"
  exit 1
}

[ -a "$ZNN_BIN_DIR/znn-functions.sh" ] && source "$ZNN_BIN_DIR/znn-functions.sh" || {
   echo "Failed to load general functions"
  echo "  Expected to find 'znn-functions.sh' in '$ZNN_BIN_DIR'"
  exit 1
}

[ "$#" -eq "1" ] || {
  echo "Usage: $0 <db_ip>"
  exit 1
}

[ "$#" == "1" ] && {
  DB_IP=$1
}

echo "Checking that znn is installed"

if [ ! -d "$ZNN_HOME/znn" ]; then
  echo "  ZNN is not installed in the expected '$ZNN_HOME/znn'.. exiting"
  exit 1
else 
  echo "  yes!"
fi

echo "Updating znn to add db information"
replace_line_regex "$ZNN_HOME/znn/news.php" \
  "^.db_host =.*" \
  "\$db_host = \"$DB_IP\";"



