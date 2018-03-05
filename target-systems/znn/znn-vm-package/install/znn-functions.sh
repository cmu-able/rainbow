# Sets up the znn properties to be read by the script.
# Replaces properties with '.' to properties with "_"
# and creates a variable for each property
# Receives one argument: the file defining the properties
function setup_znn_properties {
  PROP_FILE=$1
  TMP=$(mktemp)
  echo "  Reading in from '$PROP_FILE'"
  cat $PROP_FILE | awk -f "$ZNN_BIN_DIR/readproperties.awk" > $TMP
  cat $TMP | sed 's/_=/=/g' > $TMP.1
  cat $TMP.1 | sed 's/" /"/g' > $TMP.2
  source $TMP.2 2> /dev/null
}

# Sets up the balancer file in the load balancer.
# Receives the balancer file as parameter
function setup_balancer_file {
  BF="$1"
  balance=()
  balancePorts=()
  i=0
  while true; do
    tmp="customize_system_target_web${i}"
    tmpPort="customize_system_target_web${i}_httpPort"
    web=${!tmp}
    webPort=${!tmpPort}
    tmp="customize_system_target_web${i}_disabled"
    disabled=${!tmp}
    if [[ $web == "" ]]; then
      break;
    fi

    if [[ $web != "" && $disabled != "true" ]]; then
      ip=`echo $web | sed -e 's/^ *//g' -e 's/ *$//g'`
      balance=(${balance[@]} "$ip")
      port=`echo $webPort | sed -e 's/^ *//g' -e 's/ *$//g'`
      balancePorts=(${balancePorts[@]} "$port")
    fi
    i=$(($i+1))
  done

  total=${#balance[*]}
  for ((i=0; i<=$(($total-1)); i++)); do
    echo "Adding customize.system.target.web${i}"
    echo "BalancerMember http://${balance[$i]}:${balancePorts[$i]}/ retry=5 timeout=30 loadfactor=1" >> $BF
  done
  
} 
