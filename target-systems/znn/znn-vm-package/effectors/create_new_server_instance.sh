#!/usr/bin/bash

# Use AWS API to create a new web server instance, and connect it up to the load balancer
# Parameters: the ip of the load balancer that will be connected to

# variables for the AWS commands
ami_id=ami-95e8c6ff
subnet=subnet-d45ce9a3
sec_groups=(sg-e9a9b58c sg-7eaeb21b sg-a7b1f4c3)
public_ip=true

# form the command to create and run a new instance
start_cmd="ec2-run-instances $ami_id -t t2.micro -s $subnet"
for i in "${!sec_groups[@]}"
do
  start_cmd="$start_cmd -g ${sec_groups[i]}"
done

start_cmd="$start_cmd --associate-public-ip-address $public_ip"
#echo $start_cmd
start_time=$(date +%s)
cmd_output=$($start_cmd)
output_array=($cmd_output)

len=${#output_array[@]}
ip_pos=$((len-2))
instance_local_ip=${output_array[${ip_pos}]}
instance_id=${output_array[4]}

#now wait for it to come up
status="waiting"
while [ "$status" != "ok" ]; do
   status_o=$(ec2-describe-instance-status $instance_id)
   status_a=($status_o)
   status=${status_a[5]}
#   echo $status
   sleep 1;
done

end_time=$(date +%s)

duration=$((end_time-start_time)) 


#echo "Started new instance ($ami_id) at $ami_ip, took $duration seconds" 
# Get the private IP address of the new server
status_o=$(ec2-describe-instances $instance_id)
status_a=($status_o)
ip="xxx"
for i in ${status_a[@]}; do 
  if [ "$ip" = "next" ]; then 
    ip=$i; break; 
  fi; 
  if [ "$i" = "PRIVATEIPADDRESS" ]; then 
    ip="next"; 
  fi; 
done

echo $ip $duration
