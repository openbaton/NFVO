#!/bin/bash

# This script is supposed to be used with server_configure_only.sh
# With this script the iperf-client starts only

file="/root/iperf-server-ip" #the file where server_configure_only.sh script stored the iperf-server ip 

IPERF_SERVER_IP=$(cat "$file")

screen -d -m -S client iperf -c $IPERF_SERVER_IP -t 300
