#!/bin/bash

sudo apt-get install rabbitmq-server
rabbitmqctl add_user admin openbaton
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
sudo service rabbitmq-server restart
