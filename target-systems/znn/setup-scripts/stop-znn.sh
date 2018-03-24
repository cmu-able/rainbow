#!/usr/bin/bash
SSHPASS="sshpass"
SSHPWD=znn
SSHUSER=znn
PROG=$0

while getopts :i:u:l:d:0:1:2:3:4:5 opt; do
  case $opt in
    i)
	  SSHPASS="-i $OPTARG"
	  ;;
	u)
	  SSHUSER=$OPTARG
	  ;;
	l)
	  customize_system_target_lb=$OPTARG
	  ;;
	d)
	  customize_system_target_db=$OPTARG
	  ;;
	0) 
	  customize_system_target_web0=$OPTARG
	  ;;
	1)
	  customize_system_target_web1=$OPTARG
	  ;;
	2)
	  customize_system_target_web2=$OPTARG
	  ;;
	3)
	  customize_system_target_web3=$OPTARG
	  ;;
	4)
	  customize_system_target_web4=$OPTARG
	  ;;
	5)
	  customize_system_target_web5=$OPTARG
	  ;;
   esac
done
shift $((OPTIND-1))

if [ "$SSHPASS" == "sshpass" ]; then
  SSH="ssh"
else
  SSH="ssh $SSHPASS"
fi

if [ -z ${customize_system_target_lb+x} ]; then

  if [[ "$#" != 1 ]]; then
    echo "$PROG [-i credentials | -u user] target_properties"
    exit 1;
  fi

  . functions.sh
  setup_znn_properties $1

fi

$SSH $SSHUSER@$customize_system_target_lb 'bash -s' << 'ENDLB'
./sw-bin/httpd-lb-2.4.2/bin/httpd -k stop
ENDLB

$SSH $SSHUSER@$customize_system_target_db 'bash -s' << 'ENDDB'
killall mysqld
ENDDB

for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  web=${!tmp}
  tmp="customize_system_target_web${i}_disabled"
  disabled=${!tmp}
  if [[ $web != "" ]]; then
    $SSH $SSHUSER@$web web=$i 'bash -s' << 'ENDSSH2'
nohup ./sw-bin/httpd-web*-2.4.2/bin/httpd -k stop
ENDSSH2
  fi
done