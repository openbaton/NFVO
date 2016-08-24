#!/bin/bash

#Check if there are screens up and deletd them
result=$(screen -ls | grep client | wc -l);
if [ "${result}" -ne "0" ]; then
		echo "client running, it will be killed"
		pkill screen
fi

#Clean existing configuration files
if [ -f /root/iperf-server-ip ]; then
	echo "deleting config file"
	rm /root/iperf-server-ip
fi

#Configuration
touch /root/iperf-server-ip
echo "$server_private" > /root/iperf-server-ip
