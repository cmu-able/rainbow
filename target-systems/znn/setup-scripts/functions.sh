#function to see if an element is contained in an array
#   contains elements element
#     elements : the array of existing elements
#     element  : the element to find
function contains() { 
  local elements="${1}[@]"
  local element=${2} 
  echo ${!elements} $element
  for i in ${!elements} ; do
    if [ $i=$element ] ; then
	  return 1
	fi
  done
  return 0
}

#function to return the set of ips associated with the znn system
#   get_znn_ips var
#     var : the name of the variable to put the result in
function get_znn_ips() {
	#set up the ips array so that it has a unique set of ips on which to start delegates
	local __resultVar=$1
	local __resultips=()
	if [[ ${__resultips[*]} =~ $customize_system_target_lb ]]; then echo ""; else __resultips+=($customize_system_target_lb); fi
	if [[ ${__resultips[*]} =~ $customize_system_target_db ]]; then echo ""; else __resultips+=($customize_system_target_db); fi
	for i in 0 1 2 3 4 5; do
		tmp="customize_system_target_web${i}"
		web=${!tmp}
		if [[ $web != "" ]]; then
			if [[ ${__resultips[*]} =~ $web ]]; then echo ""; else __resultips+=($web); fi
		fi
	done
	eval $__resultVar='(${__resultips[@]})'
}
#set up the properties file so it can be ready by bash, and source it to add the variables
# .'s in property definitions will be replaced on _'s
#    setup_znn_properties properties
#       properties : the properties file
function setup_znn_properties () {
	cat $1 | awk -f readproperties.awk > rainbow.properties.tmp1
	cat rainbow.properties.tmp1 | sed 's/_=/=/g' > rainbow.properties.tmp2
	cat rainbow.properties.tmp2 | sed 's/" /"/g' > rainbow.properties.tmp
	. rainbow.properties.tmp 2> /dev/null
	rm -f rainbow.properties.tmp1 rainbow.properties.tmp2 rainbow.properties.tmp
}

# Checks to see if the argument is a valid ip address
#    valid_ip ip
#       ip : a value to see if it is a valid ip
function valid_ip() {
	local ip=$1
	local stat=1
	
	if [[ $ip =~ [0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        OIFS=$IFS
        IFS='.'
        ip=($ip)
        IFS=$OIFS
        [[ ${ip[0]} -le 255 && ${ip[1]} -le 255 \
            && ${ip[2]} -le 255 && ${ip[3]} -le 255 ]]
        stat=$?
    fi
    return $stat
}
