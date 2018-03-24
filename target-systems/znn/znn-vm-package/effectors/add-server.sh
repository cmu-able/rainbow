#!/usr/bin/bash
PROG=$0
F_BIN_DIR="$( cd "$( dirname "$0" )" && pwd)"
function usage () {
  echo "Usage: $PROG [-i credentials] [-u znn_user] <lb-ip> <db-ip>"
  echo "    -i -the credentials to use to connect to the machine"
  echo "    -u -the user under which znn runs"
  echo "    <lb-ip> - the ip of the load balancer to connect to"
  echo "    <db-ip> - the ip of the database to connect to"

}

while getopts :i:u: opt; do
  case $opt in
        i)
          SSHPASS="-i $OPTARG"
		  CREDENTIALS="$OPTARG"
          ;;
        u)
          SSHUSER=$OPTARG
          ;;
        \?)
          usage
          exit 1
          ;;
        :)
          usage
          exit 1
  esac
done

shift $((OPTIND-1))

if [ "$SSHPASS" == "sshpass" ]; then
  SSH="ssh"
else
  SSH="ssh $SSHPASS"
fi

if [[ "$#" != 2 ]]; then
  usage
  exit 1
fi
 
customize_system_target_lb=$1
customize_system_target_db=$2

ping -c1 ${customize_system_target_lb} || {
  echo "${customize_system_target_lb}: IP is not reachable"
  exit 1;
}

# Create the server instance
create_details=$($F_BIN_DIR/create_new_server_instance.sh)
details_a=($create_details)
customize_system_target_ws=${details_a[0]}

echo "Created server at $customize_system_target_ws, took ${details_a[1]}"
connect_cmd="$F_BIN_DIR/connect_new_server_instance.sh -u $SSHUSER"
if [[ "x$CREDENTIALS" != "x" ]]; then
  connect_cmd="$connect_cmd -i $CREDENTIALS" 
fi

connect_cmd="$connect_cmd $customize_system_target_lb $customize_system_target_db $customize_system_target_ws"
cmd_output=$($connect_cmd)
echo $cmd_output