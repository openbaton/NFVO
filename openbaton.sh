#!/bin/bash

_version="0.5-SNAPSHOT"

_base=/opt
_openbaton_base="${_base}/openbaton"
_message_queue_base="apache-activemq-5.11.1"


function start_activemq_linux {
    sudo ${_openbaton_base}/${_message_queue_base}/bin/activemq start
}

function start_activemq_osx {
    sudo ${_openbaton_base}/${_message_queue_base}/bin/macosx/activemq start
}

function check_activemq {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
	ps -a | grep -v grep | grep activemq > /dev/null
    	result=$?
        if [ "${result}" -eq "0" ]; then
         	echo "activemq service running, everything is fine"
        else
          	echo "activemq is not running, starting it:"
            	start_activemq_linux
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
	ps aux | grep -v grep | grep activemq > /dev/null
        result=$?
         if [ "${result}" -eq "0" ]; then
          	echo "activemq service running, everything is fine"
         else
           	echo "activemq is not running, starting it:"
            	start_activemq_osx
         fi
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
	resul=$(pgrep mysql | wc -l);
        if [ "${result}" -eq "0" ]; then
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

function start {

    if [ ! -d build/  ]
        then
            compile_nfvo
    fi

    check_activemq
    check_mysql
    if [ 0 -eq $? ]
        then
            screen -S openbaton java -jar "build/libs/openbaton-$_version.jar"
    fi
}


function compile_nfvo {
    ./gradlew build -x test install
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
    echo -e "Usage:\n\t ./openbaton.sh <option>\n\t"
    echo -e "where option is"
    echo -e "\t\t * compile"
    echo -e "\t\t * start"
    echo -e "\t\t * test"
    echo -e "\t\t * clean"
}

##
#   MAIN
#   TODO start activemq and/or define application.properties
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
            compile_nfvo
            start ;;
        "start" )
            start ;;
        "compile" )
            compile_nfvo ;;
        "test" )
            tests ;;
        * )
            usage
            end ;;
    esac
done

