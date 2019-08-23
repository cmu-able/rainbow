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

echo "Resetting blackholed and throttled to empty, captcha off and no authentication"
$SSH $SSHUSER@$customize_system_target_lb 'bash -s' << 'ENDSSH'
effectors/ip-block-mgmt unblock ""
effectors/ip-throttle-mgmt throttle ""
effectors/captcha off
rm -f /tmp/znn-wrapper-auth-1080
ENDSSH

echo "Resetting the load balancer configuration"
#setup load balancer
balance=()
balancePorts=()
for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  tmpP="customize_system_target_web${i}_httpPort"
  web=${!tmp}
  webPort=${!tmpP}
  tmp="customize_system_target_web${i}_disabled"
  disabled=${!tmp}
  if [[ $web != "" && $disabled != "true" ]]; then
    ip=`echo $web | sed -e 's/^ *//g' -e 's/ *$//g'`
	balance=(${balance[@]} "$ip")
    port=`echo $webPort | sed -e 's/^ *//g' -e 's/ *$//g'`
    balancePorts=(${balancePorts[@]} "$port")
  fi
done

$SSH $SSHUSER@$customize_system_target_lb 'bash -s' << 'ENDSSH3'
cd sw-bin/httpd-lb-2.4.2/conf
mv balanced.conf balanced.conf-copy
ENDSSH3
total=${#balance[*]}
for ((i=0; i<=$(($total - 1)); i++)); do
  $SSH $SSHUSER@$customize_system_target_lb balance="${balance[$i]}" port="${balancePorts[$i]}" 'bash -x' << 'ENDSSH2'
cd sw-bin/httpd-lb-2.4.2/conf
echo "BalancerMember http://$balance:$port/ retry=5 timeout=30 loadfactor=1" >> balanced.conf
ENDSSH2
done

echo "Resetting the fidelities, captchas, and authentication"
for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  web=${!tmp}
  tmp="customize_system_target_web${i}_disabled"
  disabled=${!tmp}
  if [[ $web != "" ]]; then
    $SSH $SSHUSER@$web 'bash -x' << 'ENDSSH2'
effectors/changeFidelity.sh 5 /tmp/znn-fidelity-1080
effectors/captcha off
rm -f /tmp/znn-wrapper-auth-1080
ENDSSH2
  fi
done
