#!/bin/bash

source ./gradle.properties

_version=${version}

_openbaton_base="/opt/openbaton/"
_openbaton_config_file="/etc/openbaton/openbaton-nfvo.properties"
_openbaton_plugins="http://get.openbaton.org/plugins/stable/"
_message_queue_base="apache-activemq-5.11.3"
_nfvo="${_openbaton_base}/nfvo"
_nfvo_vim_drivers="${_nfvo}/plugins/vim-drivers"
_tmpfolder=`mktemp -d`
_screen_session_name="openbaton"
_screen_name="nfvo"


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
}

function start {
    start_checks
    screen_exists=$(screen -ls | grep "\.${_screen_session_name}" | wc -l);
    if [ "${screen_exists}" -eq "0" ]; then
    	echo "Starting the NFVO in a new screen session (attach to the screen with screen -x openbaton)"
	    screen -c screenrc -d -m -S "${_screen_session_name}" -t ${_screen_name} java -jar "build/libs/openbaton-nfvo-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
    else
        echo "Starting the NFVO in the existing screen session (attach to the screen with screen -x openbaton)"
        screen -S ${_screen_session_name} -X screen -t ${_screen_name} java -jar "build/libs/openbaton-nfvo-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
    fi
}

function start_fg {
    start_checks
    java -jar "build/libs/openbaton-nfvo-$_version.jar" --spring.config.location=file:${_openbaton_config_file}
}


function stop {
    if screen -list | grep "\.${_screen_session_name}" > /dev/null ; then
	    screen -S ${_screen_session_name} -p ${_screen_name} -X stuff $'\003'
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

