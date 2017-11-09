#!/usr/bin/bash
SSHPASS="sshpass"
SSHPWD=znn
SSHUSER=znn
PROG=$0
DELAY=-1
RESET_CONFIGURATION=1
CLEAR_LOGS=0

function usage () {
  echo "Usage: $PROG [-i credentials] [-u znn_user] [-y delay] [-l lb -d db -0 web0 -1 web1 ...] | [znn_properties]"
  echo "    -i -the credentials to use to connect to the machine"
  echo "    -u -the user under which znn runs"
  echo "    -y -the delay (in Mb of randomly generated data) to generate on each"
  echo "        request to instill a delay in processing"
  echo "    -r -do not reset the configuration"
  echo "    -l,-d,-0... - the IPs of the load balancer, database and web<n>"
  echo "    znn_properties - the properties file from whence to get the lb, db, web<n> IPs"
}

while getopts :i:u:l:d:0:1:2:3:4:5:y:rc opt; do
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
	y)
	  DELAY=$OPTARG
	  RESET_CONFIGURATION=0
	  ;;
	r)
	  RESET_CONFIGURATION=0
	  ;;
	c)
	  CLEAR_LOGS=1
	  RESET_CONFIGURATION=0
	  ;;
	\?)
	  usage
	  exit 1
	  ;;
	:)
	  usage
	  exit 1
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
    usage
    exit 1
  fi

  . functions.sh
  setup_znn_properties $1

fi

# the trailing underscore is a foible of the awk program
# because there is a space before the =
echo "lb is on $customize_system_target_lb"
echo "db is on $customize_system_target_db"
for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  web=${!tmp}
  if [[ $web != "" ]]; then
    echo "web$i is on $web"
  fi
done

if [[ ! -z ${customize_system_target_lb+x} ]]; then
  if [ "$CLEAR_LOGS" -eq 1 ]; then
    echo "Clearing logs in ${customize_system_target_lb}"
    	   $SSH $SSHUSER@${customize_system_target_lb} i=$s 'bash -s' << 'ENDSSH4'
rm -f ./sw-bin/httpd-lb-2.4.2/log/error_log
rm -f ./sw-bin/httpd-httpd-lb-2.4.2/log/access_log
ENDSSH4
  fi
fi

# set up web servers to point to the db
for i in 0 1 2 3 4 5; do
  tmp="customize_system_target_web${i}"
  web=${!tmp}
  if [[ $web != "" ]]; then
    ip=`echo $web | sed -e 's/^ *//g' -e 's/ *$//g'`
	if [ "$DELAY" -ne -1 ]; then
	  echo "Setting delay in web$i on $ip to $DELAY"
	  if [ "$DELAY" -ne 0 ]; then
	    $SSH $SSHUSER@${ip} DL=$DELAY 'bash -s' << 'ENDSSH'
echo ${DL} > /tmp/znn-delay
ENDSSH
      else
	    $SSH $SSHUSER@$ip d="$delay" 'bash -s' << 'ENDSSH2'
  echo "Removing delay"
  rm -f /tmp/znn-delay
ENDSSH2
      fi
	elif [ "$CLEAR_LOGS" -eq 1 ]; then
	   s=$i
       if [[ "$i" -gt 1 ]]; then s=0; fi
       echo Clearing logs in Server $i
	   $SSH $SSHUSER@$ip i=$s 'bash -s' << 'ENDSSH3'
rm -f ./sw-bin/httpd-web${s}-2.4.2/log/error_log
rm -f ./sw-bin/httpd-web${s}-2.4.2/log/access_log
ENDSSH3
	else 
	
	  db=`echo $customize_system_target_db | sed -e 's/^ *//g' -e 's/ *$//g'`
	  echo "Processing web$i on $ip to add $customize_system_target_db"
      $SSH $SSHUSER@$ip db=$db 'bash -s' << 'ENDSSH1'
echo "processing znn.php"
cd znn
cp news.php news-copy.php
sed s/"^\$db_host.*=.*$"/"\$db_host = \"${db}\";"/ news.php > /tmp/news.php && mv /tmp/news.php news.php
ENDSSH1
    fi
  fi
done


if [ "$RESET_CONFIGURATION" -eq "1" ]; then
  echo "Resetting effector states"
  ./reset-configuration.sh $1
fi

