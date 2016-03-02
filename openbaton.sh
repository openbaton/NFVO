#!/bin/bash

source ./gradle.properties

_version=${version}

_openbaton_base="/opt/openbaton/"
_message_queue_base="apache-activemq-5.11.3"
_openbaton_config_file=/etc/openbaton/openbaton.properties

function start_activemq_linux {
    sudo ${_openbaton_base}/${_message_queue_base}/bin/activemq start
    if [ $? -ne 0 ]; then
        echo "ERROR: activemq is not running properly (check the problem in ${_openbaton_base}/${_message_queue_base}/data/activemq.log) "
	exit 1
    fi
}

function start_activemq_osx {
    sudo ${_openbaton_base}/${_message_queue_base}/bin/macosx/activemq start
    if [ $? -ne 0 ]; then
        echo "ERROR: activemq is not running properly (check the problem in ${_openbaton_base}/${_message_queue_base}/data/activemq.log) "
	exit 1
    fi
}

function check_activemq {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	ps -aux | grep -v grep | grep activemq > /dev/null
        if [ $? -ne 0 ]; then
          	echo "activemq is not running, let's try to start it..."
            	start_activemq_linux
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	ps aux | grep -v grep | grep activemq > /dev/null
        if [ $? -ne 0 ]; then
          	echo "activemq is not running, let's try to start it..."
            	start_activemq_osx
        fi
    fi
}

function check_rabbitmq {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	ps -aux | grep -v grep | grep rabbitmq > /dev/null
        if [ $? -ne 0 ]; then
          	echo "rabbit is not running, let's try to start it..."
            	start_rabbitmq
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	ps aux | grep -v grep | grep rabbitmq > /dev/null
        if [ $? -ne 0 ]; then
          	echo "rabbitmq is not running, let's try to start it..."
            	start_rabbitmq
        fi
    fi
}


function start_rabbitmq {
    `rabbitmq-server -detached`
    if [ $? -ne 0 ]; then
        echo "ERROR: rabbitmq is not running properly (check the problem in /var/log/rabbitmq.log) "
        exit 1
    fi
}

function start_mysql_osx {
    sudo /usr/local/mysql/support-files/mysql.server start
}

function start_mysql_linux {
    sudo service mysql start
}


function check_mysql {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	result=$(pgrep mysql | wc -l);
        if [ ${result} -eq 0 ]; then
                read -p "mysql is down, would you like to start it ([y]/n):" yn
		case $yn in
			[Yy]* ) start_mysql_linux ; break;;
			[Nn]* ) echo "you can't proceed withuot having mysql up and running" 
				exit;;
			* ) start_mysql_linux;;
		esac
        else
                echo "mysql is already running.."
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	mysqladmin status
	result=$?
        if [ "${result}" -eq "0" ]; then
                echo "mysql service running..."
        else
                read -p "mysql is down, would you like to start it ([y]/n):" yn
                case $yn in
                        [Yy]* ) start_mysql_osx ; break;;
                        [Nn]* ) exit;;
                        * ) start_mysql_osx;;
                esac
        fi
    fi
}


function check_already_running {
        result=$(screen -ls | grep openbaton | wc -l);
        if [ "${result}" -ne "0" ]; then
                echo "openbaton is already running.."
		exit;
        fi
}

# Check if the property nfvo.timezone is set
# The nfvo.timezone property syncronize all the VNF with the clock of the NFVO
function check_timezone {
	if ! grep nfvo.timezone $_openbaton_config_file > /dev/null ; then
		TIMEZONE=$( date +%Z )
		echo "nfvo.timezone = $TIMEZONE" >> ${_openbaton_config_file}
	fi
}

function start {

    if [ ! -d build/  ]
        then
            compile
    fi

#    check_activemq
    check_rabbitmq
    check_timezone
    #check_mysql
    check_already_running
    if [ 0 -eq $? ]
        then
	    #screen -X eval "chdir $PWD"
	    screen -c screenrc -d -m -S openbaton -t nfvo java -jar "build/libs/openbaton-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
	    #screen -c screenrc -r -p 0
    fi
}

function stop {
    if screen -list | grep "openbaton"; then
	    screen -S openbaton -p 0 -X stuff "exit$(printf \\r)"
    fi
}

function restart {
    kill
    start
}


function kill {
    if screen -list | grep "openbaton"; then
	    screen -ls | grep openbaton | cut -d. -f1 | awk '{print $1}' | xargs kill
    fi
}


function compile {
    ./gradlew build -x test 
}

function tests {
    ./gradlew test
}

function update {
    echo "~~~~~~~~~~~~~~~~~~~~OpenBaton UPDATE~~~~~~~~~~~~~~~~~~~~"
    echo "                                                        "
    echo "              updating to version 0.15"
    echo "                                                        "
    echo "installing new requirements:"
    echo "*) rabbitmq"
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        sudo apt-get update
        sudo apt-get install -y rabbitmq-server

        ulimit -S -n 4096

        sudo rabbitmqctl add_user admin openbaton
        sudo rabbitmqctl set_user_tags admin administrator
        sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

        sudo rabbitmq-plugins enable rabbitmq_management

        sudo service rabbitmq-server restart
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        brew update
        brew install rabbitmq

        ulimit -S -n 4096

        rabbitmqctl add_user admin openbaton
        rabbitmqctl set_user_tags admin administrator
        rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

        rabbitmq-plugins enable rabbitmq_management

        rabbitmqctl stop
        rabbitmq-server start -detached
    fi
}

function clean {
    ./gradlew clean
}

function end {
    exit
}
function usage {
    echo -e "Open-Baton\n"
    echo -e "Usage:\n\t ./openbaton.sh [compile|start|stop|test|kill|clean]"
}

##
#   MAIN
##

if [ $# -eq 0 ]
   then
        usage
        exit 1
fi

declare -a cmds=($@)
for (( i = 0; i <  ${#cmds[*]}; ++ i ))
do
    case ${cmds[$i]} in
        "clean" )
            clean ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "update" )
            update ;;
        "stop" )
            stop ;;
        "restart" )
            restart ;;
        "compile" )
            compile ;;
        "kill" )
            kill ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
    if [[ $? -ne 0 ]]; 
    then
	    exit 1
    fi
done

