#!/bin/bash
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

if [[ -z ${customize_system_target_lb+x}\
     && -z ${customize_system_target_db+x}\
     && -z ${customize_system_target_web0+x}\
	 && -z ${customize_system_target_web1+x}\
	 && -z ${customize_system_target_web2+x}\
	 && -z ${customize_system_target_web3+x}\
	 && -z ${customize_system_target_web4+x}\
	 && -z ${customize_system_target_web5+x} ]]; then

  if [[ "$#" != 1 ]]; then
    echo "$PROG [-i credentials | -u user] [-d db -l lb -0 w0 -1 w1 ... | target_properties]"
    exit 1;
  fi

  . functions.sh
  setup_znn_properties $1

fi

# Start up the appropriate services on the machines
if [[ ! -z ${customize_system_target_lb+x} ]]; then
  # Start the load balancer
  echo Starting load balancer
  $SSH $SSHUSER@$customize_system_target_lb 'nohup ./sw-bin/httpd-lb-2.4.2/bin/httpd -k start' 
fi

# Start the database
if [[ ! -z ${customize_system_target_db+x} ]]; then
  echo Starting database
  $SSH $SSHUSER@$customize_system_target_db 'nohup ./sw-bin/mysql-5.5.25/bin/mysqld_safe &'
fi

# Start the servers (unless it is marked as disabled)
for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  web=${!tmp}
  tmp="customize_system_target_web${i}_disabled"
  disabled=${!tmp}
  if [[ $web != "" && $disabled != "true" ]]; then
    s=$i
    if [[ "$i" -gt 1 ]]; then s=0; fi
    echo Starting Server $i
    $SSH $SSHUSER@$web 'bash -s' << ENDW
./sw-bin/httpd-web${s}-2.4.2/bin/httpd -k start 
ENDW
  fi
done
