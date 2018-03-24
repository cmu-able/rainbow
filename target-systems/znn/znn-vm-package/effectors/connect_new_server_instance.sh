#!/usr/bin/bash
SSHPASS="sshpass"
SSHPWD=znn
SSHUSER=znn
PROG=$0

function usage () {
  echo "Usage: $PROG [-i credentials] [-u znn_user] <lb-ip> <db-ip> <ws-ip>"
  echo "    -i -the credentials to use to connect to the machine"
  echo "    -u -the user under which znn runs"
  echo "    <lb-ip> - the ip of the load balancer to connect to"
  echo "    <db-ip> - the ip of the database to connect to"
  echo "    <ws-ip> - the ip of the new webserver that needs connected to the lb and the db"

}

while getopts :i:u: opt; do
  case $opt in
        i)
          SSHPASS="-i $OPTARG"
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

if [[ "$#" != 3 ]]; then
  usage
  exit 1
fi
 
customize_system_target_lb=$1
customize_system_target_db=$2
customize_system_target_ws=$3

# Connect web server to the database
$SSH $SSHUSER@${customize_system_target_ws} DB="$customize_system_target_db" 'bash -s' << 'ENDSSH'
effectors/connect-db.sh $DB
ENDSSH

$SSH $SSHUSER@${customize_system_target_lb} WS="$customize_system_target_ws" 'bash -s' << 'ENDSSH2'
effectors/enable-server on ${WS}:1080
ENDSSH2
