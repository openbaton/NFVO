#!/bin/bash

source ./gradle.properties

_version=${version}

_openbaton_base="/opt/openbaton/"
_openbaton_config_file="/etc/openbaton/openbaton.properties"
_openbaton_plugins="http://get.openbaton.org/plugins/stable/"
_message_queue_base="apache-activemq-5.11.3"
_nfvo="${_openbaton_base}/nfvo"
_nfvo_vim_drivers="${_nfvo}/plugins/vim-drivers"
_tmpfolder=`mktemp -d`
_screen_name="nfvo"


function checkBinary {
  echo -n " * Checking for '$1'..."
  if command -v $1 >/dev/null 2>&1; then
     echo "OK"
     return 0
   else
     echo >&2 "FAILED."
     return 1
   fi
}

_ex='sh -c'
if [ "$_user" != 'root' ]; then
    if checkBinary sudo; then
        _ex='sudo -E sh -c'
    elif checkBinary su; then
        _ex='su -c'
    fi
fi


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
    ${_ex} 'rabbitmq-server -detached'
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
		case ${yn} in
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
                case ${yn} in
                        [Yy]* ) start_mysql_osx ; break;;
                        [Nn]* ) exit;;
                        * ) start_mysql_osx;;
                esac
        fi
    fi
}


function check_already_running {
    pgrep -f openbaton-${_version}.jar
    if [ "$?" -eq "0" ]; then
        echo "openbaton is already running.."
        exit;
    fi
    #result=$(screen -ls | grep openbaton | wc -l);
    #if [ "${result}" -ne "0" ]; then
    #        echo "openbaton is already running.."
    #exit;
    #fi
}

# Check if the property nfvo.timezone is set
# The nfvo.timezone property syncronize all the VNF with the clock of the NFVO
function check_timezone {
	if ! grep nfvo.timezone ${_openbaton_config_file} > /dev/null ; then
		${_ex} 'TIMEZONE=$( date +%Z )'
		${_ex} 'echo "nfvo.timezone = $TIMEZONE" >> '"${_openbaton_config_file}"
	fi
}

function install_plugins {
    echo "Getting OpenBaton Plugins..."
    wget -nH --cut-dirs 2 -r --no-parent  --reject "index.html*" "${_openbaton_plugins}" -P "${_tmpfolder}"
    mkdir -p ${_nfvo_vim_drivers}
    cp -r ${_tmpfolder}/* "${_nfvo_vim_drivers}"
}


function start_checks {
    check_already_running
    if [ ! -d build/  ]
        then
            compile
    fi
    check_rabbitmq
    check_timezone
}

function start {
    start_checks
    screen_exists=$(screen -ls | grep openbaton | wc -l);
    if [ "${screen_exists}" -eq "0" ]; then
    	echo "Starting the NFVO in a new screen session (attach to the screen with screen -x openbaton)"
	    screen -c screenrc -d -m -S openbaton -t ${_screen_name} java -jar "build/libs/openbaton-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
    else
        echo "Starting the NFVO in the existing screen session (attach to the screen with screen -x openbaton)"
        screen -S openbaton -X screen -t ${_screen_name} java -jar "build/libs/openbaton-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
    fi
}

function start_fg {
    start_checks
    java -jar "build/libs/openbaton-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
}


function stop {
    if screen -list | grep "openbaton" > /dev/null ; then
	    screen -S openbaton -p ${_screen_name} -X stuff $'\003'
    fi
}

function restart {
    kill
    start
}


function kill {
    pkill -9 -f openbaton-${_version}.jar
    screen -wipe > /dev/null
}


function compile {
    ./gradlew goJF build -x test 
}

function tests {
    ./gradlew test
}

function clean {
    ./gradlew clean
}

function end {
    exit
}
function usage {
    echo -e "Open-Baton\n"
    echo -e "Usage:\n\t ./openbaton.sh [compile|install_plugins|start|start_fg|stop|test|kill|clean]"
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
        "install_plugins" )
            install_plugins ;;
        "sc" )
            clean
            compile
            start ;;
        "start" )
            start ;;
        "start_fg" )
            start_fg ;;
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

